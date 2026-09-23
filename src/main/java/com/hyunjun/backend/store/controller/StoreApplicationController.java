package com.hyunjun.backend.store.controller;

import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.store.dto.StoreApplicationRequest;
import com.hyunjun.backend.store.dto.StoreApplicationResponse;
import com.hyunjun.backend.store.service.StoreApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store-applications")
public class StoreApplicationController {

    private final StoreApplicationService storeApplicationService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreApplicationResponse register(@Valid @RequestBody StoreApplicationRequest request, @AuthenticationPrincipal LoginPrincipal principal) {
        return storeApplicationService.submit(principal.id(), request.storeName());
    }

    @GetMapping("/me")
    public List<StoreApplicationResponse> myApplications(@AuthenticationPrincipal LoginPrincipal principal) {
        return storeApplicationService.findMyApplications(principal.id());
    }
}
