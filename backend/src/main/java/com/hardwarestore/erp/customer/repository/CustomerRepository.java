package com.hardwarestore.erp.customer.repository;

import com.hardwarestore.erp.customer.entity.Customer;
import com.hardwarestore.erp.customer.entity.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByGstin(String gstin);

    boolean existsByGstinAndIdNot(String gstin, UUID id);

    @Query("""
            SELECT c FROM Customer c
            WHERE (:active IS NULL OR c.active = :active)
              AND (:customerType IS NULL OR c.customerType = :customerType)
              AND (
                  :search = ''
                  OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(c.gstin, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Customer> search(
            @Param("search") String search,
            @Param("active") Boolean active,
            @Param("customerType") CustomerType customerType,
            Pageable pageable
    );
}
