package com.marketplace.product.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.marketplace.product",
        "com.marketplace.auth"
})
@EnableMongoRepositories(basePackages = {"com.marketplace.product.repository"})
public class ProductApplicationConfig {
}
