package com.hyunjun.backend.seller.controller;

import com.hyunjun.backend.account.security.AccountPrincipal;
import com.hyunjun.backend.seller.dto.SellerRegistrationRequest;
import com.hyunjun.backend.seller.service.SellerRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller-registrations")
@RequiredArgsConstructor
public class SellerRegistrationController {

    private final SellerRegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long submit(
            @AuthenticationPrincipal AccountPrincipal principal,
            @Valid @RequestBody SellerRegistrationRequest request
    ) {
        return registrationService.submit(
                principal.getAccountId(),
                request.getStoreName()
        );
    }
}
