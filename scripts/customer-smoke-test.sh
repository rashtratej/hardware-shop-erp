#!/usr/bin/env bash
set -Eeuo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASS_COUNT=0
TOKEN=""
TEMP_DIR="$(mktemp -d)"
RESPONSE_FILE="$TEMP_DIR/response.json"

trap 'rm -rf "$TEMP_DIR"' EXIT

fail() {
    echo
    echo "FAILED: $1"
    if [[ -f "$RESPONSE_FILE" ]]; then
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

    if [[ -n "$TOKEN" && -n "$body" ]]; then
        actual=$(curl --silent --show-error \
            -X "$method" "$BASE_URL$path" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            --data "$body" \
            -o "$RESPONSE_FILE" -w "%{http_code}")
    elif [[ -n "$TOKEN" ]]; then
        actual=$(curl --silent --show-error \
            -X "$method" "$BASE_URL$path" \
            -H "Authorization: Bearer $TOKEN" \
            -o "$RESPONSE_FILE" -w "%{http_code}")
    elif [[ -n "$body" ]]; then
        actual=$(curl --silent --show-error \
            -X "$method" "$BASE_URL$path" \
            -H "Content-Type: application/json" \
            --data "$body" \
            -o "$RESPONSE_FILE" -w "%{http_code}")
    else
        actual=$(curl --silent --show-error \
            -X "$method" "$BASE_URL$path" \
            -o "$RESPONSE_FILE" -w "%{http_code}")
    fi

    if [[ "$actual" != "$expected" ]]; then
        fail "$method $path — expected HTTP $expected, received HTTP $actual"
    fi

    pass "$method $path — HTTP $actual"
}

assert_json() {
    local filter="$1"
    local description="$2"

    jq -e "$filter" "$RESPONSE_FILE" >/dev/null \
        || fail "$description"

    pass "$description"
}

echo
echo "=========================================="
echo " HARDWARE SHOP ERP - CUSTOMER API TEST"
echo "=========================================="
echo

command -v curl >/dev/null || fail "curl is not installed"
command -v jq >/dev/null || fail "jq is not installed"

curl --silent --show-error --fail \
    "$BASE_URL/actuator/health" \
    -o "$RESPONSE_FILE" \
    || fail "Backend is not reachable"

assert_json '.status == "UP"' "Backend health is UP"

read -r -p "Admin email: " ADMIN_EMAIL
read -r -s -p "Admin password: " ADMIN_PASSWORD
echo

LOGIN_BODY=$(jq -n \
    --arg email "$ADMIN_EMAIL" \
    --arg password "$ADMIN_PASSWORD" \
    '{email:$email,password:$password}')

api POST "/api/v1/auth/login" 200 "$LOGIN_BODY"

TOKEN=$(jq -r '.data.accessToken // empty' "$RESPONSE_FILE")
[[ -n "$TOKEN" ]] || fail "Login token missing"

unset ADMIN_PASSWORD LOGIN_BODY
pass "Admin login successful"

# Generate a syntactically valid, run-specific GSTIN.
RUN_ID="$(date +%s)"
PAN_DIGITS="$(printf '%04d' $((RUN_ID % 10000)))"
GSTIN="27ABCDE${PAN_DIGITS}F1Z5"

CREATE_BODY=$(jq -n \
    --arg name "Customer Smoke Test $RUN_ID" \
    --arg gstin "$GSTIN" \
    '{
        name:$name,
        customerType:"BUSINESS",
        gstin:$gstin,
        contactPerson:"Smoke Test Contact",
        phone:"9876543210",
        email:"customer-smoke@example.com",
        billingAddressLine1:"12 Market Road",
        billingCity:"Kolkata",
        billingState:"West Bengal",
        billingStateCode:"19",
        billingPostalCode:"700001",
        shippingAddressLine1:"Warehouse Lane",
        shippingCity:"Kolkata",
        shippingState:"West Bengal",
        shippingStateCode:"19",
        shippingPostalCode:"700002",
        notes:"Automated customer API smoke test"
    }')

# 1. Create customer
api POST "/api/v1/customers" 201 "$CREATE_BODY"

CUSTOMER_ID="$(jq -r '.data.id // empty' "$RESPONSE_FILE")"
[[ -n "$CUSTOMER_ID" ]] || fail "Customer ID missing"

assert_json '.data.customerType == "BUSINESS"' "Customer type is BUSINESS"
assert_json '.data.gstin != null' "GSTIN is returned"
assert_json '.data.active == true' "New customer is active"

echo "Created customer ID: $CUSTOMER_ID"

# 2. Get customer
api GET "/api/v1/customers/$CUSTOMER_ID" 200
assert_json '.data.id != null' "Get customer returns data"

# 3. Search customer by name
api GET "/api/v1/customers?search=Customer%20Smoke%20Test" 200
assert_json '.data.content | length > 0' "Customer search returns results"

# 4. Filter by type
api GET "/api/v1/customers?customerType=BUSINESS" 200
assert_json '.data.content | type == "array"' "Customer type filter returns page"

# 5. Duplicate GSTIN should conflict
api POST "/api/v1/customers" 409 "$CREATE_BODY"

# 6. Invalid customer request should be rejected
INVALID_BODY='{"name":"","customerType":"BUSINESS","phone":"bad"}'
api POST "/api/v1/customers" 400 "$INVALID_BODY"

# 7. Update customer
UPDATE_BODY=$(jq -n \
    --arg name "Updated Customer Smoke Test $RUN_ID" \
    --arg gstin "$GSTIN" \
    '{name:$name,customerType:"BUSINESS",gstin:$gstin,phone:"9876543210"}')

api PUT "/api/v1/customers/$CUSTOMER_ID" 200 "$UPDATE_BODY"
assert_json '.data.name | startswith("Updated Customer")' "Customer update applied"

# 8. Deactivate
api DELETE "/api/v1/customers/$CUSTOMER_ID" 200
assert_json '.data.active == false' "Customer deactivated"

# 9. Activate
api PATCH "/api/v1/customers/$CUSTOMER_ID/activate" 200
assert_json '.data.active == true' "Customer activated"

echo
echo "=========================================="
echo " CUSTOMER TEST COMPLETE"
echo " Passed: $PASS_COUNT"
echo "=========================================="
