package com.moaskkyo.server.global.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long accessExpirationSeconds, long refreshExpirationSeconds) {
}
