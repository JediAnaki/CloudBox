package com.example.zhukov.cloudbox.service;

import com.example.zhukov.cloudbox.config.AbstractIT;
import com.example.zhukov.cloudbox.config.MinioConfig;
import com.example.zhukov.cloudbox.dto.file.ResourceResponse;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Transactional

public class FileServiceIT extends AbstractIT {

    @Autowired
    private FileService fileService;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Загрузка файла в MinIO")
    void shouldUploadFile_whenValidRequest() {
        var user = userService.registerUser("zhukov", "zhukov@gmail.com", "123456");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        ResourceResponse response = fileService.upload("zhukov", "/", file);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("test.txt", response.getName());
        Assertions.assertEquals("FILE", response.getType());

        assertDoesNotThrow(() -> {
            String objectPath = String.format("user-%d-files/test.txt", user.getId());
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectPath)
                    .build();
            StatObjectResponse stat = minioClient.statObject(args);
            Assertions.assertNotNull(stat);
        });

    }

    @Test
    void shouldDeleteFile_whenValidRequest() {
        var user = userService.registerUser("zhukov", "zhukov@gmail.com", "123456");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        fileService.upload("zhukov", "/", file);
        fileService.deleteFile("zhukov", file.getOriginalFilename());
        assertThrows(
                io.minio.errors.ErrorResponseException.class,
                () -> {
                    String objectPath = String.format("user-%d-files/test.txt", user.getId());
                    StatObjectArgs args = StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectPath)
                            .build();
                    minioClient.statObject(args);
                }
        );
    }

    @Test
    void shouldRenameFile_whenValidRequest() {
        var user = userService.registerUser("zhukov", "zhukov@gmail.com", "123456");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        fileService.upload("zhukov", "/", file);
        fileService.moveResource("zhukov", file.getOriginalFilename(), "newName.txt");

        assertThrows(
                ErrorResponseException.class,
                () -> {
                    String oldPath = String.format("user-%d-files/test.txt", user.getId());
                    StatObjectArgs args = StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(oldPath)
                            .build();
                    minioClient.statObject(args);
                }
        );

        assertDoesNotThrow(() -> {
            String newPath = String.format("user-%d-files/newName.txt", user.getId());
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(newPath)
                    .build();
            minioClient.statObject(args);
        });


    }

    @Test
    void shouldNotAccessOtherUserFiles_whenValidRequest() {

    }

    @Test
    void searchFile() {
    }


}
