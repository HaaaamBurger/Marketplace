package com.marketplace.aws.service;

import com.marketplace.aws.exception.AwsPhotoUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class S3FileBusinessService implements S3FileManagerService {

    private static final String[] PHOTO_EXTENSIONS = new String[]{".png", ".jpeg", ".jpg", ".gif"};

    @Override
    public String getFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    @Override
    public String getExtension(String fileName) {
        validateFilenameOrThrow(fileName);
        return validateFileExtensionOrThrow(fileName);
    }

    private void validateFilenameOrThrow(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            throw new AwsPhotoUploadException("File name is missing or has no valid extension");
        }
    }

    private String validateFileExtensionOrThrow(String fileName) {
        String originalPhotoExtension = fileName.substring(fileName.lastIndexOf('.'));

        boolean isValidPhotoExtension = Arrays.asList(PHOTO_EXTENSIONS).contains(originalPhotoExtension);
        if (isValidPhotoExtension) {
            return originalPhotoExtension;
        }

        throw new AwsPhotoUploadException("Unsupported photo extension: " + originalPhotoExtension);
    }

}

