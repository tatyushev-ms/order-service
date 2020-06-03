package com.efa.invoice;

import com.efa.Utils;
import com.efa.WithBearerJwt;
import com.efa.address.Address;
import com.efa.address.AddressType;
import com.efa.order.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@DisplayName("Invoice placement tests")
class InvoiceControllerPostTest extends AbstractInvoiceControllerTest {
    
    private String content;
    private ResultActions result;
    
    @Test
    @DisplayName("post with HAL")
    @WithBearerJwt(attributes = {
            @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
    })
    void shouldReturnHal() throws Exception {
        //given
        requestHasBody("{\"billingAddress\":{" +
                "\"addressType\":\"BILLING\",\"street1\":\"Bennelong Point\",\"stateCode\":\"NSW\"," +
                "\"city\":\"Sydney\",\"countryCode\":\"AU\",\"postcode\":\"2000\"}" +
                ",\"orderIds\":[\"12u3\",\"sad2\"]}");
        
        final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
        
        final String generatedInvoiceId = "1";
        final Address generatedBillingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
        final Invoice generatedInvoice = new Invoice(wellKnownAccountNumber, generatedBillingAddress);
        generatedInvoice.setId(generatedInvoiceId);
        generatedInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
        generatedInvoice.setOrders(setOf(orderWithId("12u3"), orderWithId("sad2")));
        generatedInvoice.setCreatedAt(Utils.Time.now());
        generatedInvoice.setLastModified(Utils.Time.lastValue());
        
        given(invoiceService.generate(wellKnownAccountNumber, setOf("12u3", "sad2"), billingAddress))
                .willReturn(generatedInvoice);
        
        //when
        result = mockMvc.perform(
                post(rootPath)
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_FORMS_JSON)
                        .content(content));
        
        //then
        getResult()
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_FORMS_JSON))
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(header().string("Location", startsWith("http://")))
                .andExpect(header().string("Location", endsWith(rootPath + "/" + generatedInvoiceId)))
                .andExpect(jsonPath("$._links", notNullValue()))
                .andExpect(jsonPath("$._links.self", notNullValue()))
                .andExpect(jsonPath("$._links.self", aMapWithSize(1)))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.self.href", startsWith("http://")))
                .andExpect(jsonPath("$._links.self.href", endsWith(rootPath + "/" + generatedInvoiceId)))
                
                .andExpect(header().string("Location", startsWith("---STUB---")));
    }
    
    private Order orderWithId(String id) {
        final Order result = new Order();
        result.setId(id);
        return result;
    }
    
    @Test
    @DisplayName("Places an invoice")
    @WithBearerJwt(attributes = {
            @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
    })
    void shouldPlaceInvoice() throws Exception {
        //given
        requestHasBody("{\"billingAddress\":{" +
                "\"addressType\":\"BILLING\",\"street1\":\"Bennelong Point\",\"stateCode\":\"NSW\"," +
                "\"city\":\"Sydney\",\"countryCode\":\"AU\",\"postcode\":\"2000\"}" +
                ",\"orderIds\":[\"12u3\",\"sad2\"]}");
        
        final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
        
        final String generatedInvoiceId = "1";
        final Address generatedBillingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
        final Invoice generatedInvoice = new Invoice(wellKnownAccountNumber, generatedBillingAddress);
        generatedInvoice.setId(generatedInvoiceId);
        generatedInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
        
        given(invoiceService.generate(wellKnownAccountNumber, setOf("12u3", "sad2"), billingAddress))
                .willReturn(generatedInvoice);
        
        //when
        executeRequest();
        
        //then
        getResult()
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(header().string("Location", startsWith("http://")))
                .andExpect(header().string("Location", endsWith(rootPath + "/" + generatedInvoiceId)));
    }
    
    @Test
    @DisplayName("Requires the presence of \"billingAddress\"")
    @WithBearerJwt(attributes = {
            @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
    })
    void shouldRequirePresenceOfBillingAddress() throws Exception {
        //given
        requestHasBody("{\"orderIds\":[\"12u3\",\"sad2\"]}");
        
        //when
        executeRequest();
        
        //then
        getResult()
                .andExpect(invoiceGenerationInvalidParamsProblem())
                .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress")))
                .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be null")));
    }
    
    @Nested
    @DisplayName("Validates \"billingAddress.street1\"")
    class Street1Validation {
        
        @Test
        @DisplayName("Requires presence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequirePresence() throws Exception {
            //given
            requestHasBodyWhereStreet1Is(absent());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.street1")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Requires not to be empty")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWhereStreet1Is(empty());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(2)))
                    .andExpect(jsonPath("$.invalidParams[*].name", everyItem(is("billingAddress.street1"))))
                    .andExpect(jsonPath("$.invalidParams[*].reason", hasItems("must not be empty", "length must be between 1 and 255")));
        }
        
        @Test
        @DisplayName("Checks max length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMaxLength() throws Exception {
            //given
            final String tooLongStreet1 = "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf" +
                    "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf" +
                    "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf";
            requestHasBodyWhereStreet1Is(tooLongStreet1);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.street1")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("length must be between 1 and 255")));
        }
        
        private void requestHasBodyWhereStreet1Is(String street1Value) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            content.append("\"addressType\":\"BILLING\"");
            if (street1Value != null) {
                content.append(",\"street1\":\"").append(street1Value).append("\"");
            }
            content.append(",\"stateCode\":\"NSW\"");
            content.append(",\"city\":\"Sydney\"");
            content.append(",\"countryCode\":\"AU\"");
            content.append(",\"postcode\":\"2000\"");
            content.append("}");
            content.append(",\"orderIds\":[\"12u3\",\"sad2\"]");
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @Nested
    @DisplayName("Validates \"billingAddress.street2\"")
    class Street2Validation {
        
        @Test
        @DisplayName("Allow absence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldAllowAbsence() throws Exception {
            //given
            requestHasBodyWhereStreet2Is(absent());
            
            final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            
            final String generatedInvoiceId = "1";
            final Address generatedBillingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            final Invoice generatedInvoice = new Invoice(wellKnownAccountNumber, generatedBillingAddress);
            generatedInvoice.setId(generatedInvoiceId);
            generatedInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
            
            given(invoiceService.generate(wellKnownAccountNumber, setOf("12u3", "sad2"), billingAddress))
                    .willReturn(generatedInvoice);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Location", notNullValue()))
                    .andExpect(header().string("Location", startsWith("http://")))
                    .andExpect(header().string("Location", endsWith(rootPath + "/" + generatedInvoiceId)));
        }
        
        @Test
        @DisplayName("Requires not to be empty")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWhereStreet2Is(empty());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.street2")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Checks max length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMaxLength() throws Exception {
            //given
            final String tooLongStreet2 = "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf" +
                    "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf" +
                    "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf";
            requestHasBodyWhereStreet2Is(tooLongStreet2);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.street2")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("length must be between 1 and 255")));
        }
        
        private void requestHasBodyWhereStreet2Is(String street2Value) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            content.append("\"addressType\":\"BILLING\"");
            content.append(",\"street1\":\"Bennelong Point\"");
            if (street2Value != null) {
                content.append(",\"street2\":\"").append(street2Value).append("\"");
            }
            content.append(",\"stateCode\":\"NSW\"");
            content.append(",\"city\":\"Sydney\"");
            content.append(",\"countryCode\":\"AU\"");
            content.append(",\"postcode\":\"2000\"");
            content.append("}");
            content.append(",\"orderIds\":[\"12u3\",\"sad2\"]");
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @Nested
    @DisplayName("Validates \"billingAddress.stateCode\"")
    class StateCodeValidation {
        
        @Test
        @DisplayName("Requires presence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequirePresence() throws Exception {
            //given
            requestHasBodyWhereStateCodeIs(absent());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.stateCode")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Requires not to be empty")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWhereStateCodeIs(empty());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(2)))
                    .andExpect(jsonPath("$.invalidParams[*].name", everyItem(is("billingAddress.stateCode"))))
                    .andExpect(jsonPath("$.invalidParams[*].reason", hasItems("must not be empty", "length must be between 1 and 3")));
        }
        
        @Test
        @DisplayName("Checks max length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMaxLength() throws Exception {
            //given
            final String tooLongStateCode = "h6ad";
            requestHasBodyWhereStateCodeIs(tooLongStateCode);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.stateCode")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("length must be between 1 and 3")));
        }
        
        private void requestHasBodyWhereStateCodeIs(String stateCodeValue) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            content.append("\"addressType\":\"BILLING\"");
            content.append(",\"street1\":\"Bennelong Point\"");
            if (stateCodeValue != null) {
                content.append(",\"stateCode\":\"").append(stateCodeValue).append("\"");
            }
            content.append(",\"city\":\"Sydney\"");
            content.append(",\"countryCode\":\"AU\"");
            content.append(",\"postcode\":\"2000\"");
            content.append("}");
            content.append(",\"orderIds\":[\"12u3\",\"sad2\"]");
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @Nested
    @DisplayName("Validates \"billingAddress.city\"")
    class CityValidation {
        
        @Test
        @DisplayName("Requires presence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequirePresence() throws Exception {
            //given
            requestHasBodyWhereCityIs(absent());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.city")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Requires not to be empty")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWhereCityIs(empty());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(2)))
                    .andExpect(jsonPath("$.invalidParams[*].name", everyItem(is("billingAddress.city"))))
                    .andExpect(jsonPath("$.invalidParams[*].reason", hasItems("must not be empty", "length must be between 1 and 255")));
        }
        
        @Test
        @DisplayName("Checks max length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMaxLength() throws Exception {
            //given
            final String tooLongCity = "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf" +
                    "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf" +
                    "sad3f hja4sd klh2fla sd7on d2c34y234gc32kg das86fa sw9d7f n6as9d87f6n asd97f6as9 d7nf98s d67fa89ns d6fas d76nf";
            requestHasBodyWhereCityIs(tooLongCity);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.city")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("length must be between 1 and 255")));
        }
        
        private void requestHasBodyWhereCityIs(String cityValue) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            content.append("\"addressType\":\"BILLING\"");
            content.append(",\"street1\":\"Bennelong Point\"");
            content.append(",\"stateCode\":\"NSW\"");
            if (cityValue != null) {
                content.append(",\"city\":\"").append(cityValue).append("\"");
            }
            content.append(",\"countryCode\":\"AU\"");
            content.append(",\"postcode\":\"2000\"");
            content.append("}");
            content.append(",\"orderIds\":[\"12u3\",\"sad2\"]");
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @Nested
    @DisplayName("Validates \"billingAddress.countryCode\"")
    class CountryCodeValidation {
        
        @Test
        @DisplayName("Requires presence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequirePresence() throws Exception {
            //given
            requestHasBodyWhereCountryCodeIs(absent());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.countryCode")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Requires not to be empty")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWhereCountryCodeIs(empty());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(2)))
                    .andExpect(jsonPath("$.invalidParams[*].name", everyItem(is("billingAddress.countryCode"))))
                    .andExpect(jsonPath("$.invalidParams[*].reason", hasItems("must not be empty", "length must be 2")));
        }
        
        @Test
        @DisplayName("Checks min length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMinLength() throws Exception {
            //given
            final String tooLongCountryCode = "t";
            requestHasBodyWhereCountryCodeIs(tooLongCountryCode);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.countryCode")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("length must be 2")));
        }
        
        @Test
        @DisplayName("Checks max length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMaxLength() throws Exception {
            //given
            final String tooLongCountryCode = "saf";
            requestHasBodyWhereCountryCodeIs(tooLongCountryCode);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.countryCode")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("length must be 2")));
        }
        
        private void requestHasBodyWhereCountryCodeIs(String countryCodeValue) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            content.append("\"addressType\":\"BILLING\"");
            content.append(",\"street1\":\"Bennelong Point\"");
            content.append(",\"stateCode\":\"NSW\"");
            content.append(",\"city\":\"Sydney\"");
            if (countryCodeValue != null) {
                content.append(",\"countryCode\":\"").append(countryCodeValue).append("\"");
            }
            content.append(",\"postcode\":\"2000\"");
            content.append("}");
            content.append(",\"orderIds\":[\"12u3\",\"sad2\"]");
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @Nested
    @DisplayName("Validates \"billingAddress.postcode\"")
    class PostcodeValidation {
        
        @Test
        @DisplayName("Requires presence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequirePresence() throws Exception {
            //given
            requestHasBodyWherePostcodeIs(absent());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.postcode")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Requires not to be empty")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWherePostcodeIs(empty());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(2)))
                    .andExpect(jsonPath("$.invalidParams[*].name", everyItem(is("billingAddress.postcode"))))
                    .andExpect(jsonPath("$.invalidParams[*].reason", hasItems("must not be empty", "length must be between 1 and 20")));
        }
        
        @Test
        @DisplayName("Checks max length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMaxLength() throws Exception {
            //given
            final String tooLongPostcode = "klh2fla sd7on d2c34y2";
            requestHasBodyWherePostcodeIs(tooLongPostcode);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.postcode")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("length must be between 1 and 20")));
        }
        
        @Test
        @DisplayName("Allow an integer value")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldAllowIntegerValue() throws Exception {
            //given
            requestHasBodyWherePostcodeIs(381);
            
            final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "381", AddressType.BILLING);
            
            final String generatedInvoiceId = "1";
            final Address generatedBillingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "381", AddressType.BILLING);
            final Invoice generatedInvoice = new Invoice(wellKnownAccountNumber, generatedBillingAddress);
            generatedInvoice.setId(generatedInvoiceId);
            generatedInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
            
            given(invoiceService.generate(wellKnownAccountNumber, setOf("12u3", "sad2"), billingAddress))
                    .willReturn(generatedInvoice);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Location", notNullValue()))
                    .andExpect(header().string("Location", startsWith("http://")))
                    .andExpect(header().string("Location", endsWith(rootPath + "/" + generatedInvoiceId)));
        }
        
        private void requestHasBodyWherePostcodeIs(String postcodeValue) {
            requestHasBodyWherePostcodeIs(postcodeValue == null ? null : () -> "\"" + postcodeValue + "\"");
        }
        
        private void requestHasBodyWherePostcodeIs(@SuppressWarnings("SameParameterValue") int postcodeValue) {
            requestHasBodyWherePostcodeIs(() -> String.valueOf(postcodeValue));
        }
        
        private void requestHasBodyWherePostcodeIs(Supplier<String> postcodeValue) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            content.append("\"addressType\":\"BILLING\"");
            content.append(",\"street1\":\"Bennelong Point\"");
            content.append(",\"stateCode\":\"NSW\"");
            content.append(",\"city\":\"Sydney\"");
            content.append(",\"countryCode\":\"AU\"");
            if (postcodeValue != null) {
                content.append(",\"postcode\":").append(postcodeValue.get());
            }
            content.append("}");
            content.append(",\"orderIds\":[\"12u3\",\"sad2\"]");
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @Nested
    @DisplayName("Validates \"billingAddress.addressType\"")
    class AddressTypeValidation {
        
        @Test
        @DisplayName("Allow absence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldAllowAbsence() throws Exception {
            //given
            requestHasBodyWhereAddressTypeIs(absent());
            
            final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            
            final String generatedInvoiceId = "1";
            final Address generatedBillingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            final Invoice generatedInvoice = new Invoice(wellKnownAccountNumber, generatedBillingAddress);
            generatedInvoice.setId(generatedInvoiceId);
            generatedInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
            
            given(invoiceService.generate(wellKnownAccountNumber, setOf("12u3", "sad2"), billingAddress))
                    .willReturn(generatedInvoice);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Location", notNullValue()))
                    .andExpect(header().string("Location", startsWith("http://")))
                    .andExpect(header().string("Location", endsWith(rootPath + "/" + generatedInvoiceId)));
        }
        
        @Test
        @DisplayName("Requires not to be empty (if present)")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWhereAddressTypeIs(empty());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type", is("https://example.net/validation-error")))
                    .andExpect(jsonPath("$.title", is("Your request parameters didn't validate.")))
                    .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.invalidParams", hasSize(2)))
                    .andExpect(jsonPath("$.invalidParams[*].name", everyItem(is("billingAddress.addressType"))))
                    .andExpect(jsonPath("$.invalidParams[*].reason", hasItems("if present, must not be empty", "if present, must be BILLING")));
        }
        
        @Test
        @DisplayName("Allow BILLING")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldAllowBilling() throws Exception {
            //given
            requestHasBodyWhereAddressTypeIs("BILLING");
            
            final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            
            final String generatedInvoiceId = "1";
            final Address generatedBillingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            final Invoice generatedInvoice = new Invoice(wellKnownAccountNumber, generatedBillingAddress);
            generatedInvoice.setId(generatedInvoiceId);
            generatedInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
            
            given(invoiceService.generate(wellKnownAccountNumber, setOf("12u3", "sad2"), billingAddress))
                    .willReturn(generatedInvoice);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Location", notNullValue()))
                    .andExpect(header().string("Location", startsWith("http://")))
                    .andExpect(header().string("Location", endsWith(rootPath + "/" + generatedInvoiceId)));
        }
        
        @Test
        @DisplayName("Does not allow SHIPPING")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldNotAllowShipping() throws Exception {
            //given
            requestHasBodyWhereAddressTypeIs("SHIPPING");
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type", is("https://example.net/validation-error")))
                    .andExpect(jsonPath("$.title", is("Your request parameters didn't validate.")))
                    .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.addressType")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("if present, must be BILLING")));
        }
        
        @Test
        @DisplayName("Does not allow an illegal value")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldNotAllowIllegalValue() throws Exception {
            //given
            requestHasBodyWhereAddressTypeIs("RECIPIENT");
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type", is("https://example.net/validation-error")))
                    .andExpect(jsonPath("$.title", is("Your request parameters didn't validate.")))
                    .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("billingAddress.addressType")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("if present, must be BILLING")));
        }
        
        private void requestHasBodyWhereAddressTypeIs(String addressTypeValue) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            if (addressTypeValue != null) {
                content.append("\"addressType\":\"").append(addressTypeValue).append("\"").append(",");
            }
            content.append("\"street1\":\"Bennelong Point\"");
            content.append(",\"stateCode\":\"NSW\"");
            content.append(",\"city\":\"Sydney\"");
            content.append(",\"countryCode\":\"AU\"");
            content.append(",\"postcode\":\"2000\"");
            content.append("}");
            content.append(",\"orderIds\":[\"12u3\",\"sad2\"]");
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @Nested
    @DisplayName("Validates \"orderIds\"")
    class OrderIdsValidation {
        
        @Test
        @DisplayName("Requires presence")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequirePresence() throws Exception {
            //given
            requestHasBodyWhereOrderIdsIs(absent());
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("orderIds")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Requires not to be empty")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldRequireNotToBeEmpty() throws Exception {
            //given
            requestHasBodyWhereOrderIdsIs("[]");
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("orderIds")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("must not be empty")));
        }
        
        @Test
        @DisplayName("Checks max length")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldCheckMaxLength() throws Exception {
            //given
            final String tooLongOrderIdsValue = "[\"sad3f\",\"hja4sd\",\"klh2fla\",\"sd7on\",\"d2c34y\",\"das86fa\",\"sw9d7f\",\"n6as9d\",\"asd97f\",\"d7nf98s\"]";
            requestHasBodyWhereOrderIdsIs(tooLongOrderIdsValue);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(invoiceGenerationInvalidParamsProblem())
                    .andExpect(jsonPath("$.invalidParams", hasSize(1)))
                    .andExpect(jsonPath("$.invalidParams[0].name", is("orderIds")))
                    .andExpect(jsonPath("$.invalidParams[0].reason", is("size must be between 1 and 9")));
        }
        
        @Test
        @DisplayName("Allows repeated order ids")
        @WithBearerJwt(attributes = {
                @WithBearerJwt.Attribute(name = "account_number", value = wellKnownAccountNumber)
        })
        void shouldAllowRepeatedOrderIds() throws Exception {
            //given
            requestHasBodyWhereOrderIdsIs("[\"1a\",\"2b\",\"3c\",\"2b\"]");
            
            final Address billingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000", AddressType.BILLING);
            
            final String generatedInvoiceId = "1";
            final Address generatedBillingAddress = new Address("Bennelong Point", null, "NSW", "Sydney", "AU", "2000");
            generatedBillingAddress.setAddressType(AddressType.BILLING);
            final Invoice generatedInvoice = new Invoice(wellKnownAccountNumber, generatedBillingAddress);
            generatedInvoice.setId(generatedInvoiceId);
            generatedInvoice.setInvoiceStatus(InvoiceStatus.CREATED);
            
            given(invoiceService.generate(wellKnownAccountNumber, setOf("1a", "2b", "3c"), billingAddress)).willReturn(generatedInvoice);
            
            //when
            executeRequest();
            
            //then
            getResult()
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Location", notNullValue()))
                    .andExpect(header().string("Location", startsWith("http://")))
                    .andExpect(header().string("Location", endsWith(rootPath + "/" + generatedInvoiceId)));
            
        }
        
        private void requestHasBodyWhereOrderIdsIs(String orderIdsValue) {
            final StringBuilder content = new StringBuilder("{");
            content.append("\"billingAddress\":{");
            content.append("\"addressType\":\"BILLING\"");
            content.append(",\"street1\":\"Bennelong Point\"");
            content.append(",\"stateCode\":\"NSW\"");
            content.append(",\"city\":\"Sydney\"");
            content.append(",\"countryCode\":\"AU\"");
            content.append(",\"postcode\":\"2000\"");
            content.append("}");
            if (orderIdsValue != null) {
                content.append(",\"orderIds\":").append(orderIdsValue);
            }
            content.append("}");
            requestHasBody(content.toString());
        }
        
    }
    
    @SafeVarargs
    private <T> Set<T> setOf(T... a) {
        return new HashSet<>(Arrays.asList(a));
    }
    
    private void requestHasBody(String content) {
        this.content = content;
    }
    
    private String absent() {
        return null;
    }
    
    private String empty() {
        return "";
    }
    
    void executeRequest() throws Exception {
        result = mockMvc.perform(
                post(rootPath)
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content));
    }
    
    private ResultActions getResult() {
        return result;
    }
    
    private ResultMatcher invoiceGenerationInvalidParamsProblem() {
        return matchAll(
                status().isUnprocessableEntity(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                jsonPath("$.type", is("https://example.net/validation-error")),
                jsonPath("$.title", is("Your request parameters didn't validate.")),
                jsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
        );
    }
    
}
