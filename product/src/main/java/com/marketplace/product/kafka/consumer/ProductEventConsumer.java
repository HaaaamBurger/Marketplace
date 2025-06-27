package com.marketplace.product.kafka.consumer;

import com.marketplace.product.kafka.config.InputTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@Configuration
public class ProductEventConsumer {

    @KafkaListener(
            topics = InputTopics.PRODUCTS_DELETE_TOPIC,
            groupId = "products_delete_group",
            properties = {"auto.offset.reset=latest"}
    )
    public void listenProductDeleteEvent(String productId) {
        log.info("[PRODUCT_EVENT_CONSUMER]: Received event {} from {}", productId, InputTopics.PRODUCTS_DELETE_TOPIC);
    }

}
