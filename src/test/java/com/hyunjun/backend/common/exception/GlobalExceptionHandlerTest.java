package com.hyunjun.backend.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;


class GlobalExceptionHandlerTest {

    @Test
    void 엔티티_검사_예외는_400_ProblemDetail로_바뀐다() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalArgumentException exception = new IllegalArgumentException("이메일은(는) 비어 있지 않은 254자 이하여야 합니다.");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/accounts");

        // when
        ProblemDetail problemDetail = handler.handleIllegalArgument(exception, request);

        // then
        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getDetail()).isEqualTo("요청 값이 올바르지 않습니다.");
    }
}