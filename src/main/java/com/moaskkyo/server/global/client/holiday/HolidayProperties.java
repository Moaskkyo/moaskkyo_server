package com.moaskkyo.server.global.client.holiday;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "holiday")
public record HolidayProperties(String serviceKey) {
}
