package com.marketplace.product.kafka.producer;

import com.marketplace.product.kafka.config.OutputTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeleteProductFromOrdersEvent(String productId) {
        log.info("[PRODUCT_EVENT_PRODUCER: Sent {} for product deletion", OutputTopics.PRODUCTS_DELETE_FROM_ORDER_TOPIC);
        kafkaTemplate.send(OutputTopics.PRODUCTS_DELETE_FROM_ORDER_TOPIC, productId);
    }

}
