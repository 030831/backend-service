package com.hyunjun.backend.account.security;

import com.hyunjun.backend.account.repository.AccountRepository;
import com.hyunjun.backend.account.service.AccountService;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AccountAuthenticationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @BeforeEach
    @AfterEach
    void 테스트_데이터_정리() {
        accountRepository.deleteAll();
    }

    @Test
    void 비밀번호가_다르면_인증에_실패한다() {
        // given
        // 실제 가입 로직으로 비밀번호 해시가 저장된 계정을 만든다.
        accountService.register(
                "test@example.com", "test", "correct-password"
        );

        // 올바른 비밀번호를 담은 인증 객체를 만든다.
        UsernamePasswordAuthenticationToken correctRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                "test@example.com", "correct-password"
        );

        // 실제 계정 조회와 비밀번호 비교를 실행한다.
        Authentication authenticated = authenticationManager.authenticate(correctRequest);

        // 정상 계정으로 인증할 수 있는 구성인지 먼저 확인한다.
        assertThat(authenticated.isAuthenticated()).isTrue();

        // 이메일은 유지하고 비밀번호만 변경한다.
        UsernamePasswordAuthenticationToken wrongRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                "test@example.com", "wrong-password"
        );

        // when & then
        // 틀린 비밀번호로 인증하면 비밀번호 불일치 예외가 발생해야 한다.
        assertThatThrownBy(() ->
                authenticationManager.authenticate(wrongRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void 로그인하면_인증정보가_세션에_저장된다() throws Exception {
        // given
        // 실제 가입 로직으로 계정을 저장하고, 생성된 계정ID를 반환한다.
        Long accountId = accountService.register(
                "test@example.com", "test", "correct-password"
        );

        // 브라우저에서 보낼 로그인 요청 본문을 준비한다.
        String loginRequest = """
                {
                    "email": "test@example.com",
                    "password": "correct-password"
                }
                """;

        // 실제 토큰 제공 API를 호출한다.
        MvcResult csrfResult = mockMvc.perform(
                        get("/auth/csrf")
                )
                .andReturn();

        // 로그인하지 않은 상태에서도 토큰을 받을 수 있는지 확인한다.
        assertThat(csrfResult.getResponse().getStatus()).isEqualTo(200);

        // 서버가 반환한 JSON 응답을 문자열로 가져온다.
        String csrfResponse = csrfResult.getResponse().getContentAsString();

        // JSON에서 토큰을 보낼 헤더 이름과 토큰 값을 각각 꺼낸다.
        String headerName = JsonPath.read(csrfResponse, "$.headerName");
        String token = JsonPath.read(csrfResponse, "$.token");

        // 토큰이 발급된 세션을 가져온다. 로그인할 때도 같은 세션을 사용한다.
        MockHttpSession csrfSession = (MockHttpSession) csrfResult.getRequest().getSession(false);

        // 서버가 사용할 수 있는 토큰과 세션을 준비했는지 확인한다.
        assertThat(headerName).isNotBlank();
        assertThat(token).isNotBlank();
        assertThat(csrfSession).isNotNull();

        // when
        // 유효한 CSRF 토큰과 함께 실제 로그인 API를 호출한다.
        // 계정 조회와 비밀번호 검사는 실제 서비스와 DB를 사용한다.
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .session(csrfSession)
                                .header(headerName, token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andReturn();


        // then
        // 로그인 성공 응답인 204가 반환됐는지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(204);

        // 로그인 처리 중 만들어진 세션을 가져온다.
        // false는 세션이 없더라도 테스트가 새로 만들지 말라는 의미
        HttpSession session = result.getRequest().getSession(false);

        // 실제 로그 처리에서 세션을 만들었는지 확인한다.
        assertThat(session).isNotNull();

        // Spring Security가 정해둔 이름으로 세션에 저장할 로그인 정보를 꺼낸다.
        // getAttribute의 반환 타입이 Object이므로 SecurityContext로 형변환한다.
        SecurityContext context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );

        // 세션만 생성하고 로그인 정보는 저장하지 않는 경우를 잡는다.
        assertThat(context).isNotNull();

        // 세션에 저장된 인증 결과를 가져온다.
        Authentication authentication = context.getAuthentication();

        // 인증 결과가 존재하고, 인증에 성공한 상태인지 확인한다.
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();

        // 로그인한 계정 정보를 우리가 만든 AccountPrincipal로 꺼낸다.
        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();

        // 처음 가입한 계정과 세션에 저장된 계정이 같은지 확인한다.
        assertThat(principal.getAccountId()).isEqualTo(accountId);

        // 로그인할 때 사용한 세션으로 새로운 요청을 보낸다.
        // 브라우저가 로그인 후 같은 세션의 쿠키를 보내는 상황을 재현한다.
        MvcResult meResult = mockMvc.perform(
                get("/auth/me")
                        .session((MockHttpSession) session)
        ).andReturn();

        // 다음 요청에서도 로그인 상태가 인정됐는지 확인한다.
        assertThat(meResult.getResponse().getStatus()).isEqualTo(200);

        // 서버가 현재 로그인한 계정의 ID를 반환했는지 확인한다.
        assertThat(meResult.getResponse().getContentAsString())
                .isEqualTo(accountId.toString());
    }

    @Test
    void 로그아웃하면_로그인_세션이_무효화된() throws Exception {
        // given
        // 실제 가입 로직으로 로그인할 계정을 준비한다.
        accountService.register(
                "test@example.com", "test", "correct-password"
        );

        // 로그인 요청에 보낼 이메일과 비밀번호를 준비한다.
        String loginRequest = """
                {
                    "email": "test@example.com",
                    "password": "correct-password"
                }
                """;

        // 실제 로그인 API를 호출한다.
        MvcResult loginResult = mockMvc.perform(
                post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest)
        ).andReturn();

        // 로그인에 성공한 상태에서 로그아웃을 검증하도록 확인하다.
        assertThat(loginResult.getResponse().getStatus()).isEqualTo(204);

        // 로그인 처리에서 만들어진 세션을 가져온다.
        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        // 로그아웃할 세션이 실제로 존재하는지 확인한다.
        assertThat(session).isNotNull();

        // when
        // 로그인한 세션과 유효한 CSRF 토큰을 담아 로그아웃을 요청한다.
        MvcResult logoutResult = mockMvc.perform(
                post("/auth/logout")
                        .session(session)
                        .with(csrf())
        ).andReturn();

        // then
        // 설정한 로그아웃 성공 응답이 반환되는지 확인한다.
        assertThat(logoutResult.getResponse().getStatus()).isEqualTo(204);

        // 성공 응답만 보내고 세션을 남겨두는 오류를 잡는다.
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void 비밀번호가_틀리면_로그인_API_401을_반환한다() throws Exception {
        // given
        // 올바른 비밀번호로 실제 계정을 저장한다.
        accountService.register(
                "test@example.com", "test", "correct-password"
        );

        // 이메일만 같게 두고, 비밀번호만 틀리게 설정한다.
        String loginRequest = """
                {
                    "email": "test@example.com",
                    "password": "wrong-password"
                }
                """;

        // when
        // 실제 로그인 API에 틀린 비밀번호를 보낸다.
        // CSRF 검사 때문에 차단되지 않도록 유효한 토큰을 넣는다.
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andReturn();

        // then
        // 비밀번호 불일치가 로그인 실패 응답인 401로 반환됐는지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void 로그인하지_않고_내_정보를_조회하면_401를_반환한다() throws Exception {
        // given
        // 로그인하지 않은 상황이므로 계정 생성이나 로그인 세션을 준비하지 않는다.

        // when
        // 로그인 세션 없이 내 정보 조회 API를 호출한다.
        MvcResult result = mockMvc.perform(
                        get("/auth/me")
                )
                .andReturn();

        // then
        // 로그인하지 않은 요청을 401로 거절하는지 확인한다.
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }
}
