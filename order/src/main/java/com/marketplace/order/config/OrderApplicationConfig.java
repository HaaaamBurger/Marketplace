package com.marketplace.order.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.marketplace.order",
        "com.marketplace.auth",
        "com.marketplace.product"
})
@EnableMongoRepositories(basePackages = {"com.marketplace.order.repository"})
public class OrderApplicationConfig {
}
