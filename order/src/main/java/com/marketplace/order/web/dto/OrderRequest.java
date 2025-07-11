package com.marketplace.order.web.dto;

import com.marketplace.order.web.model.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Data
@Builder(toBuilder = true)
public class OrderRequest {

    @NotEmpty(message = "Product list must not be empty")
    private Set<String> productIds;

    private String address;

    @NotNull(message = "Status is required")
    OrderStatus status;
}
