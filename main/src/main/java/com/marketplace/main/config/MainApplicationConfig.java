package com.marketplace.main.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ComponentScan(basePackages = {
        "com.marketplace.main",
        "com.marketplace.common",
        "com.marketplace.product",
        "com.marketplace.user",
        "com.marketplace.order",
        "com.marketplace.auth"
})
public class MainApplicationConfig {
}
