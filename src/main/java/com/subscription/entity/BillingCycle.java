package com.subscription.entity;

/**
 * 결제 주기 Enum
 * 
 * 수학적 구조:
 * - MONTHLY: 월간 결제 (1개월 단위)
 * - QUARTERLY: 분기별 결제 (3개월 단위)
 * - YEARLY: 연간 결제 (12개월 단위)
 * 
 * 다음 결제일 계산 시 각 주기에 따라 일자를 더함
 */
public enum BillingCycle {
    MONTHLY,    // 월간
    QUARTERLY,  // 분기별
    YEARLY      // 연간
}

