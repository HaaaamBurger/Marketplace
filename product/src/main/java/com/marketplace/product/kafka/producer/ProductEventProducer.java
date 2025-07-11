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

    public void sendDeleteProductInstancesEvent(String productId) {
        log.info("[PRODUCT_EVENT_PRODUCER: Sent {} for product deletion", OutputTopics.DELETE_PRODUCT_INSTANCES_TOPIC);
        kafkaTemplate.send(OutputTopics.DELETE_PRODUCT_INSTANCES_TOPIC, productId);
    }

}
