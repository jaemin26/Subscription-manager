package com.subscription.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SchedulerConfig
 * 
 * 스케줄러 활성화 설정 클래스
 * 
 * 수학적 구조:
 * - Spring의 스케줄링 기능을 활성화하는 설정 클래스
 * - @EnableScheduling 어노테이션으로 스케줄링 기능 활성화
 * 
 * 호출 경로: Spring Boot 애플리케이션 시작 시
 *          → @Configuration 클래스 스캔
 *          → SchedulerConfig 로드
 *          → @EnableScheduling으로 스케줄러 활성화
 *          → @Scheduled 메서드가 자동으로 실행됨
 * 
 * 규칙:
 * - @Configuration: Spring 설정 클래스로 인식
 * - @EnableScheduling: 스케줄링 기능 활성화
 * - 이 클래스가 있어야 @Scheduled 어노테이션이 작동함
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 설정 클래스이므로 별도의 메서드 없이 어노테이션만으로 기능함
}

