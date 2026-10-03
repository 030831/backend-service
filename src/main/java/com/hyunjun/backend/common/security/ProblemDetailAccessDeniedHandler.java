package com.hyunjun.backend.common.security;

import com.hyunjun.backend.common.exception.ProblemDetailWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 했을 때 권한이 없을 때와 CSRF 토큰이 틀렸을 때 Security가 부르는 곳
 */
@Component
@RequiredArgsConstructor
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailWriter writer;


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        String detail = exception instanceof CsrfException
                ? "CSRF 토큰이 없거나 올바르지 않습니다."
                : "권한이 없습니다.";

        writer.write(request, response, HttpStatus.FORBIDDEN, detail);
    }
}
