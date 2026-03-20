/*
 * RateLimitService.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimitService.java
 *
 * @author Nguyen
 */
@Service
public class RateLimitService {
    private final Map<String, Bucket> uploadBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> transformBuckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.uploads-per-minute}")
    private int uploadsPerMinute;

    @Value("${app.rate-limit.transformations-per-minute}")
    private int transformationsPerMinute;

    public void checkUploadLimit(String userId) {
        Bucket bucket = uploadBuckets.computeIfAbsent(userId, k -> createUploadBucket());

        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Rate limit exceeded for uploads. Please try again later.");
        }
    }

    public void checkTransformationLimit(String userId) {
        Bucket bucket = transformBuckets.computeIfAbsent(userId, k -> createTransformBucket());

        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Rate limit exceeded for transformations. Please try again later.");
        }
    }

    private Bucket createUploadBucket() {
        Bandwidth limit = Bandwidth.classic(uploadsPerMinute,
                Refill.greedy(uploadsPerMinute, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createTransformBucket() {
        Bandwidth limit = Bandwidth.classic(transformationsPerMinute,
                Refill.greedy(transformationsPerMinute, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}
