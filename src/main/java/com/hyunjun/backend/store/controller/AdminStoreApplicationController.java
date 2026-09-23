package com.hyunjun.backend.store.controller;

import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.store.dto.RejectRequest;
import com.hyunjun.backend.store.dto.StoreApplicationResponse;
import com.hyunjun.backend.store.dto.StoreResponse;
import com.hyunjun.backend.store.service.StoreApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/store-applications")
@RequiredArgsConstructor
public class AdminStoreApplicationController {

    private final StoreApplicationService storeApplicationService;

    @GetMapping
    public List<StoreApplicationResponse> findSubmitted() {
        return storeApplicationService.findSubmitted();
    }

    @PostMapping("/{applicationId}/approve")
    public StoreResponse approve(@PathVariable Long applicationId, @AuthenticationPrincipal LoginPrincipal principal) {
        return storeApplicationService.approve(applicationId, principal.id());
    }

    @PostMapping("/{applicationId}/reject")
    public StoreApplicationResponse reject(@PathVariable Long applicationId,
                                           @AuthenticationPrincipal LoginPrincipal principal,
                                           @Valid @RequestBody RejectRequest request) {
        return storeApplicationService.reject(applicationId, principal.id(), request.reason());
    }
}
