package com.efa.order;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface OrderRepository extends PagingAndSortingRepository<Order, String> {
    
    List<Order> findByAccountNumber(String accountNumber);
    
    List<Order> findAllByAccountNumberAndIdIn(String accountNumber, Collection<String> orderIds);
    List<Order> findAllByIdIn(Collection<String> orderIds);
    
}
