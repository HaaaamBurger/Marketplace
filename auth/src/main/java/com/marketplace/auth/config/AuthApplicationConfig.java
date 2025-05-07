package com.marketplace.auth.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = {"com.marketplace.auth.repository"})
@ComponentScan(basePackages = {"com.marketplace.auth"})
public class AuthApplicationConfig {
}
