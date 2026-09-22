package com.hyunjun.backend.admin.controller;

import com.hyunjun.backend.admin.dto.AdminLoginRequest;
import com.hyunjun.backend.admin.dto.AdminResponse;
import com.hyunjun.backend.admin.security.AdminAuthenticationProvider;
import com.hyunjun.backend.admin.service.AdminAccountService;
import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.common.security.SessionLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthenticationProvider adminAuthenticationProvider;
    private final SessionLogin sessionLogin;
    private final AdminAccountService adminAccountService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(@Valid @RequestBody AdminLoginRequest loginRequest,
                      HttpServletRequest request,
                      HttpServletResponse response
    ) {

        Authentication authenticated = adminAuthenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.email(), loginRequest.password()
                )
        );

        sessionLogin.login(authenticated, request, response);
    }

    @GetMapping("/me")
    public AdminResponse me(@AuthenticationPrincipal LoginPrincipal principal) {
        return adminAccountService.getAdmin(principal.id());
    }
}
