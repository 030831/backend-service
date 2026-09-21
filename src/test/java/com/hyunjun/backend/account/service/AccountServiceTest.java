package com.hyunjun.backend.account.service;

import com.hyunjun.backend.account.exception.DuplicateAccountException;
import com.hyunjun.backend.account.repository.AccountRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @MockitoSpyBean
    PasswordEncoder passwordEncoder;

    @AfterEach
    void 테스트_데이터_정리() {
        accountRepository.deleteAll();
    }

    @Test
    void 같은_이메일로_동시에_가입하면_한_건만_저장되고_나머지는_중복_예외가_난다() throws Exception {
        // given
        // 두 스레드가 모두 중복 검사를 통과한 뒤에야 저장 단계로 넘어가게 하는 대기 지점
        CyclicBarrier barrier = new CyclicBarrier(2);


        // 실제 해시를 만든 뒤, 다른 스레드로 여기까지 올 때까지 기다린다.
        Mockito.doAnswer(invocation -> {
            Object hash = invocation.callRealMethod();
            barrier.await(10, TimeUnit.SECONDS);
            return hash;
        }).when(passwordEncoder).encode(Mockito.any());

        // 두 가입을 동시에 실행할 스레드 두개
        ExecutorService executors = Executors.newFixedThreadPool(2);

        try {
            // when
            // 이메일은 같고 닉네임만 다른 가입을 두 스레드에서 동시에 실행한다.
            Future<Long> first = executors.submit(() ->
                    accountService.register("race@example.com", "first", "password1"));

            Future<Long> second = executors.submit(() ->
                    accountService.register("race@example.com", "second", "password1"));

            // 각 스레드의 결과를 기다리고, 실패한 쪽의 예외를 모은다.
            List<Throwable> failures = new ArrayList<>();
            for (Future<Long> future : List.of(first, second)) {
                try {
                    future.get(20, TimeUnit.SECONDS);
                } catch (ExecutionException exception) {
                    failures.add(exception.getCause());
                }
            }

            // then
            // DB에는 한 건만 저장되어야 한다.
            assertThat(accountRepository.count()).isEqualTo(1L);

            // 실패한 한 건은 UNIQUE 위반을 변환한 중복 예외여야 한다.
            assertThat(failures).hasSize(1);
            assertThat(failures.get(0))
                    .isInstanceOf(DuplicateAccountException.class)
                    .hasMessage("이미 사용 중인 이메일입니다.")
                    .hasCauseInstanceOf(DataIntegrityViolationException.class);

        } finally {
            // 테스트가 실패해도 스레드를 정리한다.
            executors.shutdownNow();
        }
    }
}