package com.subscription.service;

import com.subscription.dto.MonthlyExpenseDto;
import com.subscription.dto.SubscriptionRequestDto;
import com.subscription.dto.SubscriptionResponseDto;
import com.subscription.entity.BillingCycle;
import com.subscription.entity.Subscription;
import com.subscription.entity.User;
import com.subscription.repository.SubscriptionRepository;
import com.subscription.repository.UserRepository;
import com.subscription.util.DateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Subscription Service 클래스
 * 
 * 역할: 구독 서비스 관련 비즈니스 로직 처리
 * 
 * 수학적 구조:
 * - Service 계층: Controller와 Repository 사이의 중간 계층
 * - 비즈니스 로직 처리 및 DTO ↔ Entity 변환 담당
 * 
 * 호출 경로: SubscriptionController.create()
 *          → SubscriptionService.createSubscription()
 *          → SubscriptionRepository.save()
 *          → DateCalculator.calculateNextBillingDate()
 * 
 * 규칙:
 * - Service는 Repository와 DateCalculator만 호출
 * - Controller는 Service만 호출
 * - 모든 금액 계산은 BigDecimal 사용
 * - 호출 경로 주석 및 수학적 구조 주석 필수
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * 구독 서비스 등록
     * 
     * 수학적 구조:
     * - 입력: SubscriptionRequestDto (serviceName, price, billingCycle, billingDate, userId)
     * - 출력: SubscriptionResponseDto (id 포함)
     * - 다음 결제일 자동 계산: nextBillingDate = DateCalculator.calculateNextBillingDate(billingDate, billingCycle)
     * 
     * 호출 경로: SubscriptionController.create()
     *          → SubscriptionService.createSubscription(SubscriptionRequestDto)
     *          → UserRepository.findById(userId) (User 존재 확인)
     *          → DateCalculator.calculateNextBillingDate(billingDate, billingCycle) (다음 결제일 계산)
     *          → Subscription 엔티티 생성 및 저장
     *          → SubscriptionRepository.save(Subscription)
     *          → SubscriptionResponseDto 변환 및 반환
     * 
     * @param requestDto 구독 서비스 등록 요청 DTO
     * @return SubscriptionResponseDto 등록된 구독 서비스 정보
     * @throws IllegalArgumentException User가 존재하지 않는 경우
     */
    @Transactional
    public SubscriptionResponseDto createSubscription(SubscriptionRequestDto requestDto) {
        // User 존재 확인
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + requestDto.getUserId()));

        // 다음 결제일 자동 계산
        LocalDate nextBillingDate = DateCalculator.calculateNextBillingDate(
                requestDto.getBillingDate(),
                requestDto.getBillingCycle()
        );

        // Subscription 엔티티 생성
        Subscription subscription = new Subscription();
        subscription.setServiceName(requestDto.getServiceName());
        subscription.setPrice(requestDto.getPrice());
        subscription.setBillingCycle(requestDto.getBillingCycle());
        subscription.setBillingDate(requestDto.getBillingDate());
        subscription.setNextBillingDate(nextBillingDate);
        subscription.setUser(user);

        // 저장
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // DTO 변환 및 반환
        return toResponseDto(savedSubscription);
    }

    /**
     * 구독 서비스 목록 조회 (사용자별)
     * 
     * 수학적 구조:
     * - 입력: userId (Long)
     * - 출력: List<SubscriptionResponseDto> (0개 이상)
     * - User 1개 : Subscription N개 (1:N 관계)
     * 
     * 호출 경로: SubscriptionController.getSubscriptions()
     *          → SubscriptionService.getSubscriptionsByUserId(userId)
     *          → SubscriptionRepository.findByUserId(userId)
     *          → List<Subscription> → List<SubscriptionResponseDto> 변환
     * 
     * @param userId 사용자 ID
     * @return List<SubscriptionResponseDto> 해당 사용자의 구독 서비스 목록
     */
    public List<SubscriptionResponseDto> getSubscriptionsByUserId(Long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptions.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 구독 서비스 상세 조회
     * 
     * 수학적 구조:
     * - 입력: id (Long)
     * - 출력: SubscriptionResponseDto (1개 또는 null)
     * 
     * 호출 경로: SubscriptionController.getById()
     *          → SubscriptionService.getSubscriptionById(id)
     *          → SubscriptionRepository.findById(id)
     *          → SubscriptionResponseDto 변환 및 반환
     * 
     * @param id 구독 서비스 ID
     * @return SubscriptionResponseDto 구독 서비스 정보
     * @throws IllegalArgumentException 구독 서비스를 찾을 수 없는 경우
     */
    public SubscriptionResponseDto getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("구독 서비스를 찾을 수 없습니다. id: " + id));
        return toResponseDto(subscription);
    }

    /**
     * 구독 서비스 수정
     * 
     * 수학적 구조:
     * - 입력: id (Long), SubscriptionRequestDto
     * - 출력: SubscriptionResponseDto
     * - 다음 결제일 재계산: nextBillingDate = DateCalculator.calculateNextBillingDate(billingDate, billingCycle)
     * 
     * 호출 경로: SubscriptionController.update()
     *          → SubscriptionService.updateSubscription(id, SubscriptionRequestDto)
     *          → SubscriptionRepository.findById(id) (기존 구독 조회)
     *          → DateCalculator.calculateNextBillingDate(billingDate, billingCycle) (다음 결제일 재계산)
     *          → Subscription 엔티티 업데이트
     *          → SubscriptionRepository.save(Subscription)
     *          → SubscriptionResponseDto 변환 및 반환
     * 
     * @param id 구독 서비스 ID
     * @param requestDto 구독 서비스 수정 요청 DTO
     * @return SubscriptionResponseDto 수정된 구독 서비스 정보
     * @throws IllegalArgumentException 구독 서비스를 찾을 수 없는 경우
     */
    @Transactional
    public SubscriptionResponseDto updateSubscription(Long id, SubscriptionRequestDto requestDto) {
        // 기존 구독 조회
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("구독 서비스를 찾을 수 없습니다. id: " + id));

        // User 존재 확인
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + requestDto.getUserId()));

        // 다음 결제일 재계산
        LocalDate nextBillingDate = DateCalculator.calculateNextBillingDate(
                requestDto.getBillingDate(),
                requestDto.getBillingCycle()
        );

        // 엔티티 업데이트
        subscription.setServiceName(requestDto.getServiceName());
        subscription.setPrice(requestDto.getPrice());
        subscription.setBillingCycle(requestDto.getBillingCycle());
        subscription.setBillingDate(requestDto.getBillingDate());
        subscription.setNextBillingDate(nextBillingDate);
        subscription.setUser(user);

        // 저장
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // DTO 변환 및 반환
        return toResponseDto(savedSubscription);
    }

    /**
     * 구독 서비스 삭제
     * 
     * 수학적 구조:
     * - 입력: id (Long)
     * - 출력: void
     * 
     * 호출 경로: SubscriptionController.delete()
     *          → SubscriptionService.deleteSubscription(id)
     *          → SubscriptionRepository.findById(id) (존재 확인)
     *          → SubscriptionRepository.deleteById(id)
     * 
     * @param id 구독 서비스 ID
     * @throws IllegalArgumentException 구독 서비스를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteSubscription(Long id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new IllegalArgumentException("구독 서비스를 찾을 수 없습니다. id: " + id);
        }
        subscriptionRepository.deleteById(id);
    }

    /**
     * 결제 임박 구독 서비스 조회 (3일 이내)
     * 
     * 수학적 구조:
     * - 입력: userId (Long)
     * - 출력: List<SubscriptionResponseDto> (0개 이상)
     * - 조건: today <= nextBillingDate <= today + 3일
     * 
     * 호출 경로: SubscriptionController.getUpcomingSubscriptions()
     *          → SubscriptionService.getUpcomingSubscriptions(userId)
     *          → LocalDate.now() (오늘 날짜)
     *          → LocalDate.now().plusDays(3) (3일 후)
     *          → SubscriptionRepository.findByUserIdAndNextBillingDateBetween(userId, today, threeDaysLater)
     *          → List<Subscription> → List<SubscriptionResponseDto> 변환
     * 
     * @param userId 사용자 ID
     * @return List<SubscriptionResponseDto> 3일 이내 결제 예정인 구독 서비스 목록
     */
    public List<SubscriptionResponseDto> getUpcomingSubscriptions(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndNextBillingDateBetween(
                userId,
                today,
                threeDaysLater
        );
        
        return subscriptions.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 월별 지출액 계산
     * 
     * 수학적 구조:
     * - 입력: userId (Long)
     * - 출력: MonthlyExpenseDto (totalMonthlyExpense)
     * - 계산 공식:
     *   - MONTHLY: price (그대로)
     *   - QUARTERLY: price ÷ 3
     *   - YEARLY: price ÷ 12
     * - 최종 합계: totalMonthlyExpense = Σ(각 구독의 월 환산 금액)
     * 
     * 호출 경로: SubscriptionController.getMonthlyExpense()
     *          → SubscriptionService.getMonthlyExpense(userId)
     *          → SubscriptionRepository.findByUserId(userId) (모든 구독 조회)
     *          → 각 구독의 월 환산 금액 계산 (calculateMonthlyAmount)
     *          → BigDecimal.add()로 합산
     *          → MonthlyExpenseDto 생성 및 반환
     * 
     * @param userId 사용자 ID
     * @return MonthlyExpenseDto 월별 총 지출액
     */
    public MonthlyExpenseDto getMonthlyExpense(Long userId) {
        // 사용자의 모든 구독 조회
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        
        // 각 구독의 월 환산 금액을 합산
        BigDecimal totalMonthlyExpense = subscriptions.stream()
                .map(this::calculateMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // MonthlyExpenseDto 생성 및 반환
        MonthlyExpenseDto dto = new MonthlyExpenseDto();
        dto.setUserId(userId);
        dto.setTotalMonthlyExpense(totalMonthlyExpense);
        
        return dto;
    }

    /**
     * 구독의 월 환산 금액 계산
     * 
     * 수학적 구조:
     * - 입력: Subscription (price, billingCycle)
     * - 출력: BigDecimal (월 환산 금액)
     * - 계산 공식:
     *   - MONTHLY: price (그대로)
     *   - QUARTERLY: price ÷ 3
     *   - YEARLY: price ÷ 12
     * - 소수점 2자리까지 반올림 (RoundingMode.HALF_UP)
     * 
     * 호출 경로: SubscriptionService.getMonthlyExpense()
     *          → calculateMonthlyAmount(Subscription)
     *          → BigDecimal.divide() (QUARTERLY, YEARLY의 경우)
     * 
     * @param subscription 구독 서비스 엔티티
     * @return BigDecimal 월 환산 금액
     */
    private BigDecimal calculateMonthlyAmount(Subscription subscription) {
        BigDecimal price = subscription.getPrice();
        BillingCycle billingCycle = subscription.getBillingCycle();
        
        return switch (billingCycle) {
            case MONTHLY -> price;
            case QUARTERLY -> price.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case YEARLY -> price.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        };
    }

    /**
     * Subscription 엔티티를 SubscriptionResponseDto로 변환
     * 
     * 수학적 구조:
     * - 입력: Subscription (Entity)
     * - 출력: SubscriptionResponseDto (DTO)
     * - 1:1 매핑 (모든 필드 복사)
     * 
     * 호출 경로: SubscriptionService의 모든 조회 메서드
     *          → toResponseDto(Subscription)
     *          → SubscriptionResponseDto 생성 및 필드 복사
     * 
     * @param subscription 구독 서비스 엔티티
     * @return SubscriptionResponseDto 구독 서비스 응답 DTO
     */
    private SubscriptionResponseDto toResponseDto(Subscription subscription) {
        SubscriptionResponseDto dto = new SubscriptionResponseDto();
        dto.setId(subscription.getId());
        dto.setServiceName(subscription.getServiceName());
        dto.setPrice(subscription.getPrice());
        dto.setBillingCycle(subscription.getBillingCycle());
        dto.setBillingDate(subscription.getBillingDate());
        dto.setNextBillingDate(subscription.getNextBillingDate());
        dto.setUserId(subscription.getUser().getId());
        return dto;
    }
}

