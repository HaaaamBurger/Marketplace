package com.marketplace.product.web.dto;

import com.marketplace.common.dto.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ProductResponse extends BaseResponse {

    private String name;

    private String ownerId;

    private String description;

    private BigDecimal price;

    private Integer amount;

    private String photoUrl;

    private Boolean active;

}
