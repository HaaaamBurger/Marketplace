package com.martketplace.aws.service;

import com.marketplace.aws.config.AwsApplicationConfig;
import com.marketplace.aws.exception.AwsPhotoUploadException;
import com.marketplace.aws.service.S3ProductPhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AwsApplicationConfig.class)
public class S3ProductPhotoServiceTest {

    @Autowired
    private S3ProductPhotoService s3ProductPhotoService;

    @MockitoBean
    private S3Client s3Client;

    @Test
    public void uploadFile_ShouldSuccessfullyUploadPhoto() {
        String fileName = "fileName";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("photo", "photo.png", "image/png", "photo".getBytes());

        URL url = s3ProductPhotoService.uploadFile(mockMultipartFile, fileName);

        assertThat(url).isNotNull();
        assertThat(url.getPath()).contains(fileName);
        assertThat(url.getPath()).contains(s3ProductPhotoService.AWS_S3_PRODUCTS_PHOTO_LOCATION);
        assertThat(url.getAuthority()).isEqualTo(extractAuthority(s3ProductPhotoService.AWS_S3_BUCKET_BASE_URL));
    }

    @Test
    public void uploadFile_ShouldThrowException_WhenUnsupportedExtensionFormat() {
        String fileName = "fileName";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("photo", "photo.svg", "image/svg", "photo".getBytes());

        assertThatThrownBy(() -> s3ProductPhotoService.uploadFile(mockMultipartFile, fileName))
                .isInstanceOf(AwsPhotoUploadException.class)
                .hasMessage("Unsupported photo extension: .svg");
    }

    @Test
    public void uploadFile_ShouldThrowException_WhenMultipartIsEmpty() {
        String fileName = "fileName";
        MultipartFile mockedMultipartFile = mock(MultipartFile.class);

        when(mockedMultipartFile.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> s3ProductPhotoService.uploadFile(mockedMultipartFile, fileName))
                .isInstanceOf(AwsPhotoUploadException.class)
                .hasMessage("Cannot upload not multipart photo");

    }

    @Test
    public void uploadFile_ShouldThrowException_WhenNotInstanceFromMultipart() {
        String fileName = "fileName";
        ByteArrayResource mockedByteArrayResource = mock(ByteArrayResource.class);

        assertThatThrownBy(() -> s3ProductPhotoService.uploadFile(mockedByteArrayResource, fileName))
                .isInstanceOf(AwsPhotoUploadException.class)
                .hasMessage("Cannot upload not multipart photo");

    }

    @Test
    public void validateAndGetExtensionFromFilename_ShouldReturnExtension() {
        String pngFileName = "file.png";
        String jpegFileName = "file.jpeg";
        String jpgFileName = "file.jpg";
        String gifFileName = "file.gif";

        String pngExtension = s3ProductPhotoService.validateAndGetExtensionFromFilename(pngFileName);
        String jpegExtension = s3ProductPhotoService.validateAndGetExtensionFromFilename(jpegFileName);
        String jpgExtension = s3ProductPhotoService.validateAndGetExtensionFromFilename(jpgFileName);
        String gifExtension = s3ProductPhotoService.validateAndGetExtensionFromFilename(gifFileName);

        assertThat(pngExtension).isNotNull();
        assertThat(pngExtension).isEqualTo(".png");
        assertThat(jpegExtension).isNotNull();
        assertThat(jpegExtension).isEqualTo(".jpeg");
        assertThat(jpgExtension).isNotNull();
        assertThat(jpgExtension).isEqualTo(".jpg");
        assertThat(gifExtension).isNotNull();
        assertThat(gifExtension).isEqualTo(".gif");
    }

    @Test
    public void validateAndGetExtensionFromFilename_ShouldThrowException_WhenUnsupportedFormat() {
        String fileName = "file.csv";

        assertThatThrownBy(() -> s3ProductPhotoService.validateAndGetExtensionFromFilename(fileName))
                .isInstanceOf(AwsPhotoUploadException.class).hasMessage("Unsupported photo extension: .csv");

    }

    @Test
    public void validateAndGetExtensionFromFilename_ShouldThrowException() {
        String fileName = "file";

        assertThatThrownBy(() -> s3ProductPhotoService.validateAndGetExtensionFromFilename(fileName))
                .isInstanceOf(AwsPhotoUploadException.class)
                .hasMessage("File name is missing or has no valid extension");

    }

    private String extractAuthority(String url) {
        try {
            return new URL(url).getAuthority();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

}