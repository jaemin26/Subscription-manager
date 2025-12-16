/**
 * TypeScript 타입 정의
 * 백엔드 DTO와 일치하는 타입 정의
 * 
 * 호출 경로: 프론트엔드 컴포넌트 → services/api.ts → 백엔드 API
 */

/**
 * 결제 주기 타입
 * 수학적 구조: MONTHLY, QUARTERLY, YEARLY 중 하나
 * 
 * 호출 경로: 백엔드 BillingCycle Enum과 일치
 */
export type BillingCycle = 'MONTHLY' | 'QUARTERLY' | 'YEARLY';

/**
 * 결제 주기 상수 객체 (런타임에서 사용)
 */
export const BillingCycle = {
  MONTHLY: 'MONTHLY' as const,      // 월간 결제 (1개월 단위)
  QUARTERLY: 'QUARTERLY' as const,  // 분기별 결제 (3개월 단위)
  YEARLY: 'YEARLY' as const         // 연간 결제 (12개월 단위)
} as const;

/**
 * 구독 서비스 등록/수정 요청용 DTO
 * 
 * 수학적 구조:
 * - 계층 간 데이터 전송 객체 (Data Transfer Object)
 * - 프론트엔드 → 백엔드 Controller → Service 계층으로 데이터 전달
 * 
 * 호출 경로: SubscriptionForm 컴포넌트
 *          → services/api.ts.createSubscription()
 *          → POST /api/subscriptions
 *          → SubscriptionController.create()
 *          → SubscriptionService.createSubscription()
 * 
 * 검증 규칙:
 * - serviceName: 필수, 공백 불가, 최대 100자
 * - price: 필수, 0보다 커야 함, 소수점 2자리까지
 * - billingCycle: 필수
 * - billingDate: 필수 (YYYY-MM-DD 형식)
 * - userId: 필수, 양수
 */
export interface SubscriptionRequestDto {
  /** 사용자 ID (양의 정수, 1 이상) */
  userId: number;
  
  /** 서비스 이름 (비어있지 않은 문자열, 최대 100자) */
  serviceName: string;
  
  /** 가격 (0보다 큰 값, 소수점 2자리까지) */
  price: number;
  
  /** 결제 주기 */
  billingCycle: BillingCycle;
  
  /** 결제일 (YYYY-MM-DD 형식, ISO 8601) */
  billingDate: string; // 예: "2024-12-15"
}

/**
 * 구독 서비스 조회 응답용 DTO
 * 
 * 수학적 구조:
 * - 계층 간 데이터 전송 객체 (Data Transfer Object)
 * - 백엔드 Service → Controller → 프론트엔드로 데이터 전달
 * 
 * 호출 경로: SubscriptionService.getSubscriptionById()
 *          → SubscriptionResponseDto 생성
 *          → SubscriptionController.getById()
 *          → ResponseEntity<SubscriptionResponseDto> 반환
 *          → services/api.ts.getSubscriptionById()
 *          → 프론트엔드 컴포넌트
 * 
 * 필드 설명:
 * - id: 구독 서비스 ID (Primary Key)
 * - serviceName: 서비스 이름
 * - price: 가격 (소수점 2자리까지)
 * - billingCycle: 결제 주기
 * - billingDate: 결제일
 * - nextBillingDate: 다음 결제일 (자동 계산됨)
 * - userId: 사용자 ID
 */
export interface SubscriptionResponseDto {
  /** 구독 서비스 ID (Primary Key, 양의 정수) */
  id: number;
  
  /** 서비스 이름 */
  serviceName: string;
  
  /** 가격 (소수점 2자리까지) */
  price: number;
  
  /** 결제 주기 */
  billingCycle: BillingCycle;
  
  /** 결제일 (YYYY-MM-DD 형식) */
  billingDate: string;
  
  /** 
   * 다음 결제일 (자동 계산됨)
   * 
   * 수학적 구조:
   * - 계산 공식: nextBillingDate = billingDate + 주기
   * - MONTHLY: billingDate + 1개월
   * - QUARTERLY: billingDate + 3개월
   * - YEARLY: billingDate + 1년
   * 
   * 호출 경로: 백엔드 DateCalculator.calculateNextBillingDate()에서 계산
   */
  nextBillingDate: string;
  
  /** 사용자 ID (양의 정수) */
  userId: number;
}

/**
 * 월별 지출액 조회 응답용 DTO
 * 
 * 수학적 구조:
 * - 계층 간 데이터 전송 객체 (Data Transfer Object)
 * - 백엔드 Service → Controller → 프론트엔드로 데이터 전달
 * 
 * 호출 경로: SubscriptionService.getMonthlyExpense()
 *          → 모든 구독의 월 환산 금액 합계 계산
 *          → MonthlyExpenseDto 생성
 *          → SubscriptionController.getMonthlyExpense()
 *          → ResponseEntity<MonthlyExpenseDto> 반환
 *          → services/api.ts.getMonthlyExpense()
 *          → MonthlyExpenseChart 컴포넌트
 * 
 * 월 환산 계산 공식:
 * - MONTHLY: price (그대로)
 * - QUARTERLY: price ÷ 3
 * - YEARLY: price ÷ 12
 * 
 * 최종 합계: 모든 구독의 월 환산 금액을 합산
 * totalMonthlyExpense = Σ(각 구독의 월 환산 금액)
 */
export interface MonthlyExpenseDto {
  /** 사용자 ID (양의 정수, 1 이상) */
  userId: number;
  
  /** 
   * 월별 총 지출액 (모든 구독의 월 환산 금액 합계)
   * 
   * 수학적 구조: 소수점 2자리까지
   * 
   * 호출 경로: 백엔드 SubscriptionService.getMonthlyExpense()
   *          → 각 구독의 월 환산 금액 계산
   *          → BigDecimal.add()로 합산
   */
  totalMonthlyExpense: number;
}

/**
 * 사용자 정보
 * 
 * 수학적 구조:
 * - 1:N 관계의 1 (One)
 * - User 1개 : Subscription N개
 * 
 * 호출 경로: 백엔드 User 엔티티와 일치
 */
export interface User {
  /** 사용자 ID (Primary Key, 자동 증가 정수) */
  id: number;
  
  /** 이메일 (고유 식별자) */
  email: string;
  
  /** 비밀번호 */
  password: string;
  
  /** 사용자 이름 */
  name: string;
}

