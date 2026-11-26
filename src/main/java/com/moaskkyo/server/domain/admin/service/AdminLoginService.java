package com.moaskkyo.server.domain.admin.service;

import com.moaskkyo.server.domain.admin.domain.Admin;
import com.moaskkyo.server.domain.admin.domain.repository.AdminRepository;
import com.moaskkyo.server.domain.admin.presentation.dto.request.AdminLoginRequest;
import com.moaskkyo.server.domain.admin.presentation.dto.response.AdminLoginResponse;
import com.moaskkyo.server.global.auth.IssuedTokens;
import com.moaskkyo.server.global.auth.JwtTokenProvider;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AdminLoginResponse execute(AdminLoginRequest request) {
        String phoneNumber = request.getPhoneNumber().trim().replace("-", "");
        Admin admin = adminRepository.findWithSchoolByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        IssuedTokens tokens = jwtTokenProvider.issue(admin.getAdminId(), Role.ADMIN);
        return AdminLoginResponse.of(admin, tokens);
    }
}
