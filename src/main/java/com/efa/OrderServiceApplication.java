package com.efa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

//@EnableDiscoveryClient
@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@ImportAutoConfiguration(exclude = ErrorMvcAutoConfiguration.class)
@Import({ErrorConfiguration.class, HypermediaConfiguration.class})
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
    
}
