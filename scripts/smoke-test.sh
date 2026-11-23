#!/usr/bin/env bash

set -Eeuo pipefail

# --------------------------------------------------
# Configuration
# --------------------------------------------------

BASE_URL="${BASE_URL:-http://localhost:8080}"
SUPPLIER_ID="${SUPPLIER_ID:-e96da7ca-09d0-4148-b88e-aa3ab6c08295}"
PRODUCT_ID="${PRODUCT_ID:-c76556a4-1944-4524-bb28-7585c30a5a77}"

PASS_COUNT=0
FAIL_COUNT=0
TOKEN=""

TEMP_DIR="$(mktemp -d)"
RESPONSE_FILE="$TEMP_DIR/response.json"

trap 'rm -rf "$TEMP_DIR"' EXIT

# --------------------------------------------------
# Utilities
# --------------------------------------------------

fail() {
    echo
    echo "FAILED: $1"

    if [[ -f "$RESPONSE_FILE" ]]; then
        echo "Response:"
        jq . "$RESPONSE_FILE" 2>/dev/null || cat "$RESPONSE_FILE"
    fi

    exit 1
}

pass() {
    echo "PASS: $1"
    PASS_COUNT=$((PASS_COUNT + 1))
}

api() {
    local method="$1"
    local path="$2"
    local expected="$3"
    local body="${4:-}"
    local actual

    if [[ -n "$TOKEN" ]]; then
        if [[ -n "$body" ]]; then
            actual=$(curl --silent --show-error \
                -X "$method" \
                "$BASE_URL$path" \
                -H "Authorization: Bearer $TOKEN" \
                -H "Content-Type: application/json" \
                --data "$body" \
                -o "$RESPONSE_FILE" \
                -w "%{http_code}")
        else
            actual=$(curl --silent --show-error \
                -X "$method" \
                "$BASE_URL$path" \
                -H "Authorization: Bearer $TOKEN" \
                -o "$RESPONSE_FILE" \
                -w "%{http_code}")
        fi
    else
        if [[ -n "$body" ]]; then
            actual=$(curl --silent --show-error \
                -X "$method" \
                "$BASE_URL$path" \
                -H "Content-Type: application/json" \
                --data "$body" \
                -o "$RESPONSE_FILE" \
                -w "%{http_code}")
        else
            actual=$(curl --silent --show-error \
                -X "$method" \
                "$BASE_URL$path" \
                -o "$RESPONSE_FILE" \
                -w "%{http_code}")
        fi
    fi

    if [[ "$expected" == "NON_2XX" ]]; then
        if (( actual >= 200 && actual < 300 )); then
            fail "$method $path — expected failure, received HTTP $actual"
        fi

        pass "$method $path — correctly rejected (HTTP $actual)"
    else
        if [[ "$actual" != "$expected" ]]; then
            fail "$method $path — expected HTTP $expected, received HTTP $actual"
        fi

        pass "$method $path — HTTP $actual"
    fi
}
assert_json() {
    local filter="$1"
    local description="$2"

    if ! jq -e "$filter" "$RESPONSE_FILE" >/dev/null; then
        fail "$description"
    fi

    pass "$description"
}

# --------------------------------------------------
# Prerequisites
# --------------------------------------------------

echo
echo "=========================================="
echo " HARDWARE SHOP ERP - API SMOKE TEST"
echo "=========================================="
echo

command -v curl >/dev/null || fail "curl is not installed"
command -v jq >/dev/null || fail "jq is not installed"

curl --silent --show-error --fail \
    "$BASE_URL/actuator/health" \
    -o "$RESPONSE_FILE" \
    || fail "Backend is not reachable at $BASE_URL"

assert_json '.status == "UP"' "Backend health is UP"

echo

read -r -p "Admin email: " ADMIN_EMAIL
read -r -s -p "Admin password: " ADMIN_PASSWORD
echo

# --------------------------------------------------
# 1. Authentication
# --------------------------------------------------

LOGIN_BODY=$(jq -n \
    --arg email "$ADMIN_EMAIL" \
    --arg password "$ADMIN_PASSWORD" \
    '{email: $email, password: $password}')

api POST "/api/v1/auth/login" 200 "$LOGIN_BODY"

TOKEN=$(jq -r '.data.accessToken // empty' "$RESPONSE_FILE")

[[ -n "$TOKEN" ]] || fail "Login succeeded but accessToken was missing"

unset ADMIN_PASSWORD LOGIN_BODY

pass "Admin authentication token obtained"

# --------------------------------------------------
# 2. Capture baseline inventory
# --------------------------------------------------

api GET "/api/v1/inventory/$PRODUCT_ID" 200

BASELINE=$(jq -r '.data.quantityOnHand' "$RESPONSE_FILE")

[[ "$BASELINE" != "null" && -n "$BASELINE" ]] \
    || fail "Could not read baseline inventory"

echo "Baseline inventory: $BASELINE"

# --------------------------------------------------
# 3. Create a fresh purchase order
# --------------------------------------------------

TODAY=$(date +%Y-%m-%d)

PO_BODY=$(jq -n \
    --arg supplierId "$SUPPLIER_ID" \
    --arg productId "$PRODUCT_ID" \
    --arg orderDate "$TODAY" \
    '{
        supplierId: $supplierId,
        orderDate: $orderDate,
        notes: "Automated smoke test purchase order",
        items: [
            {
                productId: $productId,
                orderedQuantity: 10,
                unitCost: 360
            }
        ]
    }')

api POST "/api/v1/purchase-orders" 201 "$PO_BODY"

PO_ID=$(jq -r '.data.id // empty' "$RESPONSE_FILE")
PO_NUMBER=$(jq -r '.data.poNumber // empty' "$RESPONSE_FILE")
PO_ITEM_ID=$(jq -r '.data.items[0].id // empty' "$RESPONSE_FILE")

[[ -n "$PO_ID" ]] || fail "Purchase order ID missing"
[[ -n "$PO_ITEM_ID" ]] || fail "Purchase order item ID missing"

assert_json '.data.status == "DRAFT"' "New PO has DRAFT status"
assert_json '.data.items[0].orderedQuantity == 10' "PO quantity is 10"

echo
echo "Created PO: $PO_NUMBER"
echo "PO ID: $PO_ID"
echo "PO item ID: $PO_ITEM_ID"

# --------------------------------------------------
# 4. Submit and approve
# --------------------------------------------------

api POST "/api/v1/purchase-orders/$PO_ID/submit" 200

assert_json '.data.status == "PENDING_APPROVAL"' \
    "PO submitted for approval"

api POST "/api/v1/purchase-orders/$PO_ID/approve" 200

assert_json '.data.status == "APPROVED"' \
    "PO approved"

# --------------------------------------------------
# 5. Approved PO must not be editable
# --------------------------------------------------

api PUT "/api/v1/purchase-orders/$PO_ID" NON_2XX "$PO_BODY"

# --------------------------------------------------
# 6. Over-receipt before first delivery
# --------------------------------------------------

RECEIPT_DATE=$(date +%Y-%m-%d)

OVER_RECEIPT_BODY=$(jq -n \
    --arg itemId "$PO_ITEM_ID" \
    --arg receiptDate "$RECEIPT_DATE" \
    '{
        receiptDate: $receiptDate,
        supplierDeliveryNote: "SMOKE-OVER-001",
        notes: "Intentional over-receipt test",
        items: [
            {
                purchaseOrderItemId: $itemId,
                receivedQuantity: 11
            }
        ]
    }')

api POST \
    "/api/v1/purchase-orders/$PO_ID/goods-receipts" \
    NON_2XX \
    "$OVER_RECEIPT_BODY"

# Verify rejected receipt did not change stock.

api GET "/api/v1/inventory/$PRODUCT_ID" 200

assert_json \
    ".data.quantityOnHand == ($BASELINE | tonumber)" \
    "Rejected over-receipt did not change inventory"

# --------------------------------------------------
# 7. First partial receipt: 6 BAG
# --------------------------------------------------

FIRST_RECEIPT_BODY=$(jq -n \
    --arg itemId "$PO_ITEM_ID" \
    --arg receiptDate "$RECEIPT_DATE" \
    '{
        receiptDate: $receiptDate,
        supplierDeliveryNote: "SMOKE-DELIVERY-001",
        notes: "First automated partial delivery",
        items: [
            {
                purchaseOrderItemId: $itemId,
                receivedQuantity: 6
            }
        ]
    }')

api POST \
    "/api/v1/purchase-orders/$PO_ID/goods-receipts" \
    201 \
    "$FIRST_RECEIPT_BODY"

FIRST_GRN=$(jq -r '.data.receiptNumber' "$RESPONSE_FILE")

pass "First GRN created: $FIRST_GRN"

api GET "/api/v1/inventory/$PRODUCT_ID" 200

EXPECTED_FIRST=$(jq -n \
    --arg baseline "$BASELINE" \
    '($baseline | tonumber) + 6')

assert_json \
    ".data.quantityOnHand == ($EXPECTED_FIRST | tonumber)" \
    "Inventory increased by 6 after first GRN"

api GET "/api/v1/purchase-orders/$PO_ID" 200

assert_json '.data.status == "PARTIALLY_RECEIVED"' \
    "PO status is PARTIALLY_RECEIVED"

assert_json '.data.items[0].receivedQuantity == 6' \
    "PO received quantity is 6"

assert_json '.data.items[0].outstandingQuantity == 4' \
    "PO outstanding quantity is 4"

# --------------------------------------------------
# 8. Attempt to receive more than outstanding
# --------------------------------------------------

SECOND_OVER_RECEIPT_BODY=$(jq -n \
    --arg itemId "$PO_ITEM_ID" \
    --arg receiptDate "$RECEIPT_DATE" \
    '{
        receiptDate: $receiptDate,
        supplierDeliveryNote: "SMOKE-OVER-002",
        notes: "Intentional outstanding quantity test",
        items: [
            {
                purchaseOrderItemId: $itemId,
                receivedQuantity: 5
            }
        ]
    }')

api POST \
    "/api/v1/purchase-orders/$PO_ID/goods-receipts" \
    NON_2XX \
    "$SECOND_OVER_RECEIPT_BODY"

api GET "/api/v1/inventory/$PRODUCT_ID" 200

assert_json \
    ".data.quantityOnHand == ($EXPECTED_FIRST | tonumber)" \
    "Second rejected over-receipt did not change inventory"

# --------------------------------------------------
# 9. Second partial receipt: remaining 4 BAG
# --------------------------------------------------

SECOND_RECEIPT_BODY=$(jq -n \
    --arg itemId "$PO_ITEM_ID" \
    --arg receiptDate "$RECEIPT_DATE" \
    '{
        receiptDate: $receiptDate,
        supplierDeliveryNote: "SMOKE-DELIVERY-002",
        notes: "Final automated delivery",
        items: [
            {
                purchaseOrderItemId: $itemId,
                receivedQuantity: 4
            }
        ]
    }')

api POST \
    "/api/v1/purchase-orders/$PO_ID/goods-receipts" \
    201 \
    "$SECOND_RECEIPT_BODY"

SECOND_GRN=$(jq -r '.data.receiptNumber' "$RESPONSE_FILE")

pass "Second GRN created: $SECOND_GRN"

# --------------------------------------------------
# 10. Verify final PO state
# --------------------------------------------------

api GET "/api/v1/purchase-orders/$PO_ID" 200

assert_json '.data.status == "RECEIVED"' \
    "PO status is RECEIVED"

assert_json '.data.items[0].receivedQuantity == 10' \
    "Total received quantity is 10"

assert_json '.data.items[0].outstandingQuantity == 0' \
    "Outstanding quantity is zero"

# --------------------------------------------------
# 11. Verify final inventory
# --------------------------------------------------

api GET "/api/v1/inventory/$PRODUCT_ID" 200

EXPECTED_FINAL=$(jq -n \
    --arg baseline "$BASELINE" \
    '($baseline | tonumber) + 10')

assert_json \
    ".data.quantityOnHand == ($EXPECTED_FINAL | tonumber)" \
    "Final inventory increased by exactly 10"

# --------------------------------------------------
# 12. Verify GRN history
# --------------------------------------------------

api GET \
    "/api/v1/purchase-orders/$PO_ID/goods-receipts" \
    200

assert_json \
    '(.data | length) == 2' \
    "Exactly two GRNs exist for this PO"

assert_json \
    '([.data[].items[].receivedQuantity] | add) == 10' \
    "GRN history totals 10 BAG"

# --------------------------------------------------
# Summary
# --------------------------------------------------

echo
echo "=========================================="
echo " SMOKE TEST COMPLETED SUCCESSFULLY"
echo "=========================================="
echo
echo "Purchase order : $PO_NUMBER"
echo "First GRN      : $FIRST_GRN"
echo "Second GRN     : $SECOND_GRN"
echo "PO ID          : $PO_ID"
echo "Baseline stock : $BASELINE"
echo "Final stock    : $EXPECTED_FINAL"
echo
echo "Assertions passed: $PASS_COUNT"
echo "Assertions failed: 0"
echo
