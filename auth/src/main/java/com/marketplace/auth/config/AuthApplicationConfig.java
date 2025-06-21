package com.marketplace.auth.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.marketplace.auth",
        "com.marketplace.usercore",
        "com.marketplace.common"
})
public class AuthApplicationConfig {
}
