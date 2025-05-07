package com.marketplace.main.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.marketplace.main",
        "com.marketplace.common",
        "com.marketplace.product",
        "com.marketplace.user",
        "com.marketplace.order"
})
public class MainApplicationConfig {
}
