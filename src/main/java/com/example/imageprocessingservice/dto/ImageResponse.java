/*
 * ImageResponse.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ImageResponse.java
 *
 * @author Nguyen
 */
@Data
@Builder
public class ImageResponse {
    private String id;
    private String originalFilename;
    private String url;
    private Long fileSize;
    private String contentType;
    private Integer width;
    private Integer height;
    private String format;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
