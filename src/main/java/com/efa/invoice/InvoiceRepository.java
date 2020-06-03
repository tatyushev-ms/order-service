package com.efa.invoice;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface InvoiceRepository extends PagingAndSortingRepository<Invoice, String> {
    
    //Invoice findByBillingAddress(Address address);
    Optional<Invoice> findByAccountNumberAndId(String accountNumber, String id);
    
}
