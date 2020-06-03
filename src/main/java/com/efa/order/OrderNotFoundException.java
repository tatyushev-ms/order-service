package com.efa.order;

public class OrderNotFoundException extends RuntimeException {
    
    private final String id;
    
    public OrderNotFoundException(String id) {
        super("order-not-found-" + id);
        this.id = id;
    }
    
    public String getOrderId() {
        return id;
    }
    
}
