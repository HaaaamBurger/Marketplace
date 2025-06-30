package com.marketplace.product.web.validator;

import com.marketplace.aws.exception.AwsPhotoUploadException;
import com.marketplace.aws.service.S3FileUploadService;
import com.marketplace.product.web.dto.ProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@RequiredArgsConstructor
public class ProductValidator implements Validator {

    private final S3FileUploadService s3FileUploadService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ProductRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        ProductRequest productRequest = (ProductRequest) target;
        if (productRequest.getPhoto() != null && !productRequest.getPhoto().isEmpty()) {
            try {
                s3FileUploadService.getExtension(productRequest.getPhoto().getOriginalFilename());
            } catch (AwsPhotoUploadException exception) {
                rejectPhotoValue(errors, exception.getMessage());
            }
        }
    }

    private void rejectPhotoValue(Errors errors, String message) {
        errors.rejectValue(
                "photo",
                "error.photo",
                message
        );
    }
}
