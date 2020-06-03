package com.efa.invoice;

import com.efa.address.Address;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InvoiceService {
    
    Invoice generate(String accountNumber, Set<String> orderIds, Address billingAddress);
    
    Optional<Invoice> findById(String accountNumber, String id);
    
    List<Invoice> findByAccountNumber(String accountNumber);
    
    void updateInvoiceStatus(String accountNumber, String id, InvoiceStatus invoiceStatus);
    
}
