package com.subscription.dto;

import com.subscription.entity.BillingCycle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SubscriptionResponseDto
 * 
 * 구독 서비스 조회 응답용 DTO
 * 
 * 수학적 구조:
 * - 계층 간 데이터 전송 객체 (Data Transfer Object)
 * - Service → Controller 계층으로 데이터 전달
 * 
 * 호출 경로: SubscriptionService.getSubscriptionById()
 *          → SubscriptionResponseDto 생성
 *          → SubscriptionController.getById()
 *          → ResponseEntity<SubscriptionResponseDto> 반환
 * 
 * 필드 설명:
 * - id: 구독 서비스 ID
 * - serviceName: 서비스 이름
 * - price: 가격 (BigDecimal)
 * - billingCycle: 결제 주기
 * - billingDate: 결제일
 * - nextBillingDate: 다음 결제일 (자동 계산됨)
 * - userId: 사용자 ID
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponseDto {

    /**
     * 구독 서비스 ID (Primary Key)
     * 수학적 구조: 양의 정수
     */
    private Long id;

    /**
     * 서비스 이름
     */
    private String serviceName;

    /**
     * 가격 (BigDecimal 사용 - 금액 정밀도 보장)
     * 
     * 수학적 구조: DECIMAL(10, 2)
     * - 소수점 2자리까지 저장
     */
    private BigDecimal price;

    /**
     * 결제 주기 (Enum)
     * 수학적 구조: MONTHLY, QUARTERLY, YEARLY 중 하나
     */
    private BillingCycle billingCycle;

    /**
     * 결제일 (LocalDate)
     * 수학적 구조: YYYY-MM-DD 형식의 날짜
     */
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
    private LocalDate nextBillingDate;

    /**
     * 사용자 ID
     * 수학적 구조: 양의 정수
     */
    private Long userId;
}

