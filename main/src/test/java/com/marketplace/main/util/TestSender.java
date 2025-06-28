package com.marketplace.main.util;

import com.marketplace.product.kafka.config.OutputTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeleteProductFromOrdersEvent(String productId) {
        sendEvent(OutputTopics.PRODUCTS_DELETE_FROM_ORDER_TOPIC, productId);
    }

    private void sendEvent(String topic, Object data) {
        log.info("[TEST_SENDER]: Sent {} to topic {}", data, topic);
        kafkaTemplate.send(topic, data);
    }

}
