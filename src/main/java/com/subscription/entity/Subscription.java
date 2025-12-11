package com.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Subscription 엔티티
 * 
 * 수학적 구조:
 * - 1:N 관계의 N (Many)
 * - User 1개 : Subscription N개
 * - 연관관계의 주인 (Owner): user_id 외래키를 가짐
 * 
 * 호출 경로: SubscriptionController.create()
 *          → SubscriptionService.createSubscription()
 *          → SubscriptionRepository.save()
 *          → DateCalculator.calculateNextBillingDate() (다음 결제일 계산)
 * 
 * 연관관계:
 * - @ManyToOne: 여러 Subscription이 하나의 User에 속함
 * - @JoinColumn(name = "user_id"): 외래키 컬럼명 지정 (연관관계의 주인)
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    /**
     * 구독 서비스 ID (Primary Key)
     * 수학적 구조: 자동 증가 정수 (AUTO_INCREMENT)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 서비스 이름 (예: 넷플릭스, 스포티파이)
     */
    @Column(nullable = false)
    private String serviceName;

    /**
     * 가격 (BigDecimal 사용 - 금액 정밀도 보장)
     * 
     * 수학적 구조: DECIMAL(10, 2)
     * - 소수점 2자리까지 저장 (원 단위 + 소수점)
     * - 정밀한 금액 계산을 위해 BigDecimal 사용
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 결제 주기 (Enum)
     * 수학적 구조: MONTHLY, QUARTERLY, YEARLY 중 하나
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    /**
     * 결제일 (LocalDate)
     * 수학적 구조: YYYY-MM-DD 형식의 날짜
     */
    @Column(nullable = false)
    private LocalDate billingDate;

    /**
     * 다음 결제일 (LocalDate)
     * 
     * 수학적 구조:
     * - 계산 공식: nextBillingDate = billingDate + 주기
     * - MONTHLY: billingDate + 1개월
     * - QUARTERLY: billingDate + 3개월
     * - YEARLY: billingDate + 1년
     * 
     * 호출 경로: DateCalculator.calculateNextBillingDate()에서 계산
     */
    @Column(nullable = false)
    private LocalDate nextBillingDate;

    /**
     * 사용자 (연관관계의 주인)
     * 
     * 수학적 구조:
     * - Foreign Key: user_id
     * - User 1개 : Subscription N개 (1:N)
     * 
     * 호출 경로: Subscription.getUser()
     *          → User 정보 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

