package com.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User 엔티티
 * 
 * 수학적 구조:
 * - 1:N 관계의 1 (One)
 * - User 1개 : Subscription N개
 * 
 * 호출 경로: SubscriptionService 등에서 User 조회
 *          → UserRepository.findById()
 *          → User.getSubscriptions() (연관된 구독 목록)
 * 
 * 연관관계:
 * - @OneToMany: User 1개가 여러 Subscription을 가질 수 있음
 * - mappedBy="user": Subscription 엔티티의 user 필드와 매핑
 * - cascade=CascadeType.ALL: User 삭제 시 연관된 Subscription도 함께 삭제
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 사용자 ID (Primary Key)
     * 수학적 구조: 자동 증가 정수 (AUTO_INCREMENT)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이메일 (Unique 제약조건)
     * 수학적 구조: 고유 식별자 (1:1 대응)
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 비밀번호
     */
    @Column(nullable = false)
    private String password;

    /**
     * 사용자 이름
     */
    @Column(nullable = false)
    private String name;

    /**
     * 구독 서비스 목록 (1:N 관계)
     * 
     * 수학적 구조:
     * - User 1개 : Subscription N개 (1:N)
     * - 양방향 연관관계 (User ↔ Subscription)
     * 
     * 호출 경로: User.getSubscriptions()
     *          → SubscriptionService.getSubscriptionsByUserId()
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();
}

