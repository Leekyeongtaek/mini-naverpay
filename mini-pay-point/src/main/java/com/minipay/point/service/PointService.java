package com.minipay.point.service;

import com.minipay.common.domain.Point;
import com.minipay.common.domain.PointHistory;
import com.minipay.common.enums.TransactionType;
import com.minipay.point.dto.PointChargeRequest;
import com.minipay.point.dto.PointHistoryResponse;
import com.minipay.point.dto.PointResponse;
import com.minipay.point.dto.PointUseRequest;
import com.minipay.point.repository.PointHistoryRepository;
import com.minipay.point.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointResponse chargePoint(PointChargeRequest request) {

        Point point = pointRepository.findByUserId(request.getUserId()).orElseGet(() -> pointRepository.save(new Point(request.getUserId(), 0L)));

        // 포인트 충전
        point.charge(request.getAmount());

        // 포인트 충전 내역 저장
        PointHistory pointHistory = new PointHistory(
                request.getUserId(),
                request.getAmount(),
                TransactionType.CHARGE
        );
        pointHistoryRepository.save(pointHistory);

        return PointResponse.from(point);
    }

    public PointResponse chargePointWithPessimisticLock(PointChargeRequest request) {
        Point point = pointRepository.findByUserIdWithPessimisticLock(request.getUserId()).orElseGet(() -> pointRepository.save(new Point(request.getUserId(), 0L)));

        point.charge(request.getAmount());

        PointHistory history = new PointHistory(
                request.getUserId(),
                request.getAmount(),
                TransactionType.CHARGE
        );
        pointHistoryRepository.save(history);

        return PointResponse.from(point);
    }

    /**
     * 낙관적 락 사용
     * Point 엔티티의 @Version 필드를 기반으로 커밋 시점에 버전 충돌을 감지
     */
    public PointResponse chargePointWithOptimisticLock(PointChargeRequest request) {
        Point point = pointRepository.findByUserId(request.getUserId())
                .orElseGet(() -> pointRepository.save(new Point(request.getUserId(), 0L)));

        point.charge(request.getAmount());

        PointHistory history = new PointHistory(
                request.getUserId(),
                request.getAmount(),
                TransactionType.CHARGE
        );
        pointHistoryRepository.save(history);

        return PointResponse.from(point);
    }

    /**
     * 포인트 잔액 조회
     */
    public PointResponse getBalance(Long userId) {
        Point point = pointRepository.findByUserId(userId)
                .orElseGet(() -> new Point(userId, 0L));
        return PointResponse.from(point);
    }

    /**
     * 포인트 사용/차감
     */
    public PointResponse usePoint(PointUseRequest request) {
        Point point = pointRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        point.use(request.getAmount());

        PointHistory pointHistory = new PointHistory(
                request.getUserId(),
                request.getAmount(),
                TransactionType.USE);
        pointHistoryRepository.save(pointHistory);

        return PointResponse.from(point);
    }

    /**
     * 포인트 거래 내역 조회
     * @param userId 사용자 ID
     * @param lastHistoryId 직전 조회한 마지막 historyId
     * @param size 한 번에 조회할 개수
     */
    public List<PointHistoryResponse> getHistories(Long userId, Long lastHistoryId, int size) {
        Pageable pageable = PageRequest.of(0, size);

        List<PointHistory> histories;

        if (lastHistoryId == null || lastHistoryId <= 0) {
            histories = pointHistoryRepository.findHistoriesFirstPage(userId, pageable);
        } else {
            histories = pointHistoryRepository.findHistoriesNoOffset(userId, lastHistoryId, pageable);
        }

        return histories.stream()
                .map(PointHistoryResponse::from)
                .toList();
    }
}
