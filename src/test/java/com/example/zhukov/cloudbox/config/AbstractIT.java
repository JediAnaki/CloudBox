package com.example.zhukov.cloudbox.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIT {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static MinIOContainer minioContainer = new MinIOContainer("minio/minio:RELEASE.2023-09-07T02-05-02Z");

    @BeforeAll
    static void createBucket() throws Exception {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials(minioContainer.getUserName(), minioContainer.getPassword())
                .build();
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket("test-bucket")
                        .build());
        if (!exists) {
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                    .bucket("test-bucket")
                    .build();
            minioClient.makeBucket(makeBucketArgs);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));

        registry.add("minio.endpoint", () -> minioContainer.getS3URL());
        registry.add("minio.access-key", () -> minioContainer.getUserName());
        registry.add("minio.secret-key", () -> minioContainer.getPassword());
    }
}
