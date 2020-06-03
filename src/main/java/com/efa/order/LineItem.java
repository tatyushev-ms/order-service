package com.efa.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineItem {
    
    private String name;
    private String productId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal tax;
    
}
