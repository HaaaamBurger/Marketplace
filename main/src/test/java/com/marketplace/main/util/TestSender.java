package com.marketplace.main.util;

import com.marketplace.product.kafka.config.OutputTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeleteProductFromOrdersEvent(String productId) {
        sendEvent(OutputTopics.PRODUCT_DELETE_INSTANCES_TOPIC, productId);
    }

    private void sendEvent(String topic, Object data) {
        kafkaTemplate.send(topic, data);
    }

}
