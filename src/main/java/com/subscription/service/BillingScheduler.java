package com.subscription.service;

import com.subscription.entity.Subscription;
import com.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * BillingScheduler
 * 
 * 결제 임박 구독 서비스 배치 작업 스케줄러
 * 
 * 수학적 구조:
 * - 매일 오전 9시에 실행되는 배치 작업
 * - 오늘부터 3일 이내 결제 예정인 구독 조회
 * - 조건: today <= nextBillingDate <= today + 3일
 * 
 * 호출 경로: Spring Boot 애플리케이션 시작 시
 *          → SchedulerConfig에 의해 스케줄러 활성화
 *          → @Scheduled(cron = "0 0 9 * * *") 메서드가 매일 오전 9시에 자동 실행
 *          → BillingScheduler.checkUpcomingBilling()
 *          → SubscriptionRepository.findByNextBillingDateBetween()
 *          → 콘솔에 로그 출력
 * 
 * 규칙:
 * - @Scheduled: Spring의 스케줄링 기능 사용
 * - cron 표현식: "0 0 9 * * *" (매일 오전 9시)
 * - 테스트 시 cron을 "0 * * * * *" (매분) 또는 "0/10 * * * * *" (10초마다)로 변경 가능
 * - 호출 경로 주석 필수
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingScheduler {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 결제 임박 구독 서비스 확인 배치 작업
     * 
     * 수학적 구조:
     * - 입력: 없음 (자동 실행)
     * - 출력: 콘솔 로그
     * - 조건: today <= nextBillingDate <= today + 3일
     * - 조회된 구독 수: 0개 이상
     * 
     * 호출 경로: Spring 스케줄러
     *          → @Scheduled(cron = "0 0 9 * * *") (매일 오전 9시)
     *          → BillingScheduler.checkUpcomingBilling()
     *          → LocalDate.now() (오늘 날짜)
     *          → LocalDate.now().plusDays(3) (3일 후)
     *          → SubscriptionRepository.findByNextBillingDateBetween(today, threeDaysLater)
     *          → List<Subscription> 조회
     *          → 콘솔에 로그 출력
     * 
     * cron 표현식 설명:
     * - "0 0 9 * * *": 초(0) 분(0) 시(9) 일(*) 월(*) 요일(*)
     * - 매일 오전 9시 0분 0초에 실행
     * 
 * 테스트용 cron 표현식:
 * - "0 * * * * *": 매분 0초에 실행 (즉시 테스트 가능)
 * - "0/10 * * * * *": 10초마다 실행 (빠른 테스트)
     */
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시에 실행
    public void checkUpcomingBilling() {
        log.info("=== 결제 임박 구독 서비스 확인 배치 작업 시작 ===");
        
        // 오늘 날짜와 3일 후 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        
        log.info("조회 기간: {} ~ {}", today, threeDaysLater);
        
        // 3일 이내 결제 예정인 구독 조회
        List<Subscription> upcomingSubscriptions = subscriptionRepository.findByNextBillingDateBetween(
                today,
                threeDaysLater
        );
        
        // 결과 로그 출력
        if (upcomingSubscriptions.isEmpty()) {
            log.info("결제 임박 구독 서비스가 없습니다.");
        } else {
            log.info("결제 임박 구독 서비스 {}개를 찾았습니다:", upcomingSubscriptions.size());
            
            for (Subscription subscription : upcomingSubscriptions) {
                long daysUntilBilling = java.time.temporal.ChronoUnit.DAYS.between(
                        today,
                        subscription.getNextBillingDate()
                );
                
                log.info("  - 서비스명: {}, 가격: {}원, 결제일: {} ({}일 후), 사용자 ID: {}",
                        subscription.getServiceName(),
                        subscription.getPrice(),
                        subscription.getNextBillingDate(),
                        daysUntilBilling,
                        subscription.getUser().getId()
                );
            }
        }
        
        log.info("=== 결제 임박 구독 서비스 확인 배치 작업 종료 ===");
    }
}

