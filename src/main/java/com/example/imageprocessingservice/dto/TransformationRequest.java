/*
 * TransformationRequest.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * TransformationRequest.java
 *
 * @author Nguyen
 */
@Data
public class TransformationRequest {

    private Map<String, Object> transformations;

    @Data
    public static class Resize {
        private Integer width;
        private Integer height;
        private Boolean maintainAspectRatio = true;
    }

    @Data
    public static class Crop {
        @NotNull
        private Integer width;
        @NotNull
        private Integer height;
        private Integer x;
        private Integer y;
    }

    @Data
    public static class Rotate {
        @Min(0)
        @Max(360)
        private Double angle;
    }

    @Data
    public static class Filter {
        private Boolean grayscale;
        private Boolean sepia;
        private Double brightness;
        private Double contrast;
    }

    @Data
    public static class Watermark {
        @NotBlank
        private String text;
        private Integer fontSize;
        private String color;
        private String position;
    }
}
