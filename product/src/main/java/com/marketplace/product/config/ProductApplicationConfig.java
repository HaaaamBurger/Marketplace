package com.marketplace.product.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.marketplace.product",
        "com.marketplace.auth"
})
public class ProductApplicationConfig {
}
