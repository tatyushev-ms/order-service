package com.efa.order;

import com.efa.address.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static com.efa.Utils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringJUnitConfig
@DataMongoTest
@DisplayName("OrderRepository tests")
class OrderRepositoryTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    @DisplayName("Saves an order")
    public void shouldSaveOrder() {
        //given
        final Address shippingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000");
        final LineItem lineItem = new LineItem("T-shirt", "SKU-12345", 1, bd("14.99"), bd("0.03"));
        Order order = new Order("112233", shippingAddress);
        order.addLineItem(lineItem);
        
        //when
        order = orderRepository.save(order);
        
        assertThat(order.getId(), is(notNullValue()));
    }
    
    @Test
    @DisplayName("A lastModified field is set")
    public void lastModifiedFieldIsSet() {
        //given
        final Address shippingAddress = new Address("100 Citizen Road", null, "NSW", "Blacktown", "AU", "2148");
        
        Order order = new Order("147852", shippingAddress);
        order.addLineItem(new LineItem("Women's Small T-shirt", "SKU-78946", 4, bd("11.99"), bd("0.04")));
        
        //when
        order = orderRepository.save(order);
        
        //then
        assertThat(order.getLastModified(), is(notNullValue()));
    }
    
    @Test
    @DisplayName("A createdAt field is set")
    public void createdAtFieldIsSet() {
        //given
        final Address shippingAddress = new Address("100 Citizen Road", null, "NSW", "Blacktown", "AU", "2148");
        
        Order order = new Order("147852", shippingAddress);
        order.addLineItem(new LineItem("Men's Medium T-shirt", "SKU-45623", 2, bd("12.99"), bd("0.05")));
        
        //when
        order = orderRepository.save(order);
        
        //then
        assertThat(order.getCreatedAt(), is(notNullValue()));
    }
    
    @Test
    @DisplayName("lastModified equals to createdAt after the first save")
    public void lastModifiedEqualsToCreatedAtAfterFirstSave() {
        //given
        final Address shippingAddress = new Address("100 Citizen Road", null, "NSW", "Blacktown", "AU", "2148");
        
        Order order = new Order("147852", shippingAddress);
        order.addLineItem(new LineItem("Women's Medium T-shirt", "SKU-79456", 3, bd("12.99"), bd("0.05")));
        
        //when
        order = orderRepository.save(order);
        
        //then
        assertThat(order.getLastModified(), is(equalTo(order.getCreatedAt())));
    }
    
    @Test
    @DisplayName("lastModified differs from createdAt after the update")
    public void lastModifiedDiffersFromCreatedAtAfterUpdate() {
        //given
        final Address shippingAddress = new Address("100 Citizen Road", null, "NSW", "Blacktown", "AU", "2148");
        
        Order order = new Order("147852", shippingAddress);
        order.addLineItem(new LineItem("Men's Large T-shirt", "SKU-46486", 1, bd("15.99"), bd("0.06")));
        
        //when
        order = orderRepository.save(order);
        
        //then
        assertThat(order.getLastModified(), is(equalTo(order.getCreatedAt())));
        
        order = orderRepository.save(order);
        assertThat(order.getLastModified(), is(not(equalTo(order.getCreatedAt()))));
    }
    
    @Test
    @DisplayName("Finds a user")
    public void findsUser() {
        //given
        final Address shippingAddress = new Address("1600 Pennsylvania Ave NW", null, "DC", "Washington", "US", "20500");
        
        final Order order = new Order("12345", shippingAddress);
        order.addLineItem(new LineItem("Women's Small T-shirt", "SKU-78946", 4, bd("11.99"), bd("0.04")));
        order.addLineItem(new LineItem("Men's Medium T-shirt", "SKU-45623", 2, bd("12.99"), bd("0.05")));
        order.addLineItem(new LineItem("Women's Medium T-shirt", "SKU-79456", 3, bd("12.99"), bd("0.05")));
        order.addLineItem(new LineItem("Men's Large T-shirt", "SKU-46486", 1, bd("15.99"), bd("0.06")));
        
        //when
        orderRepository.save(order);
        
        final List<Order> found = orderRepository.findByAccountNumber(order.getAccountNumber());
        
        //then
        assertThat(found, is(not(empty())));
        
        final Order foundOrder = found.get(0);
        
        assertThat(foundOrder.getAccountNumber(), is(equalTo("12345")));
        assertThat(foundOrder.getShippingAddress(), is(equalTo(shippingAddress)));
        assertThat(foundOrder.getLineItems(), hasSize(4));
        assertThat(foundOrder.getLineItems(), hasItem(new LineItem("Women's Small T-shirt", "SKU-78946", 4, bd("11.99"), bd("0.04"))));
    }
    
}
