# backend-service

고객의 상품 주문부터 판매자의 상품 관리, 관리자의 입점 심사까지 구현한 **Spring Boot 기반 쇼핑몰 REST API**입니다.

주문 과정에서 발생하는 중복 요청, 재고 경쟁, 결제·취소·만료의 동시 실행을 다루며 **트랜잭션과 DB 제약을 활용한 데이터 일관성**에 중점을 두었습니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어·프레임워크 | Java 21, Spring Boot 4.1.1, Spring MVC |
| 인증·검증 | Spring Security, HTTP Session, CSRF, Bean Validation |
| 데이터 | Spring Data JPA, Hibernate, MySQL 8.4, Flyway |
| 테스트 | JUnit Jupiter, AssertJ, Mockito |
| API 문서 | springdoc-openapi, Swagger UI |
| 빌드·배포 | Gradle, Docker Compose, GitHub Actions, GHCR, Caddy |

## 주요 기능

| 영역 | 구현 내용 |
| --- | --- |
| 계정·인증 | 회원가입, 세션 로그인·로그아웃, 고객·관리자 권한 분리, 로그인 실패 시 계정 잠금 |
| 입점 심사 | 스토어 신청, 관리자 승인·반려, 신청 이력 조회, 중복 신청 제한 |
| 상품·재고 | 상품 등록·수정, 옵션별 판매 단위(SKU)와 재고 관리, 판매 중지·재개, 상품 목록·상세 조회 |
| 장바구니 | 동일 SKU 수량 합산, 수량 변경·삭제, 선택 항목 주문 |
| 주문 | 주문 당시 상품 정보 저장, 총액 검증, 재고 차감, 중복 요청 처리, 본인 주문 조회 |
| 결제·취소 | 결제 금액 검증과 상태 변경, 전체·항목별 취소, 취소 금액 기록과 재고 복원 |
| 주문 만료 | 생성 후 30분이 지난 미결제 주문의 자동 만료와 재고 복원 |

결제·취소는 주문 상태와 금액을 관리하는 범위로 구현했으며, 외부 결제사는 연동하지 않았습니다.

## 핵심 설계

### 1. 중복 요청에도 주문은 한 번만 생성

주문 요청이 반복되거나 동시에 도착해도 동일 주문에 대한 재고 차감이 반복되지 않도록 처리했습니다.

- 계정 ID와 멱등키(`idempotencyKey`)로 기존 주문을 조회하고, 이미 존재하면 해당 주문을 반환합니다.
- 두 요청이 동시에 기존 주문이 없다고 판단하는 상황은 `(account_id, idempotency_key)` UNIQUE 제약으로 처리합니다.
- 주문을 먼저 저장한 뒤 재고를 차감하여 중복 키로 저장에 실패한 요청은 재고 차감 단계에 진입하지 않습니다.
- 중복 저장에 실패한 트랜잭션이 롤백된 뒤 기존 주문을 다시 조회하도록 주문 생성과 요청 조정의 책임을 분리했습니다.

구현: [OrderService](src/main/java/com/hyunjun/backend/order/service/OrderService.java), [OrderCreator](src/main/java/com/hyunjun/backend/order/service/OrderCreator.java)

### 2. 동시 주문의 재고 초과 판매 방지

재고를 조회한 뒤 애플리케이션에서 계산해 저장하면, 동시 요청이 같은 재고를 기준으로 차감할 수 있습니다. 이를 방지하기 위해 **차감 후 수량이 0 이상인 경우에만 갱신하는 조건부 UPDATE**를 사용했습니다.

- UPDATE 결과로 차감 성공 여부를 판단합니다.
- 주문 저장과 모든 품목의 재고 차감을 하나의 트랜잭션에서 수행합니다.
- 한 품목이라도 부족하면 주문과 이미 차감한 다른 품목의 재고를 함께 롤백합니다.
- 여러 품목을 처리할 때 SKU ID 오름차순으로 재고에 접근하여 요청마다 잠금 순서가 달라지는 데서 발생하는 교착을 방지합니다.

구현: [StockRepository](src/main/java/com/hyunjun/backend/product/repository/StockRepository.java), [OrderCreator](src/main/java/com/hyunjun/backend/order/service/OrderCreator.java)

### 3. 결제·취소·만료 간 상태 충돌 제어

같은 주문에 결제·취소·만료가 동시에 실행되면 상태 변경과 재고 복원이 충돌할 수 있습니다. 세 작업 모두 **주문 행의 비관적 쓰기 잠금**을 획득한 뒤 현재 상태를 검사하도록 구성했습니다.

- 이미 취소한 항목은 다시 취소할 수 없도록 제한합니다.
- 결제 또는 취소가 끝난 주문은 만료 처리 대상에서 제외합니다.
- 주문 상태 변경과 재고 복원을 같은 트랜잭션에서 처리합니다.
- 부분 취소 시 결제 전에는 결제 예정 금액을 줄이고, 결제 후에는 취소 금액을 누적합니다.
- 주문 당시 상품명·옵션·단가를 주문 항목에 저장하여 상품 정보 변경 후에도 주문 내역을 유지합니다.

구현: [Order](src/main/java/com/hyunjun/backend/order/domain/Order.java), [OrderStateService](src/main/java/com/hyunjun/backend/order/service/OrderStateService.java), [OrderExpiryService](src/main/java/com/hyunjun/backend/order/service/OrderExpiryService.java)

### 4. 장바구니 주문의 트랜잭션 분리

장바구니의 선택 항목을 주문 입력으로 변환하여 기존 주문 생성 로직을 재사용합니다. 주문 저장이 완료된 뒤 장바구니 항목을 삭제하도록 트랜잭션을 분리했습니다.

주문 생성이 실패하면 장바구니를 유지합니다. 주문 저장 후 장바구니 삭제만 실패한 경우에는 같은 멱등키로 재요청하여 기존 주문을 반환하고 항목 삭제를 다시 수행합니다.

구현: [CartService](src/main/java/com/hyunjun/backend/cart/service/CartService.java)

### 5. 인증·권한과 오류 응답

- 고객과 관리자 계정 및 권한을 분리하고, 판매자 기능에서는 스토어 보유 여부와 상품 소유권을 검사합니다.
- BCrypt 기반 비밀번호 저장, 로그인 시 세션 ID 변경, CSRF 검증을 적용했습니다.
- 비밀번호를 5회 틀리면 15분 동안 계정을 잠그고, 로그인 성공 시 실패 기록을 초기화합니다.
- 인증·권한·입력값·업무 규칙 오류를 `ProblemDetail` 형식으로 응답합니다.

구현: [SecurityConfig](src/main/java/com/hyunjun/backend/common/config/SecurityConfig.java), [LoginLock](src/main/java/com/hyunjun/backend/common/domain/LoginLock.java), [GlobalExceptionHandler](src/main/java/com/hyunjun/backend/common/exception/GlobalExceptionHandler.java)

## 테스트

실제 서버를 임의 포트로 실행하고 HTTP 요청과 MySQL 데이터를 함께 확인하는 통합 테스트를 작성했습니다. 쿠키와 CSRF 토큰을 관리하는 테스트 도구를 통해 로그인 이후의 요청 흐름도 테스트합니다.

동시성 테스트에서는 여러 스레드와 실행 대기 지점을 사용하여 요청이 경쟁하는 상황을 구성했습니다.

| 검증 영역 | 작성한 주요 시나리오 |
| --- | --- |
| 주문 동시성 | 마지막 재고 1개에 대한 두 구매자의 주문 경쟁, 동일 멱등키 동시 주문, 품목 순서를 바꾼 20개 동시 주문 |
| 주문 상태 | 동일 주문 동시 취소, 결제와 만료의 경쟁, 취소와 만료의 경쟁 |
| 주문 트랜잭션 | 재고 부족 시 주문과 다른 품목의 재고 차감 롤백, 총액 불일치 거부 |
| 장바구니 | 동시 담기 시 수량 합산, 수량 상한, 선택 주문, 주문 실패 시 항목 유지 |
| 인증·권한 | 로그인 잠금·해제, 고객·관리자 권한 분리, CSRF 검증, 타인 주문·장바구니 접근 제한 |
| 계정·스토어 | 동일 이메일 동시 가입, 입점 승인·반려·재신청 |

테스트 코드: [src/test/java/com/hyunjun/backend](src/test/java/com/hyunjun/backend)

## 프로젝트 구조

`com.hyunjun.backend` 아래에 업무별 패키지를 두고, 각 업무를 컨트롤러·서비스·도메인·저장소·DTO로 구성했습니다.

| 패키지 | 책임 |
| --- | --- |
| `account` / `admin` | 고객·관리자 계정과 인증 |
| `store` | 입점 신청·심사·스토어 관리 |
| `product` | 상품·SKU·재고 관리 |
| `cart` | 장바구니와 선택 주문 |
| `order` | 주문 생성·조회·결제·취소·만료 |
| `common` | 보안 설정·공통 예외·응답 |

서비스가 트랜잭션 경계를 관리하고 도메인 객체가 상태 변경 규칙을 검사합니다. DB 스키마는 Flyway 마이그레이션으로 관리하며 Hibernate는 엔티티와 테이블의 매핑을 검증합니다.

## CI/CD

GitHub Actions에 다음 파이프라인을 구성했습니다.

**main push → MySQL 기반 테스트 → JAR·Docker 이미지 빌드 → GHCR 게시 → 배포 활성화 시 SSH 배포·상태 확인**

- Docker Compose로 애플리케이션·MySQL·Caddy 구성
- 커밋별 이미지 태그와 지정 태그로 되돌리는 수동 롤백 워크플로
- Caddy를 통한 HTTPS 접속 구성
- MySQL 백업 스크립트 작성

설정: [CI](.github/workflows/ci.yml), [Rollback](.github/workflows/rollback.yml), [Docker Compose](deploy/compose.yaml)

## 실행 방법

JDK 21과 MySQL 8.4를 준비하고 `backend_service` DB 및 접속 계정을 생성합니다. 기본 접속 주소는 `localhost:3306/backend_service`입니다.

DB 접속 정보는 `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` 환경 변수로 지정합니다. 주소를 변경하려면 `SPRING_DATASOURCE_URL`을 지정합니다.

프로젝트 루트에서 실행합니다.

```sh
./gradlew bootRun
```

관리자 기능을 사용하려면 `ADMIN_BOOTSTRAP_EMAIL`, `ADMIN_BOOTSTRAP_NAME`, `ADMIN_BOOTSTRAP_PASSWORD`를 지정합니다. 관리자 계정이 없을 때 초기 계정을 생성합니다.

- [Swagger UI](http://localhost:8080/swagger-ui/index.html): API 목록과 요청·응답 명세
- [OpenAPI](http://localhost:8080/v3/api-docs): API 명세 JSON

### 테스트 실행

테스트 전용 `backend_service_test` DB와 접속 계정을 준비합니다. 기본 접속 계정은 `backend_app`이며, 비밀번호는 `SPRING_DATASOURCE_PASSWORD`로 지정합니다.

테스트는 DB의 데이터를 삭제하며 실행되므로 반드시 테스트 전용 DB에 연결해야 합니다.

```sh
./gradlew test
```

테스트 결과는 `build/reports/tests/test/index.html`에서 확인할 수 있습니다.
