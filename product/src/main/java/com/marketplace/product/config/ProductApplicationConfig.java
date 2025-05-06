package com.marketplace.product.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.marketplace.auth"})
public class ProductApplicationConfig {
}
