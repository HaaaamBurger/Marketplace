package com.marketplace.order.web.model;

import com.marketplace.common.model.AuditableEntity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "Order")
public class Order extends AuditableEntity {

    @Id
    private String id;

    @NotNull(message = "User ID cannot be null")
    private String userId;

    @NotEmpty(message = "Order must contain at least one product")
    private List<String> productIds;

    @NotNull(message = "Address cannot be null")
    private String address;

    @NotNull(message = "Order status must not be null")
    private OrderStatus status;

}
