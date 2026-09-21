package com.hyunjun.backend.account.controller;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.exception.DuplicateAccountException;
import com.hyunjun.backend.account.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @MockitoSpyBean
    PasswordEncoder passwordEncoder;

    @BeforeEach
    @AfterEach
    void 테스트_데이터_정리() {
        accountRepository.deleteAll();
    }

    @Test
    void 가입하면_계정과_비밀번호_해시가_저장된다() throws Exception {
        // given
        // 가입요청에 사용할 JSON
        String request = """
                {
                    "email": "test@example.com",
                    "nickname": "test",
                    "password": "password"
                }
                """;

        // when
        // CSRF 토큰과 함께 가입 요청을 보내고 결과를 받는다.
        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andReturn();


        // then
        // 가입 성공 응답인지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(201);

        // 응답으로 받은 ID를 숫자로 변환한다.
        Long accountId = Long.valueOf(
                result.getResponse().getContentAsString()
        );

        // 응답 ID에 해당하는 계정을 DB에 조회한다.
        Account account = accountRepository.findById(accountId)
                .orElseThrow();

        // 게정이 정확히 하나 생성되었는지 확인한.
        assertThat(accountRepository.count()).isEqualTo(1L);

        // 입력한 이메일과 닉네임이 저장되었는지 확인한다.
        assertThat(accountRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(accountRepository.existsByNickname("test")).isTrue();

        // 비밀번호를 평문으로 저장하지 않았는지 확인한다.
        assertThat(account.getPasswordHash()).isNotEqualTo("password");

        // 저장된 해시가 입력한 비밀번호와 일치하는지 확인한다.
        assertThat(passwordEncoder.matches(
                "password", account.getPasswordHash()
        )).isTrue();
    }

    @Test
    void 이메일_형식이_잘못되면_가입되지_않는다() throws Exception {
        // given
        // 이메일 형식만 잘못된 가입 요청을 준비한다.
        String request = """
                {
                    "email": "Invalid-email",
                    "nickname": "test",
                    "password": "password"
                }
                """;

        // when
        // 가입요청을 실행하고 결과를 보관한다.
        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andReturn();

        // then
        // 잘못된 입력에 대한 400 응답인지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(400);

        // 요청이 거부되어 DB에 계정이 남지 않았는지 확인한다.
        assertThat(accountRepository.count()).isZero();
    }

    @Test
    void 이메일_중복되면_추가로_가입되지_않는다() throws Exception {
        // given
        // 먼저 가입할 계정 정보를 준비한다.
        String firstRequest = """
                {
                    "email": "test@example.com",
                    "nickname": "first",
                    "password": "password"
                }
                """;

        // 실제 가입 요청으로 기존 계정을 만들고 성공 여부를 확인한다.
        mockMvc.perform(
                post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest)
        ).andExpect(status().isCreated());

        // 이메일은 같고 닉네임이 다른 두 번째 요청을 준비한다.
        String duplicateRequest = """
                {
                    "email": "test@example.com",
                    "nickname": "second",
                    "password": "password"
                }
                """;

        // when
        // 같은 이메일로 다시 가입을 요청한다.
        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(duplicateRequest)
                )
                .andReturn();

        // then
        // 실제 중복 검사에서 거부되어 409 반환을 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(409);

        // 첫 번째 계정만 남아있는지 확인한다.
        assertThat(accountRepository.count()).isEqualTo(1L);

        // 두 번째 요청의 닉네으로 계정이 저장되지 않았는지 확인한다.
        assertThat(accountRepository.existsByNickname("second")).isFalse();
    }

    @Test
    void 닉네임이_중복되면_추가로_가입되지_않는다() throws Exception {
        // given
        // 첫 번째 계정의 가입 정보
        String firstRequest = """
                {
                    "email": "first@example.com",
                    "nickname": "same",
                    "password": "password"
                }
                """;

        // 실제 가입 요청으로 기존 계정을 만들고 성공 여부를 확인한다.
        mockMvc.perform(
                post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest)
        ).andExpect(status().isCreated());

        // 이메일은 다르고 닉네임은 같게 한다.
        String duplicateRequest = """
                {
                    "email": "second@example.com",
                    "nickname": "same",
                    "password": "password"
                }
                """;

        // when
        // 같은 닉네임으로 다시 요청한다.
        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(duplicateRequest)
                )
                .andReturn();

        // then
        // 중복 가입이 409 응답으로 거부됐는지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(409);

        // 첫 번째 계정 하나만 남아 있는지 확인한다.
        assertThat(accountRepository.count()).isEqualTo(1L);

        // 두 번째 이메일로 계정이 생성되지 않았는지 확인한다.
        assertThat(accountRepository.existsByEmail("second@example.com")).isFalse();
    }

    @Test
    void 비밀번호가_공백이면_가입되지_않는다() throws Exception {
        // given
        // 이메일과 닉네임은 정상이고, 비밀번호만 공백인 요청을 준비한다.
        String request = """
                {
                    "email": "test@example.com",
                    "nickname": "test",
                    "password": "  "
                }
                """;

        // when
        // 공백 비밀번호로 가입을 요청한다.
        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andReturn();

        // then
        // 잘못된 입력으로 거부되어 400을 반환하는지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(400);

        // 계정이 저장되지 않았는지 확인한다.
        assertThat(accountRepository.count()).isZero();
    }

    @Test
    void 같은_이메일로_동시에_가입하면_하나만_저장된다() throws Exception {
        // given
        // 두 요청이 모두 도착해야 다음 단계로 진행하는 대기 지점이다.
        CyclicBarrier barrier = new CyclicBarrier(2);

        // 실제 해시를 만든 뒤, 두 요청 모두 중복 검사를 통과할 때까지 기다린다.
        Mockito.doAnswer(invocation -> {
            String hash = (String) invocation.callRealMethod();
            barrier.await(30, TimeUnit.SECONDS);
            return hash;
        }).when(passwordEncoder).encode(Mockito.any(CharSequence.class));

        // 이메일은 같고 닉네임만 다른 요청 두 개를 만들기 위한 양식
        String requestTemplate = """
                {
                    "email": "race@example.com",
                    "nickname": "%s",
                    "password": "password"
                }
                """;

        // 두 요청을 각각 실행할 작업 스레드를 준비한다.
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // when
            // 첫 번째 가입 요청을 별도의 스레드에서 실행한다.
            Future<MvcResult> first = executor.submit(() ->
                    mockMvc.perform(
                            post("/accounts")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestTemplate.formatted("first"))
                    ).andReturn());

            // 첫 번째 요청의 완료를 기다리지 않고 두 번째 요청도 실행한다.
            Future<MvcResult> second = executor.submit(() ->
                    mockMvc.perform(
                            post("/accounts")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestTemplate.formatted("second"))
                    ).andReturn());

            // 두 요청 처리가 끝나면 각각의 결과를 얻는다.
            MvcResult firstResult = first.get(45, TimeUnit.SECONDS);
            MvcResult secondResult = second.get(45, TimeUnit.SECONDS);

            // then
            // 어느 요청이 먼저 성공했든, 성공 하나와, 중복 거부 하나이어야 한다.
            assertThat(List.of(
                    firstResult.getResponse().getStatus(),
                    secondResult.getResponse().getStatus()
            )).containsExactlyInAnyOrder(201, 409);

            // 실제 DB에는 계정 하나만 남아야 한다.
            assertThat(accountRepository.count()).isEqualTo(1L);
            assertThat(accountRepository.existsByEmail("race@example.com")).isTrue();

            // 409를 반환한 요청의 처리 결과를 선택한다.
            MvcResult rejectedResult = firstResult;
            if (firstResult.getResponse().getStatus() != 409) {
                rejectedResult = secondResult;
            }

            // 사전 중복 검사가 아니라 실제 DB 오류를 변환한 경로인지 확인한다.
            assertThat(rejectedResult.getResolvedException())
                    .isInstanceOf(DuplicateAccountException.class)
                    .hasCauseInstanceOf(DataIntegrityViolationException.class);
        } finally {
            // 테스트가 실패하더라도 작업 스레드를 종료한다.
            executor.shutdownNow();

            // 데이터 정리 전에 작업이 종료되었는지 확인한다.
            assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        }
    }
}