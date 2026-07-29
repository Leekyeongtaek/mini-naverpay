package com.minipay.common.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "points_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    // 낙관적 락 전용 버전 관리 컬럼
    @Version
    private Long version;

    public Point(Long userId, Long amount) {
        this.userId = userId;
        this.amount = amount;
    }

    // 포인트 충천
    public void charge(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.amount += amount;
    }
}
