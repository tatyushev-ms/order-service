package com.efa.invoice;

import com.efa.address.Address;
import com.efa.order.Order;
import com.efa.order.OrderRepository;
import com.efa.order.OrderStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@AllArgsConstructor
public class SimpleInvoiceService implements InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    
    @Override
    public Invoice generate(String accountNumber, Set<String> orderIds, Address billingAddress) {
        final List<Order> foundOrders = orderRepository.findAllByIdIn(orderIds);
        //validate(foundOrders, orderIds, accountNumber, OrderStatus.CONFIRMED);
        if (foundOrders.size() != orderIds.size()) {
            final Set<String> foundIds = foundOrders.stream().map(Order::getId).collect(Collectors.toSet());
            final Set<String> nonexistentOrdersIds = orderIds.stream().filter(not(foundIds::contains)).collect(Collectors.toSet());
            throw new IllegalArgumentException("Illegal orders");// TODO: use nonexistentOrdersIds
        }
        for (final Order order : foundOrders) {
            final Set<String> withWrongAccountNumber = new TreeSet<>();
            final Set<String> notConfirmed = new TreeSet<>();
            if (!accountNumber.equals(order.getAccountNumber())) {
                withWrongAccountNumber.add(order.getId());
            } else if (!OrderStatus.CONFIRMED.equals(order.getOrderStatus())) {
                notConfirmed.add(order.getId());
            }
            if (!withWrongAccountNumber.isEmpty()) {
                throw new IllegalArgumentException("Not all orders belongs to the specified account");// TODO: use withWrongAccountNumber
            }
            if (!notConfirmed.isEmpty()) {
                throw new IllegalArgumentException("Not all orders are confirmed");// TODO: use notConfirmed; pass an actual status
            }
        }
        
        final Invoice invoice = new Invoice(accountNumber, InvoiceStatus.CREATED, billingAddress, new TreeSet<>(foundOrders));
        foundOrders.forEach(invoice::addOrder);
        return invoiceRepository.save(invoice);
    }
    
    @Override
    public Optional<Invoice> findById(String accountNumber, String id) {
        final Optional<Invoice> result = invoiceRepository.findByAccountNumberAndId(accountNumber, id);
        result.ifPresent(mustBelongTo(accountNumber));
        return result;//.orElseThrow(() -> new IllegalArgumentException("Illegal id")); not found
    }
    
    @Override
    public List<Invoice> findByAccountNumber(String accountNumber) {
        return null;
    }
    
    @Override
    public void updateInvoiceStatus(String accountNumber, String id, InvoiceStatus invoiceStatus) {
        final Optional<Invoice> byId = findById(accountNumber, id);
    }
    
    private Consumer<Invoice> mustBelongTo(String accountNumber) {
        return invoice -> {
            if (!accountNumber.equals(invoice.getAccountNumber())) {
                throw new IllegalArgumentException("Invoice does not belong to the specified account");
            }
        };
    }
    
}
