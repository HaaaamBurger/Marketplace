package com.marketplace.aws.service;

import org.springframework.core.io.InputStreamSource;

import java.net.URL;

public interface S3FileUploadService {

    URL uploadFile(InputStreamSource file, String fileName);

    String validateAndGetExtensionFromFilename(String fileName);

}
