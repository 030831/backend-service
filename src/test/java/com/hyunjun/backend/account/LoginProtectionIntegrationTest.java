package com.hyunjun.backend.account;

import com.hyunjun.backend.support.IntegrationTestSupport;
import com.hyunjun.backend.support.TestBrowser;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class LoginProtectionIntegrationTest extends IntegrationTestSupport {

    private static final String TEST_EMAIL = "kim@example.com";
    private static final String TEST_UPPERCASE_EMAIL = TEST_EMAIL.toUpperCase();
    private static final String TEST_PASSWORD = PASSWORD;
    private static final String TEST_WRONG_EMAIL = "wrong" + TEST_EMAIL;
    private static final String TEST_WRONG_PASSWORD = "wrong" + TEST_PASSWORD ;

    private HttpResponse<String> loginWith(
            TestBrowser browser,
            String email,
            String password
    ) {
        return browser.post("/auth/login", json(Map.of("email", email, "password", password)));
    }

    private void loginRepeat(TestBrowser browser, String email, String password, int count) {
        for (int i =0 ; i < count ; i++) {
            loginWith(browser, email, password);
        }
    }

    private TestBrowser lockLogin(int lockCount) {
        TestBrowser browser = newBrowser();
        HttpResponse<String> response = signUp(browser, TEST_EMAIL, "kim");
        assertThat(response.statusCode()).isEqualTo(201);
        loginRepeat(browser, TEST_EMAIL, TEST_WRONG_PASSWORD, lockCount);
        return browser;
    }

    @Test
    void 없는_이메일과_틀린_비밀번호의_401_응답_본문이_같다() {
        // given
        TestBrowser browser = newBrowser();
        signUp(browser, TEST_EMAIL, "kim");

        // when
        HttpResponse<String> firstResponse = loginWith(browser, TEST_WRONG_EMAIL, TEST_PASSWORD);
        HttpResponse<String> secondResponse = loginWith(browser, TEST_EMAIL, TEST_WRONG_PASSWORD);

        // then
        assertThat(firstResponse.statusCode()).isEqualTo(401);
        assertThat(secondResponse.statusCode()).isEqualTo(401);
        assertThat(body(firstResponse)).isEqualTo(body(secondResponse));
    }

    @Test
    void 비밀번호를_5번_틀리면_올바른_비밀번호로도_429를_받는다() {
        // given & when
        TestBrowser browser = lockLogin(5);
        HttpResponse<String> response = loginWith(browser, TEST_EMAIL, TEST_PASSWORD);

        // then
        assertThat(response.statusCode()).isEqualTo(429);
        assertThat(body(response).get("detail").asString()).isEqualTo("로그인 시도가 너무 많습니다. 잠시 후 다시 시도하세요.");
        assertThat(jdbcTemplate.queryForObject("SELECT failed_login_count FROM accounts WHERE email = ?", Integer.class, TEST_EMAIL))
                .isEqualTo(5);

        assertThat(jdbcTemplate.queryForObject("SELECT locked_until FROM accounts WHERE email = ?",  LocalDateTime.class  , TEST_EMAIL)).isNotNull();
    }

    @Test
    void 잠긴_동안의_실패는_세지_않는다() {
        // given
        TestBrowser browser = lockLogin(5);

        // when
        HttpResponse<String> response = loginWith(browser, TEST_EMAIL, TEST_WRONG_PASSWORD);

        // then
        assertThat(response.statusCode()).isEqualTo(429);
        assertThat(jdbcTemplate.queryForObject("SELECT failed_login_count FROM accounts WHERE email = ?", Integer.class, TEST_EMAIL))
                .isEqualTo(5);
    }

    @Test
    void 잠금_시간이_지나면_다시_로그인할_수_있고_실패_횟수가_0이_된다() {
        // given
        TestBrowser browser = lockLogin(5);
        jdbcTemplate.update("UPDATE accounts SET locked_until = UTC_TIMESTAMP(6) - INTERVAL 1 MINUTE " +
                "WHERE email = ?", TEST_EMAIL);

        // when
        HttpResponse<String> response = loginWith(browser, TEST_EMAIL, TEST_PASSWORD);

        // then
        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(jdbcTemplate.queryForObject("SELECT locked_until FROM accounts WHERE email = ?",  LocalDateTime.class  , TEST_EMAIL)).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT failed_login_count FROM accounts WHERE email = ?", Integer.class, TEST_EMAIL))
                .isEqualTo(0);
    }

    @Test
    void 잠금이_끝난_뒤_첫_실패는_1부터_다시_센다() {
        // given
        TestBrowser browser = lockLogin(5);
        jdbcTemplate.update("UPDATE accounts SET locked_until = UTC_TIMESTAMP(6) - INTERVAL 1 MINUTE " +
                "WHERE email = ?", TEST_EMAIL);

        // when
        loginWith(browser, TEST_EMAIL, TEST_WRONG_PASSWORD);

        // then
        assertThat(jdbcTemplate.queryForObject("SELECT failed_login_count FROM accounts WHERE email = ?", Integer.class, TEST_EMAIL))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT locked_until FROM accounts WHERE email = ?",  LocalDateTime.class  , TEST_EMAIL)).isNull();
    }

    @Test
    void 로그인에_성공하면_실패_횟수가_0으로_돌아간다() {
        // given
        TestBrowser browser = lockLogin(3);

        // when
        HttpResponse<String> response = loginWith(browser, TEST_EMAIL, TEST_PASSWORD);

        // then
        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(jdbcTemplate.queryForObject("SELECT failed_login_count FROM accounts WHERE email = ?", Integer.class, TEST_EMAIL))
                .isEqualTo(0);
    }

    @Test
    void 관리자도_5번_틀리면_잠긴다() {
        // given
        TestBrowser testBrowser = newBrowser();

        // when
        for (int i = 0 ; i < 5 ; i++) {
            testBrowser.post("/admin/auth/login", json(Map.of("email", ADMIN_EMAIL, "password", TEST_WRONG_PASSWORD)));
        }
        HttpResponse<String> response = testBrowser.post("/admin/auth/login", json(Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD)));

        // then
        assertThat(response.statusCode()).isEqualTo(429);
    }

    @Test
    void 대문자가_섞인_이메일로_가입하면_소문자로_저장된다() {
        // given
        TestBrowser testBrowser = newBrowser();
        // when
        HttpResponse<String> response = signUp(testBrowser, TEST_UPPERCASE_EMAIL, "kim");

        // then
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(jdbcTemplate.queryForObject("SELECT email FROM accounts WHERE email = ?", String.class, TEST_UPPERCASE_EMAIL))
                .isEqualTo(TEST_UPPERCASE_EMAIL.toLowerCase());
    }

    @Test
    void 대소문자만_다른_이메일로_다시_가입하면_409를_받는다() {
        // given
        TestBrowser firstTestBrowser = newBrowser();
        signUp(firstTestBrowser, TEST_EMAIL, "firstKim");

        // when
        TestBrowser secondTestBrowser = newBrowser();
        HttpResponse<String> response = signUp(secondTestBrowser, TEST_UPPERCASE_EMAIL, "secondKim");

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(body(response).get("detail").asString()).isEqualTo("이미 사용 중인 이메일입니다.");
    }

    @Test
    void 대소문자만_다른_이메일로_로그인된다() {
        // given
        TestBrowser testBrowser = newBrowser();
        signUp(testBrowser, TEST_EMAIL, "kim");

        // when
        HttpResponse<String> response = login(testBrowser, TEST_UPPERCASE_EMAIL);

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }
}
