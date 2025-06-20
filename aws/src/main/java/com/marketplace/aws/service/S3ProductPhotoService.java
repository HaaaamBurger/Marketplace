package com.marketplace.aws.service;

import com.marketplace.aws.exception.AwsPhotoUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ProductPhotoService implements S3FileUploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.products-photo-location}")
    private String PRODUCTS_PHOTO_LOCATION;

    @Value("${aws.s3.bucket-name}")
    private String AWS_S3_BUCKET_NAME;

    @Value("${aws.s3.bucket-base-url}")
    private String AWS_S3_BUCKET_BASE_URL;

    @Override
    public URL uploadFile(InputStreamSource file, String fileName) {

        if (!(file instanceof MultipartFile multipartFile)) {
            throw new IllegalArgumentException("Cannot upload not multipart photo");
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(AWS_S3_BUCKET_NAME)
                .key(String.format("%s/%s", PRODUCTS_PHOTO_LOCATION, fileName + ".png"))
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            log.info("[S3_PRODUCT_PHOTO_SERVICE]: Photo successfully uploaded");

            return new URL(buildProductPhotoUrl(fileName));
        } catch (IOException e) {
            log.error("[S3_PRODUCT_PHOTO_SERVICE]: {}", e.getMessage());
            throw new AwsPhotoUploadException("Photo upload failed");
        }
    }

    @Override
    public void downloadFile(String targetLocation, String fileName) {

    }

    private String buildProductPhotoUrl(String fileName) {
        return AWS_S3_BUCKET_BASE_URL + "/" + PRODUCTS_PHOTO_LOCATION + "/" + fileName + ".png";
    }
}
