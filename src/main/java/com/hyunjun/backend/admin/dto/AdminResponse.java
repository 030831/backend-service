package com.hyunjun.backend.admin.dto;

import com.hyunjun.backend.admin.domain.AdminAccount;

public record AdminResponse(Long id, String email, String name) {

    public static AdminResponse from(AdminAccount adminAccount) {
        return new AdminResponse(adminAccount.getId(), adminAccount.getEmail(), adminAccount.getName());
    }
}
