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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ProductPhotoService implements S3FileUploadService {

    private final S3Client s3Client;

    private final S3FileManagerService s3FileManagerService;

    @Value("${aws.s3.products-photo-location}")
    public String AWS_S3_PRODUCTS_PHOTO_LOCATION;

    @Value("${aws.s3.bucket-name}")
    private String AWS_S3_BUCKET_NAME;

    @Value("${aws.s3.bucket-base-url}")
    public String AWS_S3_BUCKET_BASE_URL;

    @Override
    public URL uploadFile(InputStreamSource file, String fileName) {

        MultipartFile multipartFile = validateInputStreamSourceOrThrow(file);
        String extension = s3FileManagerService.getExtension(multipartFile.getOriginalFilename());

        return uploadFileOrThrow(multipartFile, fileName, extension);
    }

    @Override
    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(AWS_S3_BUCKET_NAME)
                .key(buildProductPhotoPath(fileName))
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (RuntimeException exception) {
            log.error("[S3_PRODUCT_PHOTO_SERVICE]: File deletion failed {}", exception.getMessage());
            return;
        }

        log.info("[S3_PRODUCT_PHOTO_SERVICE]: File {} successfully deleted", fileName);
    }

    private MultipartFile validateInputStreamSourceOrThrow(InputStreamSource file) {
        if (!(file instanceof MultipartFile multipartFile) || multipartFile.isEmpty()) {
            throw new AwsPhotoUploadException("Cannot upload not multipart photo");
        }

        return multipartFile;
    }

    private URL uploadFileOrThrow(MultipartFile multipartFile, String fileName, String extension) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(AWS_S3_BUCKET_NAME)
                .key(buildProductPhotoPath(fileName, extension))
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            log.info("[S3_PRODUCT_PHOTO_SERVICE]: Photo successfully uploaded");

            return buildProductPhotoUrl(fileName, extension);
        } catch (IOException e) {
            throw new AwsPhotoUploadException("Photo upload failed");
        }
    }

    private URL buildProductPhotoUrl(String fileName, String extension) throws MalformedURLException {
        return new URL(AWS_S3_BUCKET_BASE_URL + "/" + buildProductPhotoPath(fileName, extension));
    }

    private String buildProductPhotoPath(String fileName, String extension) {
        return buildProductPhotoPath(fileName) + extension;
    }

    private String buildProductPhotoPath(String fileName) {
        return AWS_S3_PRODUCTS_PHOTO_LOCATION + '/' + fileName;
    }
}
