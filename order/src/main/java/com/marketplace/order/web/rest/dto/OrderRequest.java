package com.marketplace.order.web.rest.dto;

import com.marketplace.order.web.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
public class OrderRequest {

    @NotEmpty(message = "Product list must not be empty")
    private List<String> productIds;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Status is required")
    OrderStatus status;
}
