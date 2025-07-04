package com.marketplace.aws.service;

public interface S3FileManagerService {

    String getFilenameFromUrl(String url);

    String getExtension(String fileName);

}
