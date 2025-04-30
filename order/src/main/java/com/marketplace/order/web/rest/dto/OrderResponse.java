package com.marketplace.order.web.rest.dto;

import com.marketplace.order.web.model.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private String id;

    private String userId;

    private List<String> productIds;

    private String address;

    OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
