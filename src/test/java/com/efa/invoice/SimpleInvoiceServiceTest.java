package com.efa.invoice;

import com.efa.Utils;
import com.efa.address.Address;
import com.efa.address.AddressType;
import com.efa.order.LineItem;
import com.efa.order.Order;
import com.efa.order.OrderRepository;
import com.efa.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.efa.Utils.bd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.only;

@ExtendWith(MockitoExtension.class)
@DisplayName("SimpleInvoiceService tests")
class SimpleInvoiceServiceTest {
    
    @Mock
    private InvoiceRepository invoiceRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private SimpleInvoiceService simpleInvoiceService;
    
    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;
    
    protected final String wellKnownAccountNumber = "hj4g2rkh3gr2h3gh56hj8j23j4hgj9fs";
    
    @Nested
    @DisplayName("Invoice generation tests")
    class InvoiceGeneration {
        
        @Test
        @DisplayName("Generates an invoice")
        void shouldGenerateInvoice() {
            //given
            final List<Order> orderList = savedConfirmedOrders();
            final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            final Set<String> orderIds = orderList.stream().map(Order::getId).collect(Collectors.toSet());
            given(orderRepository.findAllByIdIn(orderIds)).willReturn(orderList);
            
            //when
            simpleInvoiceService.generate(wellKnownAccountNumber, orderIds, billingAddress);
            
            //then
            then(invoiceRepository).should(only()).save(invoiceCaptor.capture());
            final Invoice invoice = invoiceCaptor.getValue();
            assertThat(invoice.getId(), is(nullValue()));
            assertThat(invoice.getAccountNumber(), is(equalTo(wellKnownAccountNumber)));
            assertThat(invoice.getInvoiceStatus(), is(equalTo(InvoiceStatus.CREATED)));
            assertThat(invoice.getBillingAddress(), is(equalTo(billingAddress)));
            assertThat(invoice.getOrders(), hasSize(orderIds.size()));
            assertThat(invoice.getOrders(), containsInAnyOrder(orderList.toArray()));
            assertThat(invoice.getCreatedAt(), is(nullValue()));
            assertThat(invoice.getLastModified(), is(nullValue()));
        }
        
        private List<Order> savedConfirmedOrders() {
            final Address savedShippingAddress1 = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.SHIPPING);
            final LineItem savedLineItem1 = new LineItem("T-shirt", "SKU-12345", 1, bd("14.99"), bd("0.03"));
            final Order savedOrder1 = new Order(wellKnownAccountNumber, savedShippingAddress1);
            savedOrder1.setId("1");
            savedOrder1.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder1.addLineItem(savedLineItem1);
            savedOrder1.setCreatedAt(Utils.Time.now());
            savedOrder1.setLastModified(Utils.Time.lastValue());
            
            final Address savedShippingAddress2 = new Address("1600 Pennsylvania Ave NW", null, "DC", "Washington", "US", "20500", AddressType.SHIPPING);
            final LineItem savedLineItem2 = new LineItem("Women's Small T-shirt", "SKU-78946", 4, bd("11.99"), bd("0.04"));
            final Order savedOrder2 = new Order(wellKnownAccountNumber, savedShippingAddress2);
            savedOrder2.setId("2");
            savedOrder2.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder2.setShippingAddress(savedShippingAddress2);
            savedOrder2.addLineItem(savedLineItem2);
            savedOrder2.setCreatedAt(Utils.Time.now());
            savedOrder2.setLastModified(Utils.Time.lastValue());
            
            return Arrays.asList(savedOrder1, savedOrder2);
        }
        
    }
    
}
