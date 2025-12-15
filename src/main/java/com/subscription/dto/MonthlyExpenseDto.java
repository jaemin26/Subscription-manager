package com.subscription.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * MonthlyExpenseDto
 * 
 * 월별 지출액 조회 응답용 DTO
 * 
 * 수학적 구조:
 * - 계층 간 데이터 전송 객체 (Data Transfer Object)
 * - Service → Controller 계층으로 데이터 전달
 * 
 * 호출 경로: SubscriptionService.getMonthlyExpense()
 *          → 모든 구독의 월 환산 금액 합계 계산
 *          → MonthlyExpenseDto 생성
 *          → SubscriptionController.getMonthlyExpense()
 *          → ResponseEntity<MonthlyExpenseDto> 반환
 * 
 * 월 환산 계산 공식:
 * - MONTHLY: price (그대로)
 * - QUARTERLY: price ÷ 3
 * - YEARLY: price ÷ 12
 * 
 * 최종 합계: 모든 구독의 월 환산 금액을 합산
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyExpenseDto {

    /**
     * 사용자 ID
     * 수학적 구조: 양의 정수 (1 이상)
     */
    @NotNull(message = "사용자 ID는 필수입니다.")
    @Positive(message = "사용자 ID는 양수여야 합니다.")
    private Long userId;

    /**
     * 월별 총 지출액 (BigDecimal 사용 - 금액 정밀도 보장)
     * 
     * 수학적 구조: DECIMAL(10, 2)
     * - 소수점 2자리까지 저장
     * - 모든 구독의 월 환산 금액 합계
     * 
     * 계산 공식:
     * totalMonthlyExpense = Σ(각 구독의 월 환산 금액)
     * 
     * 호출 경로: SubscriptionService.getMonthlyExpense()
     *          → 각 구독의 월 환산 금액 계산
     *          → BigDecimal.add()로 합산
     */
    @NotNull(message = "월별 총 지출액은 필수입니다.")
    private BigDecimal totalMonthlyExpense;
}

