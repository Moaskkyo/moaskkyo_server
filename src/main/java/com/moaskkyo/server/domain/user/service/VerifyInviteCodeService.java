package com.moaskkyo.server.domain.user.service;

import com.moaskkyo.server.domain.school.domain.School;
import com.moaskkyo.server.domain.school.domain.repository.SchoolRepository;
import com.moaskkyo.server.domain.user.presentation.dto.request.VerifyInviteCodeRequest;
import com.moaskkyo.server.domain.user.presentation.dto.response.VerifyInviteCodeResponse;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyInviteCodeService {

    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public VerifyInviteCodeResponse execute(VerifyInviteCodeRequest request) {
        String inviteCode = request.getInviteCode() == null ? "" : request.getInviteCode().trim();
        if (inviteCode.length() != School.JOIN_CODE_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
        }
        School school = schoolRepository.findByJoinCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));
        return VerifyInviteCodeResponse.builder()
                .schoolName(school.getName())
                .build();
    }
}
