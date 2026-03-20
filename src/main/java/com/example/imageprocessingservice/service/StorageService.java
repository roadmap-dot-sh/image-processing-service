/*
 * StorageService.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.net.URI;

/**
 * StorageService.java
 *
 * @author Nguyen
 */
@Service
@Slf4j
public class StorageService {
    private final S3Client s3Client;
    private final String bucketName;

    public StorageService(
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.endpoint:}") String endpoint
    ) {
        this.bucketName = bucketName;

        var credentials = AwsBasicCredentials.create(accessKey, secretKey);
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (!endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        this.s3Client = builder.build();

        // Ensure bucket exists
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (NoSuchBucketException e) {
            // Create bucket if it doesn't exist
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            log.info("Created bucket: {}", bucketName);
        }
    }

    public String uploadFile(byte[] data, String key, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(data));

            // Generate URL for the uploaded file
            String url = String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
            log.info("Uploaded file to: {}", url);

            return url;

        } catch (S3Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public byte[] downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();

        } catch (S3Exception e) {
            log.error("Failed to download file from S3", e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Deleted file: {}", key);

        } catch (S3Exception e) {
            log.error("Failed to delete file from S3", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
