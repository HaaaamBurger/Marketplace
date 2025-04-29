package com.marketplace.product.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductCreateRequest {

    private String name;

    private String description;

    private BigDecimal price;

}
