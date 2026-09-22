package com.hyunjun.backend.admin.bootstrap;

import com.hyunjun.backend.admin.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrap implements ApplicationRunner {

    private final AdminBootstrapProperties properties;
    private final AdminAccountService adminAccountService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isConfigured()) {
            return;
        }

        try {
            boolean created = adminAccountService.createIfNoneExists(
                    properties.email(), properties.name(), properties.password()
            );

            if (created) {
                log.info("초기 관리자를 만들었습니다: {}", properties.email());
            }
        } catch (DataIntegrityViolationException exception) {
            log.info("초기 관리자가 이미 있습니다: {}", properties.email());
        }
    }
}
