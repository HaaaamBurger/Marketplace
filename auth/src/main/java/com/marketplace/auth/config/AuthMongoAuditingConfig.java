package com.marketplace.auth.config;

import com.marketplace.common.config.MongoAuditingConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MongoAuditingConfig.class)
public class AuthMongoAuditingConfig {
}
