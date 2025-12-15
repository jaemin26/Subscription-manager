package com.subscription.repository;

import com.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Subscription Repository 인터페이스
 * 
 * 역할: Subscription 엔티티에 대한 데이터베이스 접근 담당
 * 
 * 호출 경로: SubscriptionService.createSubscription()
 *          → SubscriptionRepository.save()
 *          → SubscriptionService.getSubscriptionsByUserId()
 *          → SubscriptionRepository.findByUserId()
 *          → SubscriptionService.getUpcomingSubscriptions()
 *          → SubscriptionRepository.findByNextBillingDateBetween()
 * 
 * 규칙:
 * - JpaRepository 상속으로 기본 CRUD 메서드 제공
 * - Spring Data JPA의 메서드 네이밍 규칙 사용
 * - 커스텀 쿼리는 @Query 어노테이션 사용
 * - 데이터베이스 접근만 담당, 비즈니스 로직은 Service 계층에 구현
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * 사용자 ID로 구독 목록 조회
     * 
     * 수학적 구조:
     * - 입력: userId (Long)
     * - 출력: List<Subscription> (0개 이상)
     * - User 1개 : Subscription N개 (1:N 관계)
     * 
     * 호출 경로: SubscriptionService.getSubscriptionsByUserId()
     *          → SubscriptionRepository.findByUserId()
     * 
     * Spring Data JPA 메서드 네이밍 규칙:
     * - findByUserId: Subscription.user.id 필드로 조회
     * - 자동으로 JPQL 생성: SELECT s FROM Subscription s WHERE s.user.id = :userId
     * 
     * @param userId 사용자 ID
     * @return List<Subscription> - 해당 사용자의 모든 구독 목록
     */
    List<Subscription> findByUserId(Long userId);
    
    /**
     * 다음 결제일이 특정 기간 내에 있는 구독 조회
     * 
     * 수학적 구조:
     * - 입력: start (LocalDate), end (LocalDate)
     * - 출력: List<Subscription> (0개 이상)
     * - 조건: start <= nextBillingDate <= end
     * 
     * 호출 경로: SubscriptionService.getUpcomingSubscriptions()
     *          → SubscriptionRepository.findByNextBillingDateBetween()
     *          → BillingScheduler.checkUpcomingBilling() (배치 작업)
     * 
     * 사용 예시:
     * - 오늘부터 3일 이내 결제 예정 조회:
     *   LocalDate today = LocalDate.now();
     *   LocalDate threeDaysLater = today.plusDays(3);
     *   findByNextBillingDateBetween(today, threeDaysLater)
     * 
     * @param start 시작 날짜 (포함)
     * @param end 종료 날짜 (포함)
     * @return List<Subscription> - 해당 기간 내 결제 예정인 구독 목록
     */
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate BETWEEN :start AND :end")
    List<Subscription> findByNextBillingDateBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    
    /**
     * 사용자 ID와 다음 결제일 범위로 구독 조회
     * 
     * 수학적 구조:
     * - 입력: userId (Long), start (LocalDate), end (LocalDate)
     * - 출력: List<Subscription> (0개 이상)
     * - 조건: user.id = userId AND start <= nextBillingDate <= end
     * 
     * 호출 경로: SubscriptionService.getUpcomingSubscriptionsByUserId()
     *          → SubscriptionRepository.findByUserIdAndNextBillingDateBetween()
     * 
     * Spring Data JPA 메서드 네이밍 규칙:
     * - findByUserIdAndNextBillingDateBetween: 두 조건을 AND로 결합
     * - 자동으로 JPQL 생성
     * 
     * @param userId 사용자 ID
     * @param start 시작 날짜 (포함)
     * @param end 종료 날짜 (포함)
     * @return List<Subscription> - 해당 사용자의 특정 기간 내 결제 예정인 구독 목록
     */
    List<Subscription> findByUserIdAndNextBillingDateBetween(
            Long userId,
            LocalDate start,
            LocalDate end
    );
}

