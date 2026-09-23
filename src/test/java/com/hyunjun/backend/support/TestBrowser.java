package com.hyunjun.backend.support;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 실제 HTTP로 서버에 요청하는 테스트용 브라우저.
 * 브라우저처럼 받은 쿠키(JSESSIONID, XSRF-TOKEN)를 저장했다가 다음 요청에 다시 보낸다.
 * 한 객체가 한 사람(한 브라우저)이다. 고객과 관리자를 동시에 쓰려면 객체를 둘 만든다.
 */
public class TestBrowser {

    private static final String CSRF_COOKIE = "XSRF-TOKEN";
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    private final String baseUrl;
    private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient client;

    public TestBrowser(int port) {
        // localhost는 점이 없어 JDK 쿠키 저장소가 도메인을 바꿔 저장하므로 127.0.0.1을 쓴다.
        this.baseUrl = "http://127.0.0.1:" + port;
        this.client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public HttpResponse<String> get(String path) {
        return send(request(path).GET().build());
    }

    /**
     * 쿠키에 있는 CSRF 토큰을 헤더에 담아 POST한다. 프런트 라이브러리가 하는 일과 같다.
     * 로그인 직후처럼 토큰 쿠키가 비어 있으면 GET을 한 번 보내 새 토큰을 먼저 받는다.
     */
    public HttpResponse<String> post(String path, String json) {
        if (csrfToken() == null) {
            get("/auth/me");
        }

        return postWithCsrfToken(path, json, csrfToken());
    }

    public HttpResponse<String> postWithCsrfToken(String path, String json, String token) {
        return send(request(path)
                .header("Content-Type", "application/json")
                .header(CSRF_HEADER, token)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build());
    }

    public HttpResponse<String> postWithoutCsrfToken(String path, String json) {
        return send(request(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build());
    }

    /** 쿠키 저장소에 있는 XSRF-TOKEN 값. 없으면 null. */
    public String csrfToken() {
        return cookieManager.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals(CSRF_COOKIE))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10));
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new IllegalStateException(request.uri() + " 요청 실패", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(request.uri() + " 요청 중단", exception);
        }
    }
}
