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

import java.io.ByteArrayInputStream;
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
                String type = item.isDir() ? "DIRECTORY" : "FILE";
                if (type.equals("DIRECTORY")) {
                    objectName = objectName.substring(0, objectName.length() - 1);
                }
                int lastSlashIndex = objectName.lastIndexOf("/");
                String fileName = objectName.substring(lastSlashIndex + 1);

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

    public void deleteFile(String username, String path) {
        var byUsername = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("Path is required");
        }
        String objectPath = String.format("user-%d-files/%s", byUsername.getId(), path);
        try {
            var object = RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectPath)
                    .build();
            minioClient.removeObject(object);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file", e);
        }

    }

    public ResourceResponse createDirectory(String username, String path) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (path.isEmpty()) {
            throw new RuntimeException("Path is required");
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        String objectPath = String.format("user-%d-files/%s", user.getId(), path);
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectPath)
                    .stream(emptyStream, 0, PART_SIZE)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            throw new RuntimeException("Error creating directory", e);
        }
        var pathWithoutSlash = path.substring(0, path.length() - 1);
        var lastSlash = pathWithoutSlash.lastIndexOf("/");

        String parentPath;
        String folderName;

        if (lastSlash == -1) {
            parentPath = "/";
            folderName = pathWithoutSlash;
        } else {
            parentPath = pathWithoutSlash.substring(0, lastSlash + 1);
            folderName = pathWithoutSlash.substring(lastSlash + 1);
        }
        return new ResourceResponse(parentPath, folderName, null, "DIRECTORY");
    }

    public ResourceResponse getResourceInfo(String username, String path) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (path == null || path.isEmpty()) {
            throw new RuntimeException("Path is required");
        }

        StatObjectResponse statObjectResponse;
        try {
            String objectPath = String.format("user-%d-files/%s", user.getId(), path);
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectPath)
                    .build();
            statObjectResponse = minioClient.statObject(statObjectArgs);
        } catch (Exception e) {
            throw new RuntimeException("Error getting file", e);
        }

        String type = path.endsWith("/") ? "DIRECTORY" : "FILE";

        String parentPath;
        String resourceName;

        if (type.equals("DIRECTORY")) {
            String pathWithoutSlash = path.substring(0, path.length() - 1);
            int lastSlash = pathWithoutSlash.lastIndexOf("/");

            if (lastSlash == -1) {
                parentPath = "/";
                resourceName = pathWithoutSlash;
            } else {
                parentPath = pathWithoutSlash.substring(0, lastSlash + 1);
                resourceName = pathWithoutSlash.substring(lastSlash + 1);
            }
        } else {
            int lastSlash = path.lastIndexOf("/");
            if (lastSlash == -1) {
                parentPath = "/";
                resourceName = path;
            } else {
                parentPath = path.substring(0, lastSlash + 1);
                resourceName = path.substring(lastSlash + 1);
            }
        }
        Long size = type.equals("DIRECTORY") ? null : statObjectResponse.size();

        return new ResourceResponse(parentPath, resourceName, size, type);
    }

    public List<ResourceResponse> searchFile(String username, String query) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<ResourceResponse> responses = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            throw new RuntimeException("Query is required");
        }

        String prefix = String.format("user-%d-files/", user.getId());

        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .prefix(prefix)
                .build();

        try {
            for (Result<Item> itemResult : minioClient.listObjects(listObjectsArgs)) {
                Item item = itemResult.get();

                String objectName = item.objectName();
                String type = item.isDir() ? "DIRECTORY" : "FILE";

                if (type.equals("DIRECTORY")) {
                    objectName = objectName.substring(0, objectName.length() - 1);
                }

                int lastSlashIndex = objectName.lastIndexOf("/");
                String fileName = objectName.substring(lastSlashIndex + 1);

                if (fileName.toLowerCase().contains(query.toLowerCase())) {
                    String pathWithoutPrefix = objectName.replace(prefix, "");

                    String parentPath;
                    int lastSlash = pathWithoutPrefix.lastIndexOf("/");

                    if (lastSlash == -1) {
                        parentPath = "/";
                    } else {
                        parentPath = pathWithoutPrefix.substring(0, lastSlash + 1);
                    }
                    Long size = type.equals("DIRECTORY") ? null : item.size();

                    ResourceResponse response = new ResourceResponse(parentPath, fileName, size, type);
                    responses.add(response);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error searching files", e);
        }

        return responses;
    }

}
