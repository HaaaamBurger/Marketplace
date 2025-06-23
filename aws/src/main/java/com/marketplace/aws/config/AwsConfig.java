package com.marketplace.aws.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
public class AwsConfig {

    @Value("${aws.s3.access-key}")
    private String AWS_S3_ACCESS_KEY;

    @Value("${aws.s3.secret-key}")
    private String AWS_S3_SECRET_KEY;

    @Bean
    public S3Client s3Client() {
        S3Client s3Client = S3Client.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY)))
                .build();

        log.info("[S3BucketService]: S3 Client successfully Initialized");
        return s3Client;
    }
}
