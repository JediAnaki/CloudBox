package com.example.zhukov.cloudbox.service;

import com.example.zhukov.cloudbox.config.MinioConfig;
import com.example.zhukov.cloudbox.dto.file.ResourceResponse;
import com.example.zhukov.cloudbox.entity.User;
import com.example.zhukov.cloudbox.repository.UserRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
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
}
