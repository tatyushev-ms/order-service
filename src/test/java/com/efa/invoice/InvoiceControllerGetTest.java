package com.efa.invoice;

import com.efa.Utils;
import com.efa.WithBearerJwt;
import com.efa.address.Address;
import com.efa.address.AddressType;
import com.efa.order.LineItem;
import com.efa.order.Order;
import com.efa.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Optional;

import static com.efa.Utils.bd;
import static com.efa.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@DisplayName("Invoice retrieval tests")
class InvoiceControllerGetTest extends AbstractInvoiceControllerTest {
    
    private final Invoice wellKnownInvoice = new Invoice();
    
    @BeforeEach
    void setUp() {
        final Address billingAddress = new Address("100 Citizen Road", null, "NSW", "Blacktown", "AU", "2148");
        billingAddress.setAddressType(AddressType.BILLING);
        
        final Address shippingAddress = new Address("1600 Pennsylvania Ave NW", null, "DC", "Washington", "US", "20500");
        shippingAddress.setAddressType(AddressType.SHIPPING);
        
        final LineItem lineItem = new LineItem("Women's Small T-shirt", "SKU-78946", 4, bd("11.99"), bd("0.04"));
        
        final Order order = new Order();
        order.setId("F2005171607103863");
        order.setAccountNumber(wellKnownAccountNumber);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.addLineItem(lineItem);
        order.setCreatedAt(Utils.Time.now());
        order.setLastModified(Utils.Time.lastValue());
        
        wellKnownInvoice.setId("b65c6n325c92s63dt5df45h723dh6457");
        wellKnownInvoice.setAccountNumber(wellKnownAccountNumber);
        wellKnownInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
        wellKnownInvoice.setBillingAddress(billingAddress);
        wellKnownInvoice.addOrder(order);
        
        wellKnownInvoice.setCreatedAt(Utils.Time.now());
        wellKnownInvoice.setLastModified(Utils.Time.lastValue());
        
        given(invoiceService.findById(wellKnownAccountNumber, wellKnownInvoice.getId()))
                .willReturn(Optional.of(wellKnownInvoice));
    }
    
    @Test
    @DisplayName("Returns an invoice by an id")
    @WithBearerJwt(attributes = {
            @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
    })
    void shouldReturnInvoiceById() throws Exception {
        final ResultActions result = mockMvc.perform(
                get(rootPath + "/" + wellKnownInvoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(wellKnownInvoice.getId())))
                .andExpect(jsonPath("$.accountNumber", is(wellKnownInvoice.getAccountNumber())))
                .andExpect(jsonPath("$.invoiceStatus", is(enumValueEqualedTo(wellKnownInvoice.getInvoiceStatus()))))
                .andExpect(jsonPath("$.billingAddress", is(notNullValue())))
                .andExpect(jsonPath("$.billingAddress.addressType", is(enumValueEqualedTo(wellKnownInvoice.getBillingAddress().getAddressType()))))
                .andExpect(jsonPath("$.billingAddress.street1", is(wellKnownInvoice.getBillingAddress().getStreet1())))
                .andExpect(jsonPath("$.billingAddress.street2", is(wellKnownInvoice.getBillingAddress().getStreet2())))
                .andExpect(jsonPath("$.billingAddress.stateCode", is(wellKnownInvoice.getBillingAddress().getStateCode())))
                .andExpect(jsonPath("$.billingAddress.city", is(wellKnownInvoice.getBillingAddress().getCity())))
                .andExpect(jsonPath("$.billingAddress.countryCode", is(wellKnownInvoice.getBillingAddress().getCountryCode())))
                .andExpect(jsonPath("$.billingAddress.postcode", is(wellKnownInvoice.getBillingAddress().getPostcode())))
                .andExpect(jsonPath("$.orders", is(notNullValue())))
                .andExpect(jsonPath("$.orders", is(hasSize(1))));
        
        final Order firstOrder = wellKnownInvoice.getOrders().iterator().next();
        result
                .andExpect(jsonPath("$.orders[0].id", is(firstOrder.getId())))
                .andExpect(jsonPath("$.orders[0].accountNumber", is(firstOrder.getAccountNumber())))
                .andExpect(jsonPath("$.orders[0].orderStatus", is(enumValueEqualedTo(firstOrder.getOrderStatus()))))
                .andExpect(jsonPath("$.orders[0].shippingAddress", is(notNullValue())))
                .andExpect(jsonPath("$.orders[0].shippingAddress.addressType", is(enumValueEqualedTo(firstOrder.getShippingAddress().getAddressType()))))
                .andExpect(jsonPath("$.orders[0].shippingAddress.street1", is(firstOrder.getShippingAddress().getStreet1())))
                .andExpect(jsonPath("$.orders[0].shippingAddress.street2", is(firstOrder.getShippingAddress().getStreet2())))
                .andExpect(jsonPath("$.orders[0].shippingAddress.stateCode", is(firstOrder.getShippingAddress().getStateCode())))
                .andExpect(jsonPath("$.orders[0].shippingAddress.city", is(firstOrder.getShippingAddress().getCity())))
                .andExpect(jsonPath("$.orders[0].shippingAddress.countryCode", is(firstOrder.getShippingAddress().getCountryCode())))
                .andExpect(jsonPath("$.orders[0].shippingAddress.postcode", is(firstOrder.getShippingAddress().getPostcode())))
                .andExpect(jsonPath("$.orders[0].lineItems", is(notNullValue())))
                .andExpect(jsonPath("$.orders[0].lineItems", is(hasSize(1))));
        
        final LineItem firstOrderFirstLineItem = firstOrder.getLineItems().iterator().next();
        result
                .andExpect(jsonPath("$.orders[0].lineItems[0].name", is(firstOrderFirstLineItem.getName())))
                .andExpect(jsonPath("$.orders[0].lineItems[0].productId", is(firstOrderFirstLineItem.getProductId())))
                .andExpect(jsonPath("$.orders[0].lineItems[0].quantity", is(firstOrderFirstLineItem.getQuantity())))
                .andExpect(jsonPath("$.orders[0].lineItems[0].price", is(doubleWhichIs(closeTo(firstOrderFirstLineItem.getPrice(), new BigDecimal("0.001"))))))
                .andExpect(jsonPath("$.orders[0].lineItems[0].price", is(doubleWhichIs(closeTo(firstOrderFirstLineItem.getPrice(), new BigDecimal("0.001"))))))
                .andExpect(jsonPath("$.orders[0].createdAt", is(dateEqualedTo(firstOrder.getCreatedAt()))))
                .andExpect(jsonPath("$.orders[0].lastModified", is(dateEqualedTo(firstOrder.getLastModified()))))
                
                .andExpect(jsonPath("$.createdAt", is(dateEqualedTo(wellKnownInvoice.getCreatedAt()))))
                .andExpect(jsonPath("$.lastModified", is(dateEqualedTo(wellKnownInvoice.getLastModified()))));
    }
    
}
