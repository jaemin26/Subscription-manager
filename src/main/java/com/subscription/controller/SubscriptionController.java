package com.subscription.controller;

import com.subscription.dto.MonthlyExpenseDto;
import com.subscription.dto.SubscriptionRequestDto;
import com.subscription.dto.SubscriptionResponseDto;
import com.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SubscriptionController
 * 
 * 구독 서비스 REST API Controller
 * 
 * 수학적 구조:
 * - RESTful API 엔드포인트 제공
 * - HTTP 메서드와 URL 경로로 리소스 조작
 * - Controller → Service → Repository 계층 구조
 * 
 * 호출 경로:
 * - HTTP 요청 → SubscriptionController
 *          → SubscriptionService
 *          → SubscriptionRepository
 *          → Database
 * 
 * 규칙:
 * - Controller는 Service만 호출, Repository 직접 호출 금지
 * - @Valid로 요청 검증 필수
 * - ResponseEntity로 응답 반환
 * - 예외는 @ExceptionHandler로 처리
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 구독 서비스 등록
     * 
     * 수학적 구조:
     * - 입력: SubscriptionRequestDto (JSON)
     * - 출력: SubscriptionResponseDto (JSON)
     * - HTTP 상태 코드: 201 Created
     * 
     * 호출 경로: POST /api/subscriptions
     *          → SubscriptionController.create()
     *          → @Valid SubscriptionRequestDto 검증
     *          → SubscriptionService.createSubscription()
     *          → SubscriptionResponseDto 반환
     * 
     * @param requestDto 구독 서비스 등록 요청 DTO
     * @return ResponseEntity<SubscriptionResponseDto> 등록된 구독 서비스 정보 (201 Created)
     */
    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> create(
            @Valid @RequestBody SubscriptionRequestDto requestDto
    ) {
        SubscriptionResponseDto response = subscriptionService.createSubscription(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 구독 서비스 목록 조회 (사용자별)
     * 
     * 수학적 구조:
     * - 입력: userId (Query Parameter)
     * - 출력: List<SubscriptionResponseDto> (JSON)
     * - HTTP 상태 코드: 200 OK
     * 
     * 호출 경로: GET /api/subscriptions?userId={id}
     *          → SubscriptionController.getSubscriptions()
     *          → SubscriptionService.getSubscriptionsByUserId(userId)
     *          → List<SubscriptionResponseDto> 반환
     * 
     * @param userId 사용자 ID (Query Parameter)
     * @return ResponseEntity<List<SubscriptionResponseDto>> 구독 서비스 목록 (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDto>> getSubscriptions(
            @RequestParam Long userId
    ) {
        List<SubscriptionResponseDto> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * 구독 서비스 상세 조회
     * 
     * 수학적 구조:
     * - 입력: id (Path Variable)
     * - 출력: SubscriptionResponseDto (JSON)
     * - HTTP 상태 코드: 200 OK
     * 
     * 호출 경로: GET /api/subscriptions/{id}
     *          → SubscriptionController.getById()
     *          → SubscriptionService.getSubscriptionById(id)
     *          → SubscriptionResponseDto 반환
     * 
     * @param id 구독 서비스 ID (Path Variable)
     * @return ResponseEntity<SubscriptionResponseDto> 구독 서비스 정보 (200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> getById(
            @PathVariable Long id
    ) {
        SubscriptionResponseDto response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 구독 서비스 수정
     * 
     * 수학적 구조:
     * - 입력: id (Path Variable), SubscriptionRequestDto (JSON)
     * - 출력: SubscriptionResponseDto (JSON)
     * - HTTP 상태 코드: 200 OK
     * 
     * 호출 경로: PUT /api/subscriptions/{id}
     *          → SubscriptionController.update()
     *          → @Valid SubscriptionRequestDto 검증
     *          → SubscriptionService.updateSubscription(id, requestDto)
     *          → SubscriptionResponseDto 반환
     * 
     * @param id 구독 서비스 ID (Path Variable)
     * @param requestDto 구독 서비스 수정 요청 DTO
     * @return ResponseEntity<SubscriptionResponseDto> 수정된 구독 서비스 정보 (200 OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionRequestDto requestDto
    ) {
        SubscriptionResponseDto response = subscriptionService.updateSubscription(id, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 구독 서비스 삭제
     * 
     * 수학적 구조:
     * - 입력: id (Path Variable)
     * - 출력: void (응답 본문 없음)
     * - HTTP 상태 코드: 204 No Content
     * 
     * 호출 경로: DELETE /api/subscriptions/{id}
     *          → SubscriptionController.delete()
     *          → SubscriptionService.deleteSubscription(id)
     *          → 204 No Content 반환
     * 
     * @param id 구독 서비스 ID (Path Variable)
     * @return ResponseEntity<Void> 삭제 성공 (204 No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 결제 임박 구독 서비스 조회 (3일 이내)
     * 
     * 수학적 구조:
     * - 입력: userId (Query Parameter)
     * - 출력: List<SubscriptionResponseDto> (JSON)
     * - HTTP 상태 코드: 200 OK
     * - 조건: today <= nextBillingDate <= today + 3일
     * 
     * 호출 경로: GET /api/subscriptions/upcoming?userId={id}
     *          → SubscriptionController.getUpcomingSubscriptions()
     *          → SubscriptionService.getUpcomingSubscriptions(userId)
     *          → List<SubscriptionResponseDto> 반환
     * 
     * @param userId 사용자 ID (Query Parameter)
     * @return ResponseEntity<List<SubscriptionResponseDto>> 결제 임박 구독 서비스 목록 (200 OK)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<SubscriptionResponseDto>> getUpcomingSubscriptions(
            @RequestParam Long userId
    ) {
        List<SubscriptionResponseDto> subscriptions = subscriptionService.getUpcomingSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * 월별 지출액 조회
     * 
     * 수학적 구조:
     * - 입력: userId (Query Parameter)
     * - 출력: MonthlyExpenseDto (JSON)
     * - HTTP 상태 코드: 200 OK
     * - 계산 공식: totalMonthlyExpense = Σ(각 구독의 월 환산 금액)
     * 
     * 호출 경로: GET /api/subscriptions/monthly-expense?userId={id}
     *          → SubscriptionController.getMonthlyExpense()
     *          → SubscriptionService.getMonthlyExpense(userId)
     *          → MonthlyExpenseDto 반환
     * 
     * @param userId 사용자 ID (Query Parameter)
     * @return ResponseEntity<MonthlyExpenseDto> 월별 지출액 정보 (200 OK)
     */
    @GetMapping("/monthly-expense")
    public ResponseEntity<MonthlyExpenseDto> getMonthlyExpense(
            @RequestParam Long userId
    ) {
        MonthlyExpenseDto response = subscriptionService.getMonthlyExpense(userId);
        return ResponseEntity.ok(response);
    }
}

