package com.minipay.point.repository;

import com.minipay.common.domain.Point;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUserId(Long userId);

    // 비관적 락 적용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Point p where p.userId = :userId")
    Optional<Point> findByUserIdWithPessimisticLock(@Param("userId") Long userId);
}
