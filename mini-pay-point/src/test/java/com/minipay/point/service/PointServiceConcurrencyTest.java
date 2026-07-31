package com.minipay.point.service;

import com.minipay.common.domain.Point;
import com.minipay.point.dto.PointChargeRequest;
import com.minipay.point.dto.PointUseRequest;
import com.minipay.point.facade.PointOptimisticLockFacade;
import com.minipay.point.facade.PointRedissonLockFacade;
import com.minipay.point.repository.PointHistoryRepository;
import com.minipay.point.repository.PointRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointOptimisticLockFacade pointOptimisticLockFacade;

    @Autowired
    private PointRedissonLockFacade pointRedissonLockFacade;

    @AfterEach
    void tearDown() {
        pointHistoryRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("락 미적용: 100개의 동시 충전 요청 시 갱신 손실 발생으로 기대 금액과 충전 금액이 달라진다")
    void chargePoint_concurrency_without_lock() throws InterruptedException {
        // given
        Long userId = 1L;
        Long chargeAmount = 100L;
        int threadCount = 100;

        pointRepository.save(new Point(userId, 0L));

        // 동시 요청을 위한 비동기 멀티 스레드 및 카운트다운 래치
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.chargePoint(new PointChargeRequest(userId, chargeAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드 작업 종료까지 대기
        latch.await();

        // then
        Point point = pointRepository.findByUserId(userId).orElseThrow();
        Long expectedBalance = chargeAmount * threadCount; // 100 * 100 = 10000

        System.out.println("==========================================");
        System.out.println("기대 포인트 : " + expectedBalance); // 10000
        System.out.println("실제 충전 포인트 : " + point.getAmount()); // 5600, 6700 등 실행 시마다 달라짐
        System.out.println("==========================================");

        assertThat(point.getAmount()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("비관적 락 적용: 100개의 동시 충전 요청 시 DB X-Lock으로 100% 데이터 정합성 보장")
    void chargePoint_concurrency_with_pessimistic_lock() throws InterruptedException {
        // given
        Long userId = 1L;
        Long chargeAmount = 100L;
        int threadCount = 100;

        pointRepository.save(new Point(userId, 0L));

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.chargePointWithPessimisticLock(new PointChargeRequest(userId, chargeAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Point point = pointRepository.findByUserId(userId).orElseThrow();
        Long expectedBalance = chargeAmount * threadCount;

        System.out.println("==========================================");
        System.out.println("기대 포인트 : " + expectedBalance); // 10000
        System.out.println("실제 충전 포인트 : " + point.getAmount()); // 10000
        System.out.println("==========================================");

        assertThat(point.getAmount()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("낙관적 락 적용: 버전 충돌 시 Facade 재시도를 통해 100% 데이터 정합성 보장")
    void chargePoint_concurrency_with_optimistic_lock() throws InterruptedException {
        // given
        Long userId = 1L;
        Long chargeAmount = 100L;
        int threadCount = 100;

        pointRepository.save(new Point(userId, 0L));

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointOptimisticLockFacade.chargePoint(new PointChargeRequest(userId, chargeAmount));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Point point = pointRepository.findByUserId(userId).orElseThrow();
        Long expectedBalance = chargeAmount * threadCount;

        System.out.println("==========================================");
        System.out.println("기대 포인트 : " + expectedBalance); // 10000
        System.out.println("실제 충전 포인트 : " + point.getAmount()); // 10000
        System.out.println("==========================================");

        assertThat(point.getAmount()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("Redisson 분산 락 적용: Pub/Sub 기반 분산 락으로 동시 요청 정합성 보장")
    void chargePoint_concurrency_with_redisson_lock() throws InterruptedException {
        // given
        Long userId = 1L;
        Long chargeAmount = 100L;
        int threadCount = 100;

        pointRepository.save(new Point(userId, 0L));

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointRedissonLockFacade.chargePoint(new PointChargeRequest(userId, chargeAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Point point = pointRepository.findByUserId(userId).orElseThrow();
        Long expectedBalance = chargeAmount * threadCount;

        System.out.println("==========================================");
        System.out.println("기대 포인트 : " + expectedBalance); // 10000
        System.out.println("실제 충전 포인트 : " + point.getAmount()); // 10000
        System.out.println("==========================================");

        assertThat(point.getAmount()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("포인트 차감 동시성 테스트: 10000 포인트에서 100개 스레드가 동시 100 포인트 차감 시 최종 잔여 포인트는 0원")
    void usePoint_concurrency_with_redisson_lock() throws InterruptedException {
        // given
        Long userId = 1L;
        Long initialAmount = 10000L;
        Long useAmount = 100L;
        int threadCount = 100;

        pointRepository.save(new Point(userId, initialAmount));

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointRedissonLockFacade.usePoint(new PointUseRequest(userId, useAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Point point = pointRepository.findByUserId(userId).orElseThrow();
        System.out.println("==========================================");
        System.out.println("초기 금액 : " + initialAmount + "원");
        System.out.println("최종 남은 잔액 : " + point.getAmount() + "원");
        System.out.println("==========================================");

        assertThat(point.getAmount()).isEqualTo(0L);
    }
}
