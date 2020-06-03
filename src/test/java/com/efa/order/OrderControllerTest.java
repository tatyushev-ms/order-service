package com.efa.order;

import com.efa.Utils;
import com.efa.address.Address;
import com.efa.address.AddressType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Optional;

import static com.efa.Utils.bd;
import static com.efa.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig
@WebMvcTest(OrderController.class)
@DisplayName("OrderController tests")
class OrderControllerTest {
    
    @MockBean
    private OrderService orderService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final String rootPath = "/v1/orders";
    
    private final Order wellKnownOrder = new Order();
    
    @BeforeEach
    void setUp() {
        final Address shippingAddress = new Address("1600 Pennsylvania Ave NW", null, "DC", "Washington", "US", "20500");
        shippingAddress.setAddressType(AddressType.SHIPPING);
        
        final LineItem lineItem = new LineItem("Women's Small T-shirt", "SKU-78946", 4, bd("11.99"), bd("0.04"));
        
        wellKnownOrder.setId("F2005171607103863");
        wellKnownOrder.setAccountNumber("a857c0b84313490eb078fb5b4018ffff");
        wellKnownOrder.setOrderStatus(OrderStatus.PENDING);
        wellKnownOrder.setShippingAddress(shippingAddress);
        wellKnownOrder.addLineItem(lineItem);
        wellKnownOrder.setCreatedAt(Utils.Time.now());
        wellKnownOrder.setLastModified(Utils.Time.lastValue());
        
        given(orderService.findById(wellKnownOrder.getId()))
                .willReturn(Optional.of(wellKnownOrder));
    }
    
    @Test
    @DisplayName("Returns an order by an id")
    @WithMockUser
    void shouldReturnOrderById() throws Exception {
        final ResultActions result = mockMvc.perform(
                get(rootPath + "/" + wellKnownOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(wellKnownOrder.getId())))
                .andExpect(jsonPath("$.accountNumber", is(wellKnownOrder.getAccountNumber())))
                .andExpect(jsonPath("$.orderStatus", is(enumValueEqualedTo(wellKnownOrder.getOrderStatus()))))
                .andExpect(jsonPath("$.shippingAddress", is(notNullValue())))
                .andExpect(jsonPath("$.shippingAddress.addressType", is(enumValueEqualedTo(wellKnownOrder.getShippingAddress().getAddressType()))))
                .andExpect(jsonPath("$.shippingAddress.street1", is(wellKnownOrder.getShippingAddress().getStreet1())))
                .andExpect(jsonPath("$.shippingAddress.street2", is(wellKnownOrder.getShippingAddress().getStreet2())))
                .andExpect(jsonPath("$.shippingAddress.stateCode", is(wellKnownOrder.getShippingAddress().getStateCode())))
                .andExpect(jsonPath("$.shippingAddress.city", is(wellKnownOrder.getShippingAddress().getCity())))
                .andExpect(jsonPath("$.shippingAddress.countryCode", is(wellKnownOrder.getShippingAddress().getCountryCode())))
                .andExpect(jsonPath("$.shippingAddress.postcode", is(wellKnownOrder.getShippingAddress().getPostcode())))
                .andExpect(jsonPath("$.lineItems", is(notNullValue())))
                .andExpect(jsonPath("$.lineItems", is(hasSize(1))));
        
        final LineItem firstLineItem = wellKnownOrder.getLineItems().iterator().next();
        result
                .andExpect(jsonPath("$.lineItems[0].name", is(firstLineItem.getName())))
                .andExpect(jsonPath("$.lineItems[0].productId", is(firstLineItem.getProductId())))
                .andExpect(jsonPath("$.lineItems[0].quantity", is(firstLineItem.getQuantity())))
                .andExpect(jsonPath("$.lineItems[0].price", is(doubleWhichIs(closeTo(firstLineItem.getPrice(), new BigDecimal("0.001"))))))
                .andExpect(jsonPath("$.lineItems[0].price", is(doubleWhichIs(closeTo(firstLineItem.getPrice(), new BigDecimal("0.001"))))))
                .andExpect(jsonPath("$.createdAt", is(dateEqualedTo(wellKnownOrder.getCreatedAt()))))
                .andExpect(jsonPath("$.lastModified", is(dateEqualedTo(wellKnownOrder.getLastModified()))));
    }
    
    @Test
    @DisplayName("Places an order")
    @WithMockUser
    void shouldPlaceOrder() throws Exception {
        final Address shippingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000");
        final LineItem lineItem = new LineItem("T-shirt", "SKU-12345", 1, bd("14.99"), bd("0.03"));
        final Order order = new Order("112233", shippingAddress);
        order.addLineItem(lineItem);
        
        final Address savedShippingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000");
        final LineItem savedLineItem = new LineItem("T-shirt", "SKU-12345", 1, bd("14.99"), bd("0.03"));
        final Order savedOrder = new Order("112233", savedShippingAddress);
        savedOrder.setId("1");
        savedOrder.setOrderStatus(OrderStatus.PENDING);
        savedOrder.addLineItem(savedLineItem);
        savedOrder.setCreatedAt(Utils.Time.now());
        savedOrder.setLastModified(Utils.Time.lastValue());
        
        given(orderService.save(order)).willReturn(savedOrder);
        
        final String orderJSON = objectMapper.writeValueAsString(order);
        
        mockMvc.perform(
                post(rootPath)
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(header().string("Location", startsWith("http://")))
                .andExpect(header().string("Location", endsWith(rootPath + "/" + savedOrder.getId())));
    }
    
}
