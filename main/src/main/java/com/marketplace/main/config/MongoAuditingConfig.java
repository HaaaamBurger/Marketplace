package com.marketplace.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(com.main.common.config.MongoAuditingConfig.class)
public class MongoAuditingConfig {
}
