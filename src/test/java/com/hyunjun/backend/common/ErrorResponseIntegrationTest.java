package com.hyunjun.backend.common;

import com.hyunjun.backend.support.IntegrationTestSupport;
import com.hyunjun.backend.support.TestBrowser;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ErrorResponseIntegrationTest extends IntegrationTestSupport {

    @Test
    void 로그인_안_하고_내_정보를_요청하면_401_ProblemDetail을_받는다() {
        // given
        TestBrowser testBrowser = newBrowser();

        // when
        HttpResponse<String> response = testBrowser.get("/auth/me");

        // then
        assertThat(response.headers().firstValue("Content-Type").orElse("")).isEqualTo("application/problem+json");
        assertThat(body(response).get("detail").asString()).isEqualTo("로그인이 필요합니다.");
        assertThat(body(response).get("instance").asString()).isEqualTo("/auth/me");
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void 고객_세션으로_관리자_API를_부르면_403_ProblemDetail을_받는다() {
        // given
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");


        // when
        HttpResponse<String> response = customer.get("/admin/store-applications");

        // then
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(body(response).get("detail").asString()).isEqualTo("권한이 없습니다.");
    }

    @Test
    void CSRF_토큰_없이_POST하면_403_ProblemDetail을_받고_저장되지_않는다() {
        // given
        TestBrowser testBrowser = newBrowser();

        // when
        HttpResponse<String> response = testBrowser.postWithoutCsrfToken("/accounts", json(Map.of("email", "kim@example.com", "nickname", "kim", "password", "password1")));

        // then
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(body(response).get("detail").asString()).isEqualTo("CSRF 토큰이 없거나 올바르지 않습니다.");

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts", Integer.class)).isEqualTo(0);
    }

    @Test
    void 로그인_전_토큰으로_로그인_뒤_POST하면_403을_받는다() {
        // given
        TestBrowser testBrowser = newBrowser();
        testBrowser.get("/auth/me");
        String csrfToken = testBrowser.csrfToken();
        signUp(testBrowser, "chulsoo@example.com", "철수");
        login(testBrowser, "chulsoo@example.com");

        // when
        HttpResponse<String> response = testBrowser.postWithCsrfToken("/store-applications", "{}", csrfToken);

        // then
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(body(response).get("detail").asString()).isEqualTo("CSRF 토큰이 없거나 올바르지 않습니다.");
    }

    @Test
    void 빈_이메일로_가입하면_400과_필드_오류_목록을_받는다() {
        // given
        TestBrowser testBrowser = newBrowser();

        // when
        HttpResponse<String> response = testBrowser.post("/accounts", json(Map.of("email", "", "nickname", "kim", "password", "password1")));

        // then
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(body(response).get("detail").asString()).isEqualTo("입력값이 올바르지 않습니다.");
        assertThat(body(response).get("errors").get(0).get("message").asString()).isEqualTo("이메일은 필수입니다.");
    }

    @Test
    void 관리자_세션으로_고객_API를_부르면_403_ProblemDetail을_받는다() {
        // given
        TestBrowser admin = loggedInAdmin();

        // when
        HttpResponse<String> response = admin.get("/auth/me");

        // then
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(body(response).get("detail").asString()).isEqualTo("권한이 없습니다.");
    }
}
