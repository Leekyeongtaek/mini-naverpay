package com.minipay.point.dto;

import com.minipay.common.domain.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointResponse {

    private Long userId;
    private Long balance;

    public static PointResponse from(Point point) {
        return new PointResponse(point.getUserId(), point.getAmount());
    }
}
