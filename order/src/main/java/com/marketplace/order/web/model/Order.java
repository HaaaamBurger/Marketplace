package com.marketplace.order.web.model;

import com.marketplace.common.model.AuditableEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Document(collection = "orders")
@EqualsAndHashCode(callSuper = true)
public class Order extends AuditableEntity {

    @Id
    private String id;

    @NotNull(message = "Owner Id is required")
    private String ownerId;

    @Size(min = 1, max = 50, message = "Order must contain at least 1 product and maximum 50")
    private Set<String> productIds;

    private String address;

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private BigDecimal total;

}
