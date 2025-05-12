package com.marketplace.order.web.dto;

import com.marketplace.order.web.model.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;

    private String ownerId;

    private List<String> productIds;

    private String address;

    OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
