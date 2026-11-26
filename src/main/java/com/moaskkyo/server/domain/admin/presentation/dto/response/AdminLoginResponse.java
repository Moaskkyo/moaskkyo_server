package com.moaskkyo.server.domain.admin.presentation.dto.response;

import com.moaskkyo.server.domain.admin.domain.Admin;
import com.moaskkyo.server.global.auth.IssuedTokens;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AdminLoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresInSeconds;
    private final String refreshToken;
    private final long refreshExpiresInSeconds;
    private final Long adminId;
    private final String name;
    private final String schoolName;

    public static AdminLoginResponse of(Admin admin, IssuedTokens tokens) {
        return AdminLoginResponse.builder()
                .accessToken(tokens.accessToken())
                .tokenType(IssuedTokens.TOKEN_TYPE)
                .expiresInSeconds(tokens.expiresInSeconds())
                .refreshToken(tokens.refreshToken())
                .refreshExpiresInSeconds(tokens.refreshExpiresInSeconds())
                .adminId(admin.getAdminId())
                .name(admin.getName())
                .schoolName(admin.getSchool().getName())
                .build();
    }
}
