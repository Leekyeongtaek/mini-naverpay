package com.minipay.point.service;

import com.minipay.common.domain.Point;
import com.minipay.common.domain.PointHistory;
import com.minipay.common.enums.TransactionType;
import com.minipay.point.dto.PointChargeRequest;
import com.minipay.point.dto.PointResponse;
import com.minipay.point.repository.PointHistoryRepository;
import com.minipay.point.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
