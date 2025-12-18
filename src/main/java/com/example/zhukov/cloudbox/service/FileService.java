package com.example.zhukov.cloudbox.service;

import com.example.zhukov.cloudbox.config.MinioConfig;
import com.example.zhukov.cloudbox.dto.file.ResourceResponse;
import com.example.zhukov.cloudbox.entity.User;
import com.example.zhukov.cloudbox.repository.UserRepository;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final UserRepository userRepository;
    private final static long PART_SIZE = -1;


    public ResourceResponse upload(String username,
                                   String path,
                                   MultipartFile multipartFile) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found" + username));
        String objectPath;
        if (path == null || path.isEmpty() || path.equals("/")) {
            objectPath = String.format("user-%d-files/%s", user.getId(), multipartFile.getOriginalFilename());
        } else {
            objectPath = String.format("user-%d-files/%s/%s", user.getId(), path, multipartFile.getOriginalFilename());
        }

        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectPath)
                    .stream(multipartFile.getInputStream(), multipartFile.getSize(), PART_SIZE)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResourceResponse(path, multipartFile.getOriginalFilename(), multipartFile.getSize(), "FILE");
    }

    public List<ResourceResponse> listDirectory(String username, String path) {
        List<ResourceResponse> responses = new ArrayList<>();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String normalizedPath = (path == null || path.isEmpty() || path.equals("/")) ? "/" : path;

        String prefix = (normalizedPath.equals("/"))
                ? String.format("user-%d-files/", user.getId())
                : String.format("user-%d-files/%s", user.getId(), normalizedPath);

        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .prefix(prefix)
                .build();

        try {
            for (Result<Item> itemResult : minioClient.listObjects(listObjectsArgs)) {
                Item item = itemResult.get();

                String objectName = item.objectName();
                int lastSlashIndex = objectName.lastIndexOf("/");
                String fileName = objectName.substring(lastSlashIndex + 1);

                String type = item.isDir() ? "DIRECTORY" : "FILE";
                Long size = item.isDir() ? null : item.size();

                ResourceResponse response = new ResourceResponse(normalizedPath, fileName, size, type);
                responses.add(response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error listing directory", e);
        }

        return responses;
    }

    public InputStream downloadFile(String username, String path) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (path == null || path.isEmpty()) {
            throw new RuntimeException("Path is required");
        }
        String objectPath = String.format("user-%d-files/%s", user.getId(), path);
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectPath)
                .build();
        try {
            return minioClient.getObject(args);
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }

}
