package com.marketplace.product.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "products")
public class Product extends AuditableEntity {


    @Id
    private String id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 250, message = "Description must be between 5 and 250 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "5.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 digits before the decimal point and 2 after")
    private BigDecimal price;
}

