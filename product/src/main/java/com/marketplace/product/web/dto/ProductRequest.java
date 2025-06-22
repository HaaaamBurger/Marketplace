package com.marketplace.product.web.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
public class ProductRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 250, message = "Description must be between 5 and 250 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be greater or equal to 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 digits before the decimal point and 2 after")
    private BigDecimal price;

    @Min(value = 0, message = "Amount cannot be negative value")
    private Integer amount;

    private MultipartFile photo;

    @NotNull(message = "Active is required")
    private Boolean active;

}
