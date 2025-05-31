package com.marketplace.order.web.dto;

import com.marketplace.order.web.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderUpdateRequest {

    private String address;

    @NotNull(message = "Status is required")
    OrderStatus status;

}
