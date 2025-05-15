package com.marketplace.usercore.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.marketplace.usercore"})
@EnableMongoRepositories(basePackages = {"com.marketplace.usercore.repository"})
public class UserCoreApplicationConfig {
}
