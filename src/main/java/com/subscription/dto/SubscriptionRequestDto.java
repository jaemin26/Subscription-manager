package com.subscription.dto;

import com.subscription.entity.BillingCycle;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SubscriptionRequestDto
 * 
 * 구독 서비스 등록/수정 요청용 DTO
 * 
 * 수학적 구조:
 * - 계층 간 데이터 전송 객체 (Data Transfer Object)
 * - Controller → Service 계층으로 데이터 전달
 * 
 * 호출 경로: SubscriptionController.create()
 *          → @Valid SubscriptionRequestDto
 *          → SubscriptionService.createSubscription()
 * 
 * 검증 규칙:
 * - serviceName: 필수, 공백 불가
 * - price: 필수, 0 이상
 * - billingCycle: 필수
 * - billingDate: 필수
 * - userId: 필수, 양수
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestDto {

    /**
     * 사용자 ID
     * 수학적 구조: 양의 정수 (1 이상)
     */
    @NotNull(message = "사용자 ID는 필수입니다.")
    @Positive(message = "사용자 ID는 양수여야 합니다.")
    private Long userId;

    /**
     * 서비스 이름
     * 수학적 구조: 비어있지 않은 문자열
     */
    @NotBlank(message = "서비스 이름은 필수입니다.")
    @Size(max = 100, message = "서비스 이름은 100자 이하여야 합니다.")
    private String serviceName;

    /**
     * 가격 (BigDecimal 사용 - 금액 정밀도 보장)
     * 
     * 수학적 구조: DECIMAL(10, 2)
     * - 소수점 2자리까지 저장
     * - 0 이상의 값만 허용
     */
    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
    @Digits(integer = 8, fraction = 2, message = "가격은 정수 8자리, 소수점 2자리까지 입력 가능합니다.")
    private BigDecimal price;

    /**
     * 결제 주기 (Enum)
     * 수학적 구조: MONTHLY, QUARTERLY, YEARLY 중 하나
     */
    @NotNull(message = "결제 주기는 필수입니다.")
    private BillingCycle billingCycle;

    /**
     * 결제일 (LocalDate)
     * 수학적 구조: YYYY-MM-DD 형식의 날짜
     */
    @NotNull(message = "결제일은 필수입니다.")
    private LocalDate billingDate;
}

