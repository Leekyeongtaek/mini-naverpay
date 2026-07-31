package com.minipay.point.controller;

import com.minipay.point.dto.PointChargeRequest;
import com.minipay.point.dto.PointHistoryResponse;
import com.minipay.point.dto.PointResponse;
import com.minipay.point.dto.PointUseRequest;
import com.minipay.point.facade.PointRedissonLockFacade;
import com.minipay.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final PointRedissonLockFacade pointRedissonLockFacade;

    // 포인트 잔액 조회
    @GetMapping("/{userId}")
    public ResponseEntity<PointResponse> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(pointService.getBalance(userId));
    }

    // 포인트 충전
    @PostMapping("/charge")
    public ResponseEntity<PointResponse> chargePoint(@Valid @RequestBody PointChargeRequest request) {
        PointResponse response = pointService.chargePoint(request);
        return ResponseEntity.ok(response);
    }

    // 포인트 사용/차감
    @PostMapping("/use")
    public ResponseEntity<PointResponse> usePoint(@Valid @RequestBody PointUseRequest request) {
        return ResponseEntity.ok(pointRedissonLockFacade.usePoint(request));
    }

    // 포인트 사용 내역 목록 조회
    @GetMapping("/{userId}/histories")
    public ResponseEntity<List<PointHistoryResponse>> getHistories(
            @PathVariable Long userId,
            @RequestParam(required = false) Long lastHistoryId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(pointService.getHistories(userId, lastHistoryId, size));
    }
}
