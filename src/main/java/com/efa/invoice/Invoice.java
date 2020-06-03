package com.efa.invoice;

import com.efa.address.Address;
import com.efa.address.AddressType;
import com.efa.data.BaseDocument;
import com.efa.order.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Invoice extends BaseDocument {
    
    private String accountNumber;
    
    private InvoiceStatus invoiceStatus;
    
    private Address billingAddress;
    
    private Set<Order> orders = new HashSet<>();
    
    public Invoice(String accountNumber, Address billingAddress) {
        this.accountNumber = accountNumber;
        this.invoiceStatus = InvoiceStatus.CREATED;
        this.billingAddress = billingAddress;
        this.billingAddress.setAddressType(AddressType.BILLING);
    }
    
    public void addOrder(Order order) {
        orders.add(order);
    }
    
}
