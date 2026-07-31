package com.minipay.point.service;

import com.minipay.common.domain.PointHistory;
import com.minipay.common.enums.TransactionType;
import com.minipay.point.dto.PointHistoryResponse;
import com.minipay.point.repository.PointHistoryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PointHistoryNoOffsetTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    public void setUp() {
        pointHistoryRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("No-Offset 첫 페이지 조회: lastHistoryId가 null이면 가장 최신 내역부터 size만큼 조회")
    void getHistories_firstPage() {
        // given
        Long userId = 1L;
        List<PointHistory> histories = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            histories.add(new PointHistory(userId, (long) (i * 1000), TransactionType.CHARGE));
        }
        pointHistoryRepository.saveAll(histories);

        // when
        List<PointHistoryResponse> result = pointService.getHistories(userId, null, 5);

        //then
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getAmount()).isEqualTo(15000L);
        assertThat(result.get(4).getAmount()).isEqualTo(11000L);
    }

    @Test
    @DisplayName("No-Offset 다음 페이지 조회: lastHistoryId를 기준 삼아 이전 ID 미만의 최신 내역을 연속해서 가져온다.")
    void getHistories_nextPage() {
        // given
        Long userId = 1L;
        List<PointHistory> histories = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            histories.add(new PointHistory(userId, (long) (i * 1000), TransactionType.CHARGE));
        }
        pointHistoryRepository.saveAll(histories);

        // when
        List<PointHistoryResponse> firstPage = pointService.getHistories(userId, null, 5);
        Long lastHistoryId = firstPage.get(firstPage.size() - 1).getPointsHistoryId();

        List<PointHistoryResponse> secondPage = pointService.getHistories(userId, lastHistoryId, 5);

        //then
        assertThat(secondPage).hasSize(5);
        assertThat(secondPage.get(0).getPointsHistoryId()).isLessThan(lastHistoryId);
        assertThat(secondPage.get(0).getAmount()).isEqualTo(10000L);
        assertThat(secondPage.get(4).getAmount()).isEqualTo(6000L);
    }
}
