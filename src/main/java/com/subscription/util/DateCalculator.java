package com.subscription.util;

import com.subscription.entity.BillingCycle;

import java.time.LocalDate;

/**
 * 날짜 계산 유틸리티 클래스
 * 
 * 수학적 구조:
 * - 상태 없는(stateless) 유틸리티 클래스
 * - 모든 메서드는 static 메서드
 * - 날짜 계산 로직만 담당
 * 
 * 호출 경로: SubscriptionService.createSubscription()
 *          → DateCalculator.calculateNextBillingDate()
 *          → LocalDate.plusMonths() / plusYears()
 * 
 * 역할:
 * - 다음 결제일 계산 로직을 중앙화
 * - Service 계층에서 직접 날짜 계산 금지
 * - 재사용 가능한 날짜 계산 함수 제공
 */
public class DateCalculator {

    /**
     * 다음 결제일 계산
     * 
     * 수학적 구조:
     * - 계산 공식: nextBillingDate = billingDate + 주기
     * - MONTHLY: billingDate + 1개월
     * - QUARTERLY: billingDate + 3개월
     * - YEARLY: billingDate + 1년
     * 
     * 호출 경로: SubscriptionService.createSubscription()
     *          → DateCalculator.calculateNextBillingDate(billingDate, billingCycle)
     *          → LocalDate.plusMonths() 또는 plusYears()
     * 
     * @param billingDate 현재 결제일
     * @param billingCycle 결제 주기 (MONTHLY, QUARTERLY, YEARLY)
     * @return 다음 결제일
     * @throws IllegalArgumentException billingCycle이 null이거나 유효하지 않은 경우
     */
    public static LocalDate calculateNextBillingDate(LocalDate billingDate, BillingCycle billingCycle) {
        if (billingDate == null) {
            throw new IllegalArgumentException("결제일(billingDate)은 null일 수 없습니다.");
        }
        
        if (billingCycle == null) {
            throw new IllegalArgumentException("결제 주기(billingCycle)는 null일 수 없습니다.");
        }
        
        return switch (billingCycle) {
            case MONTHLY -> billingDate.plusMonths(1);
            case QUARTERLY -> billingDate.plusMonths(3);
            case YEARLY -> billingDate.plusYears(1);
        };
    }
}

