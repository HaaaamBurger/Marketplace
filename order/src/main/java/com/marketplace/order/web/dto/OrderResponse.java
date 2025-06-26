package com.marketplace.order.web.dto;

import com.marketplace.order.web.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;

    private String ownerId;

    private Set<String> productIds;

    private String address;

    private BigDecimal total;

    OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
