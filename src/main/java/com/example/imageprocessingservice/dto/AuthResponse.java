/*
 * AuthResponse.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AuthResponse.java
 *
 * @author Nguyen
 */
@Data
@Builder
public class AuthResponse {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String token;
    private String tokenType;
}
