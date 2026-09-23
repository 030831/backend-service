package com.hyunjun.backend.support;

import com.hyunjun.backend.admin.bootstrap.AdminBootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 서버(임의 포트)와 MySQL 테스트 DB로 요청 흐름을 확인하는 통합 테스트의 공통 준비.
 * MockMvc와 달리 톰캣이 /error 재요청을 하고, 쿠키와 CSRF 토큰도 실제로 오간다.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "admin.bootstrap.email=" + IntegrationTestSupport.ADMIN_EMAIL,
                "admin.bootstrap.name=" + IntegrationTestSupport.ADMIN_NAME,
                "admin.bootstrap.password=" + IntegrationTestSupport.ADMIN_PASSWORD
        }
)
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    // 테스트 전용 관리자. 실제 관리자 비밀번호와 관계없는 테스트용 값이다.
    protected static final String ADMIN_EMAIL = "it-admin@example.com";
    protected static final String ADMIN_NAME = "테스트관리자";
    protected static final String ADMIN_PASSWORD = "it-admin-password1";

    protected static final String PASSWORD = "password1";

    private static final String TEST_DATABASE = "backend_service_test";

    protected final JsonMapper jsonMapper = JsonMapper.builder().build();

    @LocalServerPort
    protected int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminBootstrap adminBootstrap;

    @BeforeEach
    void 테스트_DB를_확인하고_비운_뒤_관리자를_만든다() throws Exception {
        // 행을 지우기 전에 지금 연결된 DB가 테스트 DB인지 확인한다.
        assertThat(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class))
                .isEqualTo(TEST_DATABASE);

        deleteAllRows();

        // 관리자 테이블이 비었으니 앱 시작 때와 같은 방법으로 테스트 관리자를 만든다.
        adminBootstrap.run(new DefaultApplicationArguments());
    }

    @AfterEach
    void 테스트_데이터를_정리한다() {
        deleteAllRows();
    }

    // 다른 테이블을 FK로 가리키는 쪽부터 지운다.
    private void deleteAllRows() {
        jdbcTemplate.update("DELETE FROM stores");
        jdbcTemplate.update("DELETE FROM store_applications");
        jdbcTemplate.update("DELETE FROM accounts");
        jdbcTemplate.update("DELETE FROM admin_accounts");
    }

    protected TestBrowser newBrowser() {
        return new TestBrowser(port);
    }

    protected HttpResponse<String> signUp(TestBrowser browser, String email, String nickname) {
        return browser.post("/accounts", json(Map.of(
                "email", email, "nickname", nickname, "password", PASSWORD)));
    }

    protected HttpResponse<String> login(TestBrowser browser, String email) {
        return browser.post("/auth/login", json(Map.of("email", email, "password", PASSWORD)));
    }

    /** 가입하고 로그인까지 마친 고객 브라우저를 돌려준다. */
    protected TestBrowser loggedInCustomer(String email, String nickname) {
        TestBrowser browser = newBrowser();
        assertThat(signUp(browser, email, nickname).statusCode()).isEqualTo(201);
        assertThat(login(browser, email).statusCode()).isEqualTo(204);
        return browser;
    }

    protected TestBrowser loggedInAdmin() {
        TestBrowser browser = newBrowser();
        HttpResponse<String> response = browser.post("/admin/auth/login",
                json(Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD)));
        assertThat(response.statusCode()).isEqualTo(204);
        return browser;
    }

    protected String json(Map<String, ?> body) {
        return jsonMapper.writeValueAsString(body);
    }

    protected JsonNode body(HttpResponse<String> response) {
        return jsonMapper.readTree(response.body());
    }
}
