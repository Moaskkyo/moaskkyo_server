package com.moaskkyo.server.domain.admin.presentation;

import com.moaskkyo.server.domain.admin.presentation.dto.request.AdminLoginRequest;
import com.moaskkyo.server.domain.admin.presentation.dto.response.AdminLoginResponse;
import com.moaskkyo.server.domain.admin.service.AdminLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminLoginService adminLoginService;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminLoginService.execute(request));
    }
}
