package com.efa.order;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/v1/orders")
@AllArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> options() {
        return ResponseEntity.ok()
                .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.PUT, HttpMethod.DELETE)
                .build();
    }
    
    @PostMapping
    public ResponseEntity<Order> post(@RequestBody Order order) {
        final Order savedOrder = orderService.save(order);
        
        final URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/{id}")
                .buildAndExpand(savedOrder.getId()).toUri();
        
        return ResponseEntity.created(uri).body(savedOrder);
    }
    
    @GetMapping
    public ResponseEntity<Collection<Order>> getCollection() {
        return null;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable String id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Order> patch(@PathVariable String id, @RequestBody Order order) {
        final OrderStatus orderStatus = order.getOrderStatus();
        orderService.updateOrderStatus(id, orderStatus);
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
    
}
