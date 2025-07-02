package com.marketplace.main.util;

import com.marketplace.order.kafka.config.InputTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;

@Service
@RequiredArgsConstructor
public class TestListener {

    private static final LinkedBlockingQueue<String> dataPriorityBlockingList = new LinkedBlockingQueue<>();

    @KafkaListener(
            topics = InputTopics.PRODUCT_DELETE_INSTANCES_TOPIC,
            groupId = "test_product_delete_instances_group",
            properties = {"auto.offset.reset=latest"}
    )
    private void listenDeleteProductFromOrdersEvent(String productId) {
        dataPriorityBlockingList.add(productId);
    }

    public boolean hasReceived(String value) {
        return dataPriorityBlockingList.contains(value);
    }
}
