package com.hyunjun.backend.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;


class GlobalExceptionHandlerTest {

    @Test
    void 처리하지_못한_예외는_500_ProblemDetail로_바뀌고_내부_메시지는_숨긴다() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalStateException exception = new IllegalStateException("내부 오류");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/accounts");

        // when
        ProblemDetail problemDetail = handler.handleUnexpected(exception, request);

        // then
        assertThat(problemDetail.getStatus()).isEqualTo(500);
        assertThat(problemDetail.getDetail()).isEqualTo("서버 오류가 발생했습니다.");
    }
}