package com.hyunjun.backend.account.controller;

import com.hyunjun.backend.account.dto.AccountResponse;
import com.hyunjun.backend.account.dto.LoginRequest;
import com.hyunjun.backend.account.security.AccountAuthenticationProvider;
import com.hyunjun.backend.account.service.AccountService;
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
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AccountAuthenticationProvider accountAuthenticationProvider;
    private final SessionLogin sessionLogin;
    private final AccountService accountService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authenticated = accountAuthenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.email(), loginRequest.password()
                )
        );

        sessionLogin.login(authenticated, request, response);
    }

    @GetMapping("/me")
    public AccountResponse me(@AuthenticationPrincipal LoginPrincipal principal) {
        return accountService.getAccount(principal.id());
    }
}
