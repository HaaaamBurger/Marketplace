package com.marketplace.order.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ComponentScan(basePackages = {
        "com.marketplace.auth",
        "com.marketplace.product"
})
public class OrderApplicationConfig {
}
