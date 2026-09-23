package com.hyunjun.backend.store.controller;

import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.store.dto.StoreResponse;
import com.hyunjun.backend.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/me")
    public StoreResponse myStore(@AuthenticationPrincipal LoginPrincipal principal) {
        return storeService.getMyStore(principal.id());
    }

}
