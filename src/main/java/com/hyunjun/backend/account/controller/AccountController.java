package com.hyunjun.backend.account.controller;

import com.hyunjun.backend.account.dto.AccountRegisterRequest;
import com.hyunjun.backend.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long register(@Valid @RequestBody AccountRegisterRequest request) {
        return accountService.register(request.getEmail(), request.getNickname(), request.getPassword());
    }
}
