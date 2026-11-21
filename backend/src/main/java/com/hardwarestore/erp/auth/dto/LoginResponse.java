package com.hardwarestore.erp.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String userId,
        String email,
        String displayName,
        String role
) {}