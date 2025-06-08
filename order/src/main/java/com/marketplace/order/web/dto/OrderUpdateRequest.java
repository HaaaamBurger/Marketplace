package com.marketplace.order.web.dto;

import com.marketplace.order.web.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderUpdateRequest {

    private String address;

    OrderStatus status;

}
