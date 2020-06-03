package com.efa.order;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SimpleOrderService implements OrderService {
    
    @Override
    public Order save(Order order) {
        return null;
    }
    
    @Override
    public Optional<Order> findById(String id) {
        return Optional.empty();
    }
    
    @Override
    public List<Order> findByAccountNumber(String accountNumber) {
        return null;
    }
    
    @Override
    public void updateOrderStatus(String id, OrderStatus orderStatus) {
    
    }
    
}
