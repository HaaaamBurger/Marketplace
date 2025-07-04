package com.martketplace.aws.service;

import com.marketplace.aws.config.AwsApplicationConfig;
import com.marketplace.aws.exception.AwsPhotoUploadException;
import com.marketplace.aws.service.S3FileBusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = AwsApplicationConfig.class)
public class S3FileBusinessServiceTest {

    @MockitoBean
    private S3Client s3Client;

    @Autowired
    private S3FileBusinessService s3FileBusinessService;

    @Test
    public void getExtensionFromFilename_ShouldReturnExtension() {
        String pngFileName = "file.png";
        String jpegFileName = "file.jpeg";
        String jpgFileName = "file.jpg";
        String gifFileName = "file.gif";

        String pngExtension = s3FileBusinessService.getExtension(pngFileName);
        String jpegExtension = s3FileBusinessService.getExtension(jpegFileName);
        String jpgExtension = s3FileBusinessService.getExtension(jpgFileName);
        String gifExtension = s3FileBusinessService.getExtension(gifFileName);

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
    public void getExtension_ShouldThrowException_WhenUnsupportedFormat() {
        String fileName = "file.csv";

        assertThatThrownBy(() -> s3FileBusinessService.getExtension(fileName))
                .isInstanceOf(AwsPhotoUploadException.class).hasMessage("Unsupported photo extension: .csv");

    }

    @Test
    public void getExtension_ShouldThrowException() {
        String fileName = "file";

        assertThatThrownBy(() -> s3FileBusinessService.getExtension(fileName))
                .isInstanceOf(AwsPhotoUploadException.class)
                .hasMessage("File name is missing or has no valid extension");

    }

}
