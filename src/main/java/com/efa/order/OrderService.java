package com.efa.order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    Order save(Order order);
    
    Optional<Order> findById(String id);
    
    List<Order> findByAccountNumber(String accountNumber);
    
    void updateOrderStatus(String id, OrderStatus orderStatus);
    
}
