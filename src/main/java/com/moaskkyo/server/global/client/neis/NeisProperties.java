package com.moaskkyo.server.global.client.neis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "neis")
public record NeisProperties(String apiKey) {
}
