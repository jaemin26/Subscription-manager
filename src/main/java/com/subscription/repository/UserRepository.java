package com.subscription.repository;

import com.subscription.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository 인터페이스
 * 
 * 역할: User 엔티티에 대한 데이터베이스 접근 담당
 * 
 * 호출 경로: SubscriptionService 등에서 User 조회
 *          → UserRepository.findById()
 *          → UserRepository.findByEmail()
 * 
 * 규칙:
 * - JpaRepository 상속으로 기본 CRUD 메서드 제공
 * - Spring Data JPA의 메서드 네이밍 규칙 사용
 * - 데이터베이스 접근만 담당, 비즈니스 로직은 Service 계층에 구현
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     * 
     * 수학적 구조: 
     * - 입력: email (String, unique)
     * - 출력: Optional<User> (0개 또는 1개)
     * - 1:1 대응 (이메일은 unique 제약조건)
     * 
     * 호출 경로: UserService.findByEmail()
     *          → UserRepository.findByEmail()
     * 
     * @param email 사용자 이메일
     * @return Optional<User> - 사용자가 존재하면 User, 없으면 empty
     */
    Optional<User> findByEmail(String email);
}

