package com.marketplace.product.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfigurer {

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(InputTopics.PRODUCTS_DELETE_TOPIC, 3, (short) 2);
    }

}
