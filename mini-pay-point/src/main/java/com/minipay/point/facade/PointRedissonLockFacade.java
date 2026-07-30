package com.minipay.point.facade;

import com.minipay.point.dto.PointChargeRequest;
import com.minipay.point.dto.PointResponse;
import com.minipay.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointRedissonLockFacade {

    private final RedissonClient redissonClient;
    private final PointService pointService;

    public PointResponse chargePoint(PointChargeRequest request) {

        String lockKey = "point:lock:" + request.getUserId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean available = lock.tryLock(10, 2, TimeUnit.SECONDS);

            if (!available) {
                log.warn("Redisson 락 획득 실패 - lockKey: {}", lockKey);
                throw new IllegalStateException("동시 요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }

            return pointService.chargePoint(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
