package com.minipay.point.repository;

import com.minipay.common.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory,Long> {
}
