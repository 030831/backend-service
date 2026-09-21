package com.hyunjun.backend.account.controller;

import com.hyunjun.backend.account.dto.AccountRegisterRequest;
import com.hyunjun.backend.account.dto.AccountRegisterResponse;
import com.hyunjun.backend.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountRegisterResponse register(@Valid @RequestBody AccountRegisterRequest request) {
        Long accountId = accountService.register(request.email(), request.nickname(), request.password());

        return new AccountRegisterResponse(accountId);
    }
}
