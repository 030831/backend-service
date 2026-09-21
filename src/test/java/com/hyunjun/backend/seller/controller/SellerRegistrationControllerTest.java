package com.hyunjun.backend.seller.controller;

import com.hyunjun.backend.account.repository.AccountRepository;
import com.hyunjun.backend.account.service.AccountService;
import com.hyunjun.backend.seller.domain.RegistrationStatus;
import com.hyunjun.backend.seller.domain.SellerRegistration;
import com.hyunjun.backend.seller.repository.SellerRegistrationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SellerRegistrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SellerRegistrationRepository registrationRepository;

    private Long createdAccountId;
    private Long createdRegistrationId;

    @AfterEach
    void 테스트_데이터_정리() {
        // 계정을 참조하는 신청서를 먼저 삭제한다.
        if (createdRegistrationId != null) {
            registrationRepository.deleteById(createdRegistrationId);
        }

        // 이번 테스트에서 만든 계정을 삭제한다.
        if (createdAccountId != null) {
            accountRepository.deleteById(createdAccountId);
        }
    }

    @Test
    void 로그인한_계정으로_입점_신청하면_심사중으로_저장된다() throws Exception {
        // given
        // 실제 가입 서비스로 신청할 계정을 저장한다.

        createdAccountId = accountService.register(
                "seller-test@example.com",
                "신청테스트",
                "password"
        );

        // 가입한 계정의 이메일과 비밀호로 로그인한다.
        MvcResult loginResult = mockMvc.perform(
                post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "seller-test@example.com",
                                    "password": "password"
                                }
                                """)
        ).andReturn();

        // 로그인 실패를 신청 기능의 오류로 오해하지 않도록 확인한다.
        assertThat(loginResult.getResponse().getStatus()).isEqualTo(204);

        // 로그인 API가 만든 세션을 가져온다.
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // 이후 요청에 사용할 로그인 세션이 있는지 확인한다.
        assertThat(session).isNotNull();

        // when
        // 로그인 세션과 CSRF 토큰을 담아 스토어 이름을 제출한다.
        MvcResult result = mockMvc.perform(
                post("/seller-registrations")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "storeName": "철수상점"
                                }
                                """)
        ).andReturn();

        // then
        // 신청 접수 성공 응답인지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(201);

        // 응답으로 받은 신청서 ID를 숫자로 변환한다.
        createdRegistrationId = Long.valueOf(result.getResponse().getContentAsString());

        // 해당 신청서가 실제 DB에 저장되었는지 다시 확인한다.
        SellerRegistration saved = registrationRepository.findById(createdRegistrationId)
                .orElseThrow();

        // 신청자가 실제 로그인한 계정인지 확인한다.
        assertThat(saved.getApplicant().getId()).isEqualTo(createdAccountId);

        // 제출한 이름과 초기 심사 상태가 저장되었는지 확인한다.
        assertThat(saved.getStoreName()).isEqualTo("철수상점");
        assertThat(saved.getStatus()).isEqualTo(RegistrationStatus.SUBMITTED);

        // 신청만 했으므로 심사자와 심사 시각은 없어야 한다.
        assertThat(saved.getReviewedBy()).isNull();
        assertThat(saved.getReviewedAt()).isNull();

    }
}