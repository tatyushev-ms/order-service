package com.efa.order;

import com.efa.address.Address;
import com.efa.address.AddressType;
import com.efa.data.BaseDocument;
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
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseDocument {
    
    private String accountNumber;
    
    private OrderStatus orderStatus;
    
    private Address shippingAddress;
    
    private Set<LineItem> lineItems = new HashSet<>();
    
    public Order(String accountNumber, Address shippingAddress) {
        this.accountNumber = accountNumber;
        this.orderStatus = OrderStatus.PENDING;
        this.shippingAddress = shippingAddress;
        this.shippingAddress.setAddressType(AddressType.SHIPPING);
    }
    
    public void addLineItem(LineItem lineItem) {
        lineItems.add(lineItem);
    }
    
}
