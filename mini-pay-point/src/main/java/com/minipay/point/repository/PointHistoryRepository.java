package com.minipay.point.repository;

import com.minipay.common.domain.PointHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // No-Offset 첫 페이지 조회
    @Query("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId ORDER BY ph.id DESC")
    List<PointHistory> findHistoriesFirstPage(@Param("userId") Long userId, Pageable pageable);

    // No-Offset 다음 페이지 조회 (WHERE id < :lastHistoryId 로 인덱스 스캔)
    @Query("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId AND ph.id < :lastHistoryId ORDER BY ph.id DESC")
    List<PointHistory> findHistoriesNoOffset(@Param("userId") Long userId, @Param("lastHistoryId") Long lastHistoryId, Pageable pageable);
}
