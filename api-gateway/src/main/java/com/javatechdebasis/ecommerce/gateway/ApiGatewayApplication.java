package com.javatechdebasis.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway. Single entry point that routes external REST traffic to
 * the appropriate microservice, resolving instances through Eureka.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
