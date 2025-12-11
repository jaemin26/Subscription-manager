package com.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 구독 서비스 매니저 애플리케이션 메인 클래스
 * 
 * 호출 경로: Spring Boot 실행 시 자동으로 이 클래스의 main 메서드 호출
 *          → SpringApplication.run() 실행
 *          → 애플리케이션 컨텍스트 초기화
 */
@SpringBootApplication
public class SubscriptionManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionManagerApplication.class, args);
    }
}

