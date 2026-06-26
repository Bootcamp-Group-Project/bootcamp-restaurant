package com.restaurant.menu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

@Slf4j
@Service
public class R2ImageService implements ImageService {

    private final S3Client s3;
    private final String bucket;
    private final String publicUrl;

    public R2ImageService(
            @Value("${r2.access-key}") String accessKey,
            @Value("${r2.secret-key}") String secretKey,
            @Value("${r2.endpoint}")   String endpoint,
            @Value("${r2.bucket}")     String bucket,
            @Value("${r2.public-url}") String publicUrl) {

        this.bucket    = bucket;
        this.publicUrl = publicUrl;
        log.info("R2 config — endpoint={} bucket={} accessKeyLen={} accessKeyPrefix={} secretKeyLen={}",
                endpoint, bucket, accessKey.length(),
                accessKey.substring(0, Math.min(4, accessKey.length())),
                secretKey.length());
        this.s3 = S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Override
    public String uploadImage(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (!"image/jpeg".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only JPG/JPEG images are allowed");
        }

        String filename = prefix + "_" + System.currentTimeMillis()
                + "-" + (1000 + new Random().nextInt(9000)) + ".jpg";

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(filename)
                        .contentType("image/jpeg")
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        return publicUrl + "/" + filename;
    }
}
