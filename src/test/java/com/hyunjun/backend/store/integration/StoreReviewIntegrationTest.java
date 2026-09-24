package com.hyunjun.backend.store.integration;

import com.hyunjun.backend.support.IntegrationTestSupport;
import com.hyunjun.backend.support.TestBrowser;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class StoreReviewIntegrationTest extends IntegrationTestSupport {

    private Long submit(TestBrowser customer, String storeName) {
        HttpResponse<String> response =
                customer.post("/store-applications", json(Map.of("storeName", storeName)));

        assertThat(response.statusCode()).isEqualTo(201);

        return body(response).get("id").asLong();
    }

    @Test
    void 관리자가_승인하면_스토어가_생기고_고객이_내_스토어를_볼_수_있다() {
        // given
        // 고객이 '철수 상점' 으로 신청했고, 관리자가 로그인했다.
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");
        Long applicationId = submit(customer, "철수상점");

        TestBrowser admin = loggedInAdmin();

        // when
        // 관리자가 그 신청을 승인한다.
        HttpResponse<String> response = admin.post("/admin/store-applications/" + applicationId + "/approve", "{}");

        // then
        // 201 응답과 새 스토어 이름을 받는다.
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body(response).get("name").asString()).isEqualTo("철수상점");

        // then
        // 고객이 내 스토어를 조회하면 같은 스토어가 보인다.
        HttpResponse<String> myStore = customer.get("/stores/me");
        assertThat(myStore.statusCode()).isEqualTo(200);
        assertThat(body(myStore).get("name").asString()).isEqualTo("철수상점");
    }

    @Test
    void 이미_승인한_신청을_다시_승인하면_409를_받는다() {
        // given
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");
        Long applicationId = submit(customer, "철수상점");

        TestBrowser admin = loggedInAdmin();
        admin.post("/admin/store-applications/" + applicationId + "/approve", "{}");

        // when
        HttpResponse<String> response = admin.post("/admin/store-applications/" + applicationId + "/approve", "{}");
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(body(response).get("detail").asString()).isEqualTo("이미 심사한 신청입니다.");
    }

    @Test
    void 반려하면_내_신청_목록에_REJECTED와_사유가_보인다() {
        // given
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");
        Long applicationId = submit(customer, "철수상점");

        TestBrowser admin = loggedInAdmin();

        // when
        HttpResponse<String> response = admin.post("/admin/store-applications/" + applicationId + "/reject",
                json(Map.of("reason", "store name is unclear")));

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body(response).get("status").asString()).isEqualTo("REJECTED");

        HttpResponse<String> myList = customer.get("/store-applications/me");
        assertThat(body(myList).get(0).get("status").asString()).isEqualTo("REJECTED");
        assertThat(body(myList).get(0).get("rejectionReason").asString()).isEqualTo("store name is unclear");
    }

    @Test
    void 반려된_뒤에는_다시_신청할_수_있다() {
        // given
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");
        Long applicationId = submit(customer, "철수상점");

        TestBrowser admin = loggedInAdmin();
        admin.post("/admin/store-applications/" + applicationId + "/reject",
                json(Map.of("reason", "store name is unclear")));

        // when
        HttpResponse<String> response = customer.post("/store-applications", json(Map.of("storeName", "철수상점2")));

        // then
        assertThat(response.statusCode()).isEqualTo(201);
    }

    @Test
    void 스토어가_있는_계정이_신청하면_409를_받는다() {
        // given
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");
        Long applicationId = submit(customer, "철수상점");

        TestBrowser admin = loggedInAdmin();
        admin.post("/admin/store-applications/" + applicationId + "/approve", "{}");

        // when
        HttpResponse<String> response = customer.post("/store-applications", json(Map.of("storeName", "철수상점2")));

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(body(response).get("detail").asString()).isEqualTo("이미 스토어가 있습니다.");
    }

    @Test
    void 없는_신청을_승인하면_404를_받는다() {
        // given
        TestBrowser admin = loggedInAdmin();

        // when
        HttpResponse<String> response = admin.post("/admin/store-applications/99999/approve", "{}");

        // then
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(body(response).get("detail").asString()).isEqualTo("신청을 찾을 수 없습니다.");
    }

    @Test
    void 고객_세션으로_승인하면_403을_받는다() {
        // given
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");
        Long applicationId = submit(customer, "철수상점");

        // when
        HttpResponse<String> response = customer.post("/admin/store-applications/" + applicationId + "/approve", "{}");

        // then
        assertThat(response.statusCode()).isEqualTo(403);
        HttpResponse<String> myList = customer.get("/store-applications/me");
        assertThat(body(myList).get(0).get("status").asString()).isEqualTo("SUBMITTED");
    }

    @Test
    void 스토어가_없으면_내_스토어는_404를_받는다() {
        // given
        TestBrowser customer = loggedInCustomer("chulsoo@example.com", "철수");

        // when
        HttpResponse<String> response = customer.get("/stores/me");

        // then
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(body(response).get("detail").asString()).isEqualTo("스토어가 없습니다.");
    }
}

