package com.hyunjun.backend.account.controller;

import com.hyunjun.backend.account.dto.AccountLoginRequest;
import com.hyunjun.backend.account.security.AccountPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    private final SessionAuthenticationStrategy sessionAuthenticationStrategy =
            new CompositeSessionAuthenticationStrategy(List.of(
                    new ChangeSessionIdAuthenticationStrategy(),
                    new CsrfAuthenticationStrategy(
                            new HttpSessionCsrfTokenRepository()
                    )
            ));

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @GetMapping("/me")
    public Long me(@AuthenticationPrincipal AccountPrincipal principal) {
        return principal.getAccountId();
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(
            @Valid @RequestBody AccountLoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
            ) {

        UsernamePasswordAuthenticationToken loginToken = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(loginToken);

        sessionAuthenticationStrategy.onAuthentication(
                authentication, request, response
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleAuthenticationException() {

    }
}
