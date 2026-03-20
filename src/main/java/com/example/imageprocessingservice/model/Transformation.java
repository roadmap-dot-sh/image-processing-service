/*
 * Transformation.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformation.java
 *
 * @author Nguyen
 */
@Entity
@Table(name = "transformed_images")
@Data
@NoArgsConstructor
public class Transformation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_image_id", nullable = false)
    private Image originalImage;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String url;

    private Long fileSize;
    private Integer width;
    private Integer height;
    private String format;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> transformations;

    @Column(name = "transformation_hash")
    private String transformationHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
