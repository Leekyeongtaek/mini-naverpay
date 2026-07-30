package com.minipay.point.facade;

import com.minipay.point.dto.PointChargeRequest;
import com.minipay.point.dto.PointResponse;
import com.minipay.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointOptimisticLockFacade {

    private final PointService pointService;

    public PointResponse chargePoint(PointChargeRequest request) throws InterruptedException {
        while (true) {
            try {
                return pointService.chargePointWithOptimisticLock(request);
            } catch (ObjectOptimisticLockingFailureException e) {
                // 버전 충돌 발생 시 잠시 대기 후 재시도
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("포인트 충전 예외 :: ", e);
                throw e;
            }
        }
    }
}
