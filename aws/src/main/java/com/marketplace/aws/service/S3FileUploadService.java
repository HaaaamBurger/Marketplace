package com.marketplace.aws.service;

import org.springframework.core.io.InputStreamSource;

import java.net.URL;
import java.util.Optional;

public interface S3FileUploadService {

    Optional<URL> uploadFile(InputStreamSource file, String fileName);

    void downloadFile(String targetLocation, String fileName);

}
