package com.marketplace.order.web.dto;

import com.marketplace.common.dto.BaseResponse;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.web.model.Product;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Set;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderResponse extends BaseResponse {

    private String ownerId;

    private Set<Product> products;

    private String address;

    private BigDecimal total;

    private OrderStatus status;

}
