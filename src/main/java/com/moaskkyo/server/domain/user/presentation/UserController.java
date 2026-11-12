package com.moaskkyo.server.domain.user.presentation;

import com.moaskkyo.server.domain.user.presentation.dto.request.VerifyInviteCodeRequest;
import com.moaskkyo.server.domain.user.presentation.dto.response.VerifyInviteCodeResponse;
import com.moaskkyo.server.domain.user.service.VerifyInviteCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final VerifyInviteCodeService verifyInviteCodeService;

    @PostMapping("/invite/verify")
    public ResponseEntity<VerifyInviteCodeResponse> verifyInviteCode(@RequestBody VerifyInviteCodeRequest request) {
        return ResponseEntity.ok(verifyInviteCodeService.execute(request));
    }
}
