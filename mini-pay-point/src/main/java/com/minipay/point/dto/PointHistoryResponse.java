package com.minipay.point.dto;

import com.minipay.common.domain.PointHistory;
import com.minipay.common.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointHistoryResponse {

    private Long pointsHistoryId;
    private Long userId;
    private Long amount;
    private TransactionType type;
    private LocalDateTime createTime;

    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getId(),
                history.getUserId(),
                history.getAmount(),
                history.getType(),
                history.getCreateTime()
        );
    }
}
