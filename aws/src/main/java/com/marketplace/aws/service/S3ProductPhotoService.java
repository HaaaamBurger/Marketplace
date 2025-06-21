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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ProductPhotoService implements S3FileUploadService {

    private final S3Client s3Client;

    private static final String[] PHOTO_EXTENSIONS = new String[]{".png", ".jpeg", ".jpg", ".gif"};

    @Value("${aws.s3.products-photo-location}")
    private String AWS_S3_PRODUCTS_PHOTO_LOCATION;

    @Value("${aws.s3.bucket-name}")
    private String AWS_S3_BUCKET_NAME;

    @Value("${aws.s3.bucket-base-url}")
    private String AWS_S3_BUCKET_BASE_URL;

    @Override
    public URL uploadFile(InputStreamSource file, String fileName) {

        if (!(file instanceof MultipartFile multipartFile) || multipartFile.isEmpty()) {
            throw new AwsPhotoUploadException("Cannot upload not multipart photo");
        }

        String extension = validateAndGetExtensionFromFilename(multipartFile.getOriginalFilename());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(AWS_S3_BUCKET_NAME)
                .key(buildProductPhotoPath(fileName, extension))
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            log.info("[S3_PRODUCT_PHOTO_SERVICE]: Photo successfully uploaded");

            return buildProductPhotoUrl(fileName, extension);
        } catch (IOException e) {
            log.error("[S3_PRODUCT_PHOTO_SERVICE]: {}", e.getMessage());
            throw new AwsPhotoUploadException("Photo upload failed");
        }
    }

    @Override
    public String validateAndGetExtensionFromFilename(String fileName) {
        String originalPhotoExtension = fileName == null ? ".png" : fileName.substring(fileName.lastIndexOf('.'));

        boolean isValidPhotoExtension = Arrays.asList(PHOTO_EXTENSIONS).contains(originalPhotoExtension);
        if (isValidPhotoExtension) {
            return originalPhotoExtension;
        }

        throw new AwsPhotoUploadException("Unsupported photo extension: " + originalPhotoExtension);
    }

    private URL buildProductPhotoUrl(String fileName, String extension) throws MalformedURLException {
        return new URL(AWS_S3_BUCKET_BASE_URL + "/" + buildProductPhotoPath(fileName, extension));
    }

    private String buildProductPhotoPath(String fileName, String extension) {
        return AWS_S3_PRODUCTS_PHOTO_LOCATION + '/' + fileName + extension;
    }
}
