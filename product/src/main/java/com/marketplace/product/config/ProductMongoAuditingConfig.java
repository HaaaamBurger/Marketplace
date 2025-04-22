package com.marketplace.product.config;

import com.marketplace.common.config.MongoAuditingConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MongoAuditingConfig.class)
public class ProductMongoAuditingConfig {
}
