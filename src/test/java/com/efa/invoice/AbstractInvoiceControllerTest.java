package com.efa.invoice;

import com.efa.OrderServiceApplication;
import com.efa.SimpleInvalidParametersConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

@SpringJUnitConfig
class AbstractInvoiceControllerTest {
    
    @Configuration
    @Import({InvalidRequestParametersAnalyzer.class, SimpleInvalidParametersConstructor.class, InvoiceRepresentationModelAssembler.class, InvoiceRepresentationModelProcessor.class})
    static class Config extends OrderServiceApplication {
        
        /**
         * Because none of
         * org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerJwtConfiguration.JwtDecoderConfiguration
         * bean method conditions match
         */
        @Bean
        JwtDecoder fakeJwtDecoder() {
            return token -> {
                throw new IllegalStateException("Shouldn't be used.");
            };
        }
        
    }
    
    @MockBean
    protected InvoiceService invoiceService;
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    protected final String rootPath = "/v1/invoices";
    
    protected final String wellKnownAccountNumber = "a857c0b84313490eb078fb5b4018ffff";
    
}
