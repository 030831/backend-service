package com.hyunjun.backend.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Embeddable
@Getter
@NoArgsConstructor
public class LoginLock {

    public static final int MAX_FAILURES = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    @Column(nullable = false)
    private int failedLoginCount;

    private Instant lockedUntil;

    public boolean isLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    public void recordFailure(Instant now) {
        if (isLocked(now)) { // 잠긴 동안의 실패는 세지 않는다. 동시 요청이 잠금을 늘리지 못한다.
            return;
        }

        if (lockedUntil != null) { // 잠금이 끝나 있었다: 처음부터 다시 센다.
            reset();
        }

        failedLoginCount++;

        if (failedLoginCount >= MAX_FAILURES) {
            lockedUntil = now.plus(LOCK_DURATION);
        }
    }

    public boolean hasFailures() {
        return failedLoginCount > 0 || lockedUntil != null;
    }

    public void reset() {
        failedLoginCount = 0;
        lockedUntil = null;
    }
}
