package com.moaskkyo.server.domain.user.presentation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class VerifyInviteCodeResponse {

    private final String schoolName;
}
