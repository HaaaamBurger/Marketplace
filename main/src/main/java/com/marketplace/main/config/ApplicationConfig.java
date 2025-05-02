package com.marketplace.main.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ComponentScan(basePackages = {
        "com.marketplace.main",
        "com.marketplace.auth",
        "com.marketplace.common",
        "com.marketplace.product",
        "com.marketplace.user",
        "com.marketplace.order"
})
@EnableMongoRepositories(basePackages = {
        "com.marketplace.auth.repository",
        "com.marketplace.product.repository",
        "com.marketplace.order.repository"
})
public class ApplicationConfig {
}
