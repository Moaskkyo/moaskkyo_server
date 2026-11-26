package com.moaskkyo.server.global.auth;

import java.nio.charset.StandardCharsets;

public final class PasswordLimit {

    public static final int MAX_BYTES = 72;

    private PasswordLimit() {
    }

    public static boolean exceeds(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}
