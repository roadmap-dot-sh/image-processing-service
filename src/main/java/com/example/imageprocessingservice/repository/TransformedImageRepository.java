/*
 * TransformedImageRepository.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.repository;

import com.example.imageprocessingservice.model.Image;
import com.example.imageprocessingservice.model.Transformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TransformedImageRepository.java
 *
 * @author Nguyen
 */
@Repository
public interface TransformedImageRepository extends JpaRepository<Transformation, String> {
    Optional<Transformation> findByOriginalImageAndTransformationHash(Image originalImage, String transformationHash);
}
