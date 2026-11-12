package com.moaskkyo.server.global.auth;

public record IssuedTokens(
        String accessToken,
        long expiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds) {

    public static final String TOKEN_TYPE = "Bearer";
}
