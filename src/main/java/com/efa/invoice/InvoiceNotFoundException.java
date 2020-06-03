package com.efa.invoice;

public class InvoiceNotFoundException extends RuntimeException {
    
    private final String id;
    
    public InvoiceNotFoundException(String id) {
        super("invoice-not-found-" + id);
        this.id = id;
    }
    
    public String getOrderId() {
        return id;
    }
    
}
