package com.marketplace.order.kafka.consumer;

import com.marketplace.order.kafka.config.InputTopics;
import com.marketplace.order.service.ProductEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductEventService productEventService;

    @KafkaListener(
            topics = InputTopics.PRODUCTS_DELETE_FROM_ORDER_TOPIC,
            groupId = "products_delete_group",
            properties = {"auto.offset.reset=latest"}
    )
    public void listenDeleteProductFromOrdersEvent(String productId) {
        log.info("[PRODUCT_EVENT_CONSUMER]: Received event {} from {}", productId, InputTopics.PRODUCTS_DELETE_FROM_ORDER_TOPIC);

        productEventService.deleteProductFromOrdersAndProduct(productId);
    }

}
