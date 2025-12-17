/**
 * API 서비스 레이어
 * 
 * 수학적 구조:
 * - 모든 API 호출은 이 파일에서만 수행
 * - 컴포넌트에서 직접 axios 호출 금지
 * - 프론트엔드 → 백엔드 API 통신 계층
 * 
 * 호출 경로 규칙:
 * - 컴포넌트 → services/api.ts → axios → 백엔드 API
 */

import axios from 'axios';
import type {
  SubscriptionRequestDto,
  SubscriptionResponseDto,
  MonthlyExpenseDto
} from '../types';

/**
 * axios 인스턴스 생성
 * 
 * 수학적 구조:
 * - 기본 URL 설정 (필요시 환경 변수 사용)
 * - 공통 헤더 설정
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8090',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 구독 서비스 등록
 * 
 * 수학적 구조:
 * - 입력: SubscriptionRequestDto (JSON)
 * - 출력: SubscriptionResponseDto (JSON)
 * - HTTP 상태 코드: 201 Created
 * 
 * 호출 경로: SubscriptionForm.handleSubmit()
 *          → api.createSubscription()
 *          → POST /api/subscriptions
 *          → SubscriptionController.create()
 *          → SubscriptionService.createSubscription()
 *          → SubscriptionResponseDto 반환
 * 
 * @param data 구독 서비스 등록 요청 DTO
 * @returns 등록된 구독 서비스 정보
 */
export const createSubscription = async (
  data: SubscriptionRequestDto
): Promise<SubscriptionResponseDto> => {
  try {
    const response = await apiClient.post<SubscriptionResponseDto>('/api/subscriptions', data);
    return response.data;
  } catch (error) {
    console.error('구독 서비스 생성 실패', error);
    throw error;
  }
};

/**
 * 구독 서비스 목록 조회 (사용자별)
 * 
 * 수학적 구조:
 * - 입력: userId (Query Parameter)
 * - 출력: List<SubscriptionResponseDto> (JSON)
 * - HTTP 상태 코드: 200 OK
 * 
 * 호출 경로: SubscriptionList 컴포넌트
 *          → api.getSubscriptions(userId)
 *          → GET /api/subscriptions?userId={id}
 *          → SubscriptionController.getSubscriptions()
 *          → SubscriptionService.getSubscriptionsByUserId(userId)
 *          → List<SubscriptionResponseDto> 반환
 * 
 * @param userId 사용자 ID
 * @returns 구독 서비스 목록
 */
export const getSubscriptions = async (
  userId: number
): Promise<SubscriptionResponseDto[]> => {
  try {
    const response = await apiClient.get<SubscriptionResponseDto[]>('/api/subscriptions', {
      params: { userId },
    });
    return response.data;
  } catch (error) {
    console.error('구독 서비스 목록 조회 실패', error);
    throw error;
  }
};

/**
 * 구독 서비스 상세 조회
 * 
 * 수학적 구조:
 * - 입력: id (Path Variable)
 * - 출력: SubscriptionResponseDto (JSON)
 * - HTTP 상태 코드: 200 OK
 * 
 * 호출 경로: SubscriptionDetail 컴포넌트
 *          → api.getSubscriptionById(id)
 *          → GET /api/subscriptions/{id}
 *          → SubscriptionController.getById()
 *          → SubscriptionService.getSubscriptionById(id)
 *          → SubscriptionResponseDto 반환
 * 
 * @param id 구독 서비스 ID
 * @returns 구독 서비스 정보
 */
export const getSubscriptionById = async (
  id: number
): Promise<SubscriptionResponseDto> => {
  try {
    const response = await apiClient.get<SubscriptionResponseDto>(`/api/subscriptions/${id}`);
    return response.data;
  } catch (error) {
    console.error('구독 서비스 상세 조회 실패', error);
    throw error;
  }
};

/**
 * 구독 서비스 수정
 * 
 * 수학적 구조:
 * - 입력: id (Path Variable), SubscriptionRequestDto (JSON)
 * - 출력: SubscriptionResponseDto (JSON)
 * - HTTP 상태 코드: 200 OK
 * 
 * 호출 경로: SubscriptionForm.handleSubmit() (수정 모드)
 *          → api.updateSubscription(id, data)
 *          → PUT /api/subscriptions/{id}
 *          → SubscriptionController.update()
 *          → SubscriptionService.updateSubscription(id, requestDto)
 *          → SubscriptionResponseDto 반환
 * 
 * @param id 구독 서비스 ID
 * @param data 구독 서비스 수정 요청 DTO
 * @returns 수정된 구독 서비스 정보
 */
export const updateSubscription = async (
  id: number,
  data: SubscriptionRequestDto
): Promise<SubscriptionResponseDto> => {
  try {
    const response = await apiClient.put<SubscriptionResponseDto>(`/api/subscriptions/${id}`, data);
    return response.data;
  } catch (error) {
    console.error('구독 서비스 수정 실패', error);
    throw error;
  }
};

/**
 * 구독 서비스 삭제
 * 
 * 수학적 구조:
 * - 입력: id (Path Variable)
 * - 출력: void (응답 본문 없음)
 * - HTTP 상태 코드: 204 No Content
 * 
 * 호출 경로: SubscriptionList.handleDelete() 또는 SubscriptionDetail.handleDelete()
 *          → api.deleteSubscription(id)
 *          → DELETE /api/subscriptions/{id}
 *          → SubscriptionController.delete()
 *          → SubscriptionService.deleteSubscription(id)
 *          → 204 No Content 반환
 * 
 * @param id 구독 서비스 ID
 */
export const deleteSubscription = async (id: number): Promise<void> => {
  try {
    await apiClient.delete(`/api/subscriptions/${id}`);
  } catch (error) {
    console.error('구독 서비스 삭제 실패', error);
    throw error;
  }
};

/**
 * 결제 임박 구독 서비스 조회 (3일 이내)
 * 
 * 수학적 구조:
 * - 입력: userId (Query Parameter)
 * - 출력: List<SubscriptionResponseDto> (JSON)
 * - HTTP 상태 코드: 200 OK
 * - 조건: today <= nextBillingDate <= today + 3일
 * 
 * 호출 경로: UpcomingBilling 컴포넌트
 *          → api.getUpcomingSubscriptions(userId)
 *          → GET /api/subscriptions/upcoming?userId={id}
 *          → SubscriptionController.getUpcomingSubscriptions()
 *          → SubscriptionService.getUpcomingSubscriptions(userId)
 *          → List<SubscriptionResponseDto> 반환
 * 
 * @param userId 사용자 ID
 * @returns 결제 임박 구독 서비스 목록
 */
export const getUpcomingSubscriptions = async (
  userId: number
): Promise<SubscriptionResponseDto[]> => {
  try {
    const response = await apiClient.get<SubscriptionResponseDto[]>('/api/subscriptions/upcoming', {
      params: { userId },
    });
    return response.data;
  } catch (error) {
    console.error('결제 임박 구독 서비스 조회 실패', error);
    throw error;
  }
};

/**
 * 월별 지출액 조회
 * 
 * 수학적 구조:
 * - 입력: userId (Query Parameter)
 * - 출력: MonthlyExpenseDto (JSON)
 * - HTTP 상태 코드: 200 OK
 * - 계산 공식: totalMonthlyExpense = Σ(각 구독의 월 환산 금액)
 *   - MONTHLY: price (그대로)
 *   - QUARTERLY: price ÷ 3
 *   - YEARLY: price ÷ 12
 * 
 * 호출 경로: MonthlyExpenseChart 컴포넌트
 *          → api.getMonthlyExpense(userId)
 *          → GET /api/subscriptions/monthly-expense?userId={id}
 *          → SubscriptionController.getMonthlyExpense()
 *          → SubscriptionService.getMonthlyExpense(userId)
 *          → MonthlyExpenseDto 반환
 * 
 * @param userId 사용자 ID
 * @returns 월별 지출액 정보
 */
export const getMonthlyExpense = async (
  userId: number
): Promise<MonthlyExpenseDto> => {
  try {
    const response = await apiClient.get<MonthlyExpenseDto>('/api/subscriptions/monthly-expense', {
      params: { userId },
    });
    return response.data;
  } catch (error) {
    console.error('월별 지출액 조회 실패', error);
    throw error;
  }
};
