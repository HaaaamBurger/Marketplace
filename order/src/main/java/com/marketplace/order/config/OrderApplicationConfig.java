package com.marketplace.order.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.marketplace.auth"})
public class OrderApplicationConfig {
}
