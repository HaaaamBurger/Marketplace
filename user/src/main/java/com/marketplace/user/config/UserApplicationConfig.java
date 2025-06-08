package com.marketplace.user.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.marketplace.user",
        "com.marketplace.auth",
        "com.marketplace.usercore"
})
public class UserApplicationConfig {
}
