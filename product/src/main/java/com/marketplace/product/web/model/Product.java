package com.marketplace.product.web.model;

import com.marketplace.common.model.AuditableEntity;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Document(collection = "products")
@EqualsAndHashCode(callSuper = true)
public class Product extends AuditableEntity {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Owner ID is required")
    private String ownerId;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 250, message = "Description must be between 5 and 250 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "5.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 digits before the decimal point and 2 after")
    private BigDecimal price;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative value")
    private Integer amount;

    private Boolean active;

    public boolean decreaseAmount() {
        if (amount > 0) {
            amount--;
            return true;
        }

        return false;
    }

}
