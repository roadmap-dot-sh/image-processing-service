/*
 * ImageService.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.service;

import com.example.imageprocessingservice.dto.*;
import com.example.imageprocessingservice.model.*;
import com.example.imageprocessingservice.repository.ImageRepository;
import com.example.imageprocessingservice.repository.TransformedImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ImageService.java
 *
 * @author Nguyen
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final TransformedImageRepository transformedImageRepository;
    private final StorageService storageService;
    private final TransformationService transformationService;

    @Transactional
    public ImageResponse uploadImage(MultipartFile file, User user) {
        try {
            // Read image metadata
            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            // Generate unique storage key
            String storageKey = generateStorageKey(file.getOriginalFilename());

            // Upload original to storage
            String url = storageService.uploadFile(file.getBytes(), storageKey, file.getContentType());

            // Create image entity
            Image image = new Image();
            image.setOriginalFilename(file.getOriginalFilename());
            image.setStorageKey(storageKey);
            image.setUrl(url);
            image.setFileSize(file.getSize());
            image.setContentType(file.getContentType());
            image.setWidth(originalImage.getWidth());
            image.setHeight(originalImage.getHeight());
            image.setFormat(file.getContentType().split("/")[1]);
            image.setUser(user);

            // Extract and store metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalName", file.getOriginalFilename());
            metadata.put("uploadTimestamp", System.currentTimeMillis());
            image.setMetadata(metadata);

            Image savedImage = imageRepository.save(image);

            return mapToResponse(savedImage);

        } catch (IOException e) {
            log.error("Failed to upload image", e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Transactional
    @CacheEvict(value = "images", key = "#id")
    public ImageResponse transformImage(String id, Map<String, Object> transformations, User user) {
        Image originalImage = getImageByIdAndUser(id, user);

        // Generate hash for transformation to check cache
        String transformHash = generateTransformHash(transformations);

        // Check if transformed version already exists
        Transformation existing = transformedImageRepository
                .findByOriginalImageAndTransformationHash(originalImage, transformHash)
                .orElse(null);

        if (existing != null) {
            return mapToResponse(existing);
        }

        try {

            // Download original from storage
            byte[] originalData = storageService.downloadFile(originalImage.getStorageKey());

            // Apply transformations
            byte[] transformedData = transformationService.applyTransformations(
                    originalData, transformations, originalImage.getFormat());

            // Generate storage key for transformed image
            String transformedKey = generateStorageKey("transformed_" + id + "_" + transformHash + "." +
                    transformations.getOrDefault("format", originalImage.getFormat()));

            // Upload transformed image
            String transformedUrl = storageService.uploadFile(transformedData, transformedKey,
                    "image/" + transformations.getOrDefault("format", originalImage.getFormat()));

            // Save transformed image record
            Transformation transformed = new Transformation();
            transformed.setOriginalImage(originalImage);
            transformed.setStorageKey(transformedKey);
            transformed.setUrl(transformedUrl);
            transformed.setFileSize((long) transformedData.length);
            transformed.setTransformations(transformations);
            transformed.setTransformationHash(transformHash);

            // Get dimensions of transformed image
            BufferedImage transformedImage = ImageIO.read(new ByteArrayResource(transformedData).getInputStream());
            transformed.setWidth(transformedImage.getWidth());
            transformed.setHeight(transformedImage.getHeight());
            transformed.setFormat((String) transformations.getOrDefault("format", originalImage.getFormat()));

            Transformation saved = transformedImageRepository.save(transformed);

            return mapToResponse(saved);

        } catch (IOException e) {
            log.error("Failed to transform image", e);
            throw new RuntimeException("Failed to transform image", e);
        }
    }

    @Cacheable(value = "images", key = "#id + '-' + #transformations.hashCode()")
    public Resource getImage(String id, Map<String, String> transformations, User user) {
        Image originalImage = getImageByIdAndUser(id, user);

        if (transformations.isEmpty()) {
            // Return original image
            byte[] data = storageService.downloadFile(originalImage.getStorageKey());
            return new ByteArrayResource(data);
        } else {
            // Check for transformed version
            String transformHash = generateTransformHash(new HashMap<>(transformations));
            Transformation transformed = transformedImageRepository
                    .findByOriginalImageAndTransformationHash(originalImage, transformHash)
                    .orElseThrow(() -> new RuntimeException("Transformed image not found"));

            byte[] data = storageService.downloadFile(transformed.getStorageKey());
            return new ByteArrayResource(data);
        }
    }

    public Page<ImageResponse> listImages(User user, Pageable pageable) {
        return imageRepository.findByUser(user, pageable)
                .map(this::mapToResponse);
    }

    public ImageResponse getImageMetadata(String id, User user) {
        Image image = getImageByIdAndUser(id, user);
        return mapToResponse(image);
    }

    @Transactional
    @CacheEvict(value = "images", key = "#id")
    public void deleteImage(String id, User user) {
        Image image = getImageByIdAndUser(id, user);

        // Delete from storage
        storageService.deleteFile(image.getStorageKey());

        // Delete transformed versions
        image.getTransformations().forEach(t ->
                storageService.deleteFile(t.getStorageKey()));

        // Delete from database
        imageRepository.delete(image);
    }

    private Image getImageByIdAndUser(String id, User user) {
        return imageRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Image not found"));
    }

    private String generateStorageKey(String filename) {
        return UUID.randomUUID().toString() + "_" + filename;
    }

    private String generateTransformHash(Map<String, Object> transformations) {
        return Integer.toHexString(transformations.hashCode());
    }

    private ImageResponse mapToResponse(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .originalFilename(image.getOriginalFilename())
                .url(image.getUrl())
                .fileSize(image.getFileSize())
                .contentType(image.getContentType())
                .width(image.getWidth())
                .height(image.getHeight())
                .format(image.getFormat())
                .metadata(image.getMetadata())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private ImageResponse mapToResponse(Transformation transformed) {
        return ImageResponse.builder()
                .id(transformed.getId())
                .originalFilename(transformed.getOriginalImage().getOriginalFilename())
                .url(transformed.getUrl())
                .fileSize(transformed.getFileSize())
                .width(transformed.getWidth())
                .height(transformed.getHeight())
                .format(transformed.getFormat())
                .metadata(transformed.getTransformations())
                .createdAt(transformed.getCreatedAt())
                .build();
    }
}
