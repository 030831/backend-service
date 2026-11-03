package com.hyunjun.backend.order.dto;

import java.util.List;

public record OrderCancelRequest(List<Long> lineIds) {
}
