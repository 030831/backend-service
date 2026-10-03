# 1단계(builder): jar를 네 개 층으로 푼다. 빌드하는 컴퓨터의 CPU로 실행한다.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jre-noble AS builder
WORKDIR /builder
COPY build/libs/application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# 2단계(runtime): 서버에서 실제로 실행되는 이미지.
FROM eclipse-temurin:21-jre-noble
WORKDIR /application
# root가 아닌 사용자로 실행한다. 앱이 뚫려도 컨테이너 안에서 root 권한을 얻지 못하게.
RUN useradd --system --create-home --uid 1001 app
# 잘 안 바뀌는 층(의존성)을 먼저, 자주 바뀌는 층(우리 코드)을 마지막에. 서버는 바뀐 층만 다시 받는다.
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]