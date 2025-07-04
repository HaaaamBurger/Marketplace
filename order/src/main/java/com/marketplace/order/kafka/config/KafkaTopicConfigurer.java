package com.marketplace.order.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfigurer {

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(InputTopics.DELETE_PRODUCT_INSTANCES_TOPIC, 3, (short) 2);
    }

}
