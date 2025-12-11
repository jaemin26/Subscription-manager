# 💸 나의 구독 서비스 매니저 (Subscription Manager) - 프로젝트 계획서

## 📋 프로젝트 개요

**프로젝트명:** 나의 구독 서비스 매니저 (Subscription Manager)

**목적:** 넷플릭스, 유튜브, 헬스장 등 정기 결제 서비스를 효율적으로 관리하는 웹 애플리케이션 개발

**개발 기간:** 7일

**기술 스택:**
- **백엔드:** Spring Boot 3.x + Java 17
- **프론트엔드:** React + TypeScript (또는 자유 선택)
- **데이터베이스:** H2 (개발) / MySQL (프로덕션)
- **ORM:** Spring Data JPA

---

## 🎯 핵심 기능

### 1. 구독 서비스 등록 및 관리
- 서비스명, 결제 금액, 결제 주기, 결제일 등록
- 구독 서비스 수정 및 삭제
- 구독 목록 조회

### 2. 월별 지출액 자동 계산
- 결제 주기가 '매월'이 아닌 경우 월 환산 로직 적용
- 연간 결제: 금액 ÷ 12
- 분기별 결제: 금액 ÷ 3
- 월별 총 지출액 계산

### 3. 결제일 임박 알림
- 오늘 날짜 기준 3일 이내 결제 예정 서비스 조회
- API를 통한 실시간 조회
- 배치 작업을 통한 자동 알림 (콘솔 로그)

### 4. 다음 결제일 자동 계산
- Java 8의 `LocalDate`를 활용한 날짜 계산
- 결제 주기에 따른 자동 계산:
  - 매월: +1개월
  - 분기별: +3개월
  - 연간: +1년

### 5. 배치 작업 (Batch)
- Spring의 `@Scheduled`를 사용한 스케줄링
- 매일 오전 9시에 결제 임박 건을 콘솔에 로그 출력
- 실제 서비스에서는 이메일 발송으로 확장 가능

### 6. JPA 연관관계 학습
- User 1명이 N개의 Subscription을 가지는 1:N 구조
- `@OneToMany`, `@ManyToOne` 어노테이션 활용

---

## 🛠 기술 스택 상세

### 백엔드
- **Spring Boot 3.2.x** - 웹 애플리케이션 프레임워크
- **Spring Data JPA** - 데이터베이스 접근 계층
- **H2 Database** - 개발용 인메모리 데이터베이스
- **Lombok** - 보일러플레이트 코드 감소
- **Spring Web** - RESTful API 개발
- **Spring Boot Scheduler** - 배치 작업

### 프론트엔드 (추천)
- **React 18** + **TypeScript** - 현대적인 UI 라이브러리
- **Vite** - 빠른 개발 환경
- **Axios** - HTTP 클라이언트
- **Recharts** - 차트 시각화 (월별 지출)

### 대안 프론트엔드
- **Thymeleaf** - 서버 사이드 렌더링 (Spring과 통합 용이)
- **Vue.js** - 다른 선택지

---

## 📁 프로젝트 구조

```
subscription-manager/
├── src/main/java/com/subscription/
│   ├── SubscriptionManagerApplication.java
│   ├── config/
│   │   └── SchedulerConfig.java          # @EnableScheduling
│   ├── controller/
│   │   └── SubscriptionController.java  # REST API
│   ├── service/
│   │   ├── SubscriptionService.java     # 비즈니스 로직
│   │   └── BillingScheduler.java        # @Scheduled 배치
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── SubscriptionRepository.java
│   ├── entity/
│   │   ├── User.java                    # 1:N 관계의 1
│   │   ├── Subscription.java            # 1:N 관계의 N
│   │   └── BillingCycle.java            # Enum (MONTHLY, QUARTERLY, YEARLY)
│   ├── dto/
│   │   ├── SubscriptionRequestDto.java
│   │   ├── SubscriptionResponseDto.java
│   │   └── MonthlyExpenseDto.java
│   └── util/
│       └── DateCalculator.java          # LocalDate 계산 로직
├── src/main/resources/
│   ├── application.properties
│   └── data.sql                         # 초기 데이터 (선택사항)
├── frontend/                            # React 프로젝트
│   ├── src/
│   │   ├── components/
│   │   │   ├── SubscriptionList.tsx
│   │   │   ├── SubscriptionForm.tsx
│   │   │   └── UpcomingBilling.tsx
│   │   ├── pages/
│   │   └── services/
│   │       └── api.ts
│   └── package.json
└── pom.xml                              # Maven 의존성
```

---

## 📅 개발 일정 (7일)

### Day 1: Spring Boot 프로젝트 초기 설정
**목표:** 프로젝트 생성 및 기본 구조 설정

- [ ] Spring Initializr로 프로젝트 생성
  - Group: `com.subscription`
  - Artifact: `subscription-manager`
  - Dependencies: Spring Web, Spring Data JPA, H2 Database, Lombok
- [ ] 프로젝트 구조 생성 (controller, service, repository, entity, dto, util)
- [ ] `application.properties` 설정
  - H2 Database 설정
  - JPA 설정 (ddl-auto: update)
  - 로깅 설정
- [ ] 기본 애플리케이션 실행 테스트

**산출물:**
- 실행 가능한 Spring Boot 애플리케이션
- 기본 프로젝트 구조

---

### Day 2: JPA 엔티티 설계 및 1:N 연관관계 구현
**목표:** 데이터베이스 스키마 설계 및 연관관계 매핑

- [ ] **User 엔티티 생성**
  ```java
  @Entity
  @Table(name = "users")
  public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      
      @Column(unique = true)
      private String email;
      
      private String password;
      private String name;
      
      @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
      private List<Subscription> subscriptions = new ArrayList<>();
  }
  ```

- [ ] **Subscription 엔티티 생성**
  ```java
  @Entity
  @Table(name = "subscriptions")
  public class Subscription {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      
      private String serviceName;
      private BigDecimal price;
      
      @Enumerated(EnumType.STRING)
      private BillingCycle billingCycle;
      
      private LocalDate billingDate;
      private LocalDate nextBillingDate;
      
      @ManyToOne
      @JoinColumn(name = "user_id")
      private User user;
  }
  ```

- [ ] **BillingCycle Enum 생성**
  ```java
  public enum BillingCycle {
      MONTHLY,    // 매월
      QUARTERLY,  // 분기별
      YEARLY      // 연간
  }
  ```

- [ ] 연관관계 테스트

**산출물:**
- User, Subscription 엔티티
- 1:N 연관관계 매핑 완료

---

### Day 3: Repository 계층 구현
**목표:** 데이터 접근 계층 구현

- [ ] **UserRepository 인터페이스**
  ```java
  public interface UserRepository extends JpaRepository<User, Long> {
      Optional<User> findByEmail(String email);
  }
  ```

- [ ] **SubscriptionRepository 인터페이스**
  ```java
  public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
      List<Subscription> findByUserId(Long userId);
      
      @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate BETWEEN :start AND :end")
      List<Subscription> findByNextBillingDateBetween(
          @Param("start") LocalDate start, 
          @Param("end") LocalDate end
      );
      
      List<Subscription> findByUserIdAndNextBillingDateBetween(
          Long userId, LocalDate start, LocalDate end
      );
  }
  ```

- [ ] Repository 메서드 테스트

**산출물:**
- UserRepository, SubscriptionRepository
- 커스텀 쿼리 메서드

---

### Day 4: 날짜 계산 유틸리티 및 Service 계층 구현
**목표:** 비즈니스 로직 구현

- [ ] **DateCalculator 유틸리티 클래스**
  ```java
  public class DateCalculator {
      public static LocalDate calculateNextBillingDate(
          LocalDate billingDate, 
          BillingCycle cycle
      ) {
          return switch (cycle) {
              case MONTHLY -> billingDate.plusMonths(1);
              case QUARTERLY -> billingDate.plusMonths(3);
              case YEARLY -> billingDate.plusYears(1);
          };
      }
  }
  ```

- [ ] **SubscriptionService 구현**
  - `createSubscription()`: 다음 결제일 자동 계산 후 저장
  - `getMonthlyExpense()`: 모든 구독의 월 환산 금액 합계
    ```java
    public BigDecimal getMonthlyExpense(Long userId) {
        List<Subscription> subscriptions = repository.findByUserId(userId);
        return subscriptions.stream()
            .map(this::calculateMonthlyExpense)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateMonthlyExpense(Subscription sub) {
        return switch (sub.getBillingCycle()) {
            case MONTHLY -> sub.getPrice();
            case QUARTERLY -> sub.getPrice().divide(
                BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP
            );
            case YEARLY -> sub.getPrice().divide(
                BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP
            );
        };
    }
    ```
  - `getUpcomingSubscriptions()`: 3일 이내 결제 예정 조회

**산출물:**
- DateCalculator 유틸리티
- SubscriptionService 비즈니스 로직

---

### Day 5: REST API Controller 구현
**목표:** RESTful API 엔드포인트 구현

- [ ] **DTO 클래스 생성**
  - `SubscriptionRequestDto`: 등록/수정용
  - `SubscriptionResponseDto`: 조회용
  - `MonthlyExpenseDto`: 월별 지출용

- [ ] **SubscriptionController 구현**
  ```java
  @RestController
  @RequestMapping("/api/subscriptions")
  public class SubscriptionController {
      
      @PostMapping
      public ResponseEntity<SubscriptionResponseDto> create(
          @Valid @RequestBody SubscriptionRequestDto request
      ) { ... }
      
      @GetMapping
      public ResponseEntity<List<SubscriptionResponseDto>> getAll(
          @RequestParam Long userId
      ) { ... }
      
      @GetMapping("/upcoming")
      public ResponseEntity<List<SubscriptionResponseDto>> getUpcoming(
          @RequestParam Long userId
      ) { ... }
      
      @GetMapping("/monthly-expense")
      public ResponseEntity<MonthlyExpenseDto> getMonthlyExpense(
          @RequestParam Long userId
      ) { ... }
      
      @GetMapping("/{id}")
      public ResponseEntity<SubscriptionResponseDto> getById(@PathVariable Long id) { ... }
      
      @PutMapping("/{id}")
      public ResponseEntity<SubscriptionResponseDto> update(
          @PathVariable Long id,
          @Valid @RequestBody SubscriptionRequestDto request
      ) { ... }
      
      @DeleteMapping("/{id}")
      public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
  }
  ```

- [ ] 예외 처리 및 유효성 검사 추가

**산출물:**
- 완전한 REST API
- DTO 클래스

---

### Day 6: Spring @Scheduled 배치 작업 구현
**목표:** 배치 작업 구현 및 프론트엔드 개발

#### 배치 작업 구현
- [ ] **SchedulerConfig 클래스**
  ```java
  @Configuration
  @EnableScheduling
  public class SchedulerConfig {
  }
  ```

- [ ] **BillingScheduler 서비스**
  ```java
  @Service
  @Slf4j
  public class BillingScheduler {
      
      @Autowired
      private SubscriptionRepository repository;
      
      @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
      public void checkUpcomingBilling() {
          LocalDate today = LocalDate.now();
          LocalDate threeDaysLater = today.plusDays(3);
          
          List<Subscription> upcoming = repository
              .findByNextBillingDateBetween(today, threeDaysLater);
          
          if (upcoming.isEmpty()) {
              log.info("결제 임박 서비스가 없습니다.");
          } else {
              log.info("=== 결제 임박 서비스 (3일 이내) ===");
              upcoming.forEach(sub -> log.info(
                  "서비스: {}, 결제일: {}, 금액: {}원",
                  sub.getServiceName(),
                  sub.getNextBillingDate(),
                  sub.getPrice()
              ));
          }
      }
  }
  ```

- [ ] 배치 작업 테스트 (cron 표현식 임시 변경)

#### 프론트엔드 개발
- [ ] React 프로젝트 초기화
  ```bash
  npm create vite@latest frontend -- --template react-ts
  ```

- [ ] 주요 컴포넌트 구현
  - `SubscriptionList`: 구독 목록 표시
  - `SubscriptionForm`: 등록/수정 폼
  - `UpcomingBilling`: 결제 임박 알림
  - `MonthlyExpenseChart`: 월별 지출 차트

- [ ] API 통신 설정
  - Axios 설정
  - API 호출 함수 작성

**산출물:**
- 배치 작업 구현 완료
- 프론트엔드 기본 구조

---

### Day 7: 통합 테스트 및 문서화
**목표:** 전체 기능 테스트 및 문서 작성

- [ ] **통합 테스트**
  - 각 API 엔드포인트 테스트
  - 날짜 계산 로직 테스트
  - 배치 작업 테스트
  - 프론트엔드-백엔드 연동 테스트

- [ ] **문서 작성**
  - `README.md`: 프로젝트 소개, 실행 방법
  - `API_DOCS.md`: API 엔드포인트 상세 설명
  - Swagger 설정 (선택사항)

- [ ] 버그 수정 및 최종 점검

**산출물:**
- 완성된 애플리케이션
- 프로젝트 문서

---

## 🗄 데이터베이스 스키마

### User 테이블
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Subscription 테이블
```sql
CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,  -- MONTHLY, QUARTERLY, YEARLY
    billing_date DATE NOT NULL,
    next_billing_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 📡 API 엔드포인트 설계

### 구독 서비스 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/subscriptions` | 구독 서비스 등록 |
| GET | `/api/subscriptions?userId={id}` | 구독 목록 조회 |
| GET | `/api/subscriptions/{id}` | 구독 상세 조회 |
| PUT | `/api/subscriptions/{id}` | 구독 수정 |
| DELETE | `/api/subscriptions/{id}` | 구독 삭제 |

### 조회 기능
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/subscriptions/upcoming?userId={id}` | 결제 임박 목록 (3일 이내) |
| GET | `/api/subscriptions/monthly-expense?userId={id}` | 월별 총 지출액 |

### 요청 예시
```json
POST /api/subscriptions
{
  "userId": 1,
  "serviceName": "넷플릭스",
  "price": 9500,
  "billingCycle": "MONTHLY",
  "billingDate": "2024-12-15"
}
```

### 응답 예시
```json
GET /api/subscriptions/upcoming?userId=1
[
  {
    "id": 1,
    "serviceName": "넷플릭스",
    "price": 9500,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15",
    "nextBillingDate": "2025-01-15"
  }
]
```

---

## 🔑 핵심 구현 포인트

### 1. LocalDate를 활용한 날짜 계산
```java
// 다음 결제일 계산
LocalDate nextBillingDate = billingDate.plusMonths(1);  // 매월
LocalDate nextBillingDate = billingDate.plusMonths(3); // 분기별
LocalDate nextBillingDate = billingDate.plusYears(1);  // 연간

// 3일 이내 조회
LocalDate today = LocalDate.now();
LocalDate threeDaysLater = today.plusDays(3);
```

### 2. BigDecimal을 사용한 정확한 금액 계산
```java
// 월 환산 (연간)
BigDecimal monthlyExpense = annualPrice
    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
```

### 3. @Scheduled 배치 작업
```java
@Scheduled(cron = "0 0 9 * * *")  // 매일 오전 9시
public void scheduledTask() {
    // 작업 수행
}
```

### 4. JPA 1:N 연관관계
```java
// User 엔티티
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Subscription> subscriptions;

// Subscription 엔티티
@ManyToOne
@JoinColumn(name = "user_id")
private User user;
```

---

## 📝 체크리스트

### 개발 전
- [x] 프로젝트 계획 수립
- [ ] 기술 스택 최종 결정
- [ ] 개발 환경 설정

### 개발 중
- [ ] Day 1: 프로젝트 초기 설정
- [ ] Day 2: 엔티티 및 연관관계
- [ ] Day 3: Repository 계층
- [ ] Day 4: Service 계층 및 날짜 계산
- [ ] Day 5: REST API
- [ ] Day 6: 배치 작업 및 프론트엔드
- [ ] Day 7: 테스트 및 문서화

### 개발 후
- [ ] 통합 테스트 완료
- [ ] 문서 작성 완료
- [ ] 코드 리뷰
- [ ] 배포 준비 (선택사항)

---

## 🚀 실행 방법

### 백엔드 실행
```bash
cd subscription-manager
./mvnw spring-boot:run
# 또는
mvn spring-boot:run
```

### 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

### 배치 작업 테스트
배치 작업을 즉시 테스트하려면 `@Scheduled`의 cron 표현식을 임시로 변경:
```java
@Scheduled(fixedRate = 60000)  // 1분마다 실행 (테스트용)
```

---

## 📚 학습 포인트

이 프로젝트를 통해 학습할 수 있는 내용:

1. **Spring Boot 기초**
   - 프로젝트 구조 이해
   - 의존성 관리
   - 설정 파일 관리

2. **JPA 연관관계**
   - 1:N 관계 매핑
   - 연관관계의 주인 (mappedBy)
   - 지연 로딩 vs 즉시 로딩

3. **Java 8 날짜/시간 API**
   - LocalDate 사용법
   - 날짜 계산 메서드
   - 날짜 비교 및 조회

4. **Spring Scheduler**
   - @Scheduled 어노테이션
   - Cron 표현식
   - 배치 작업 구현

5. **RESTful API 설계**
   - HTTP 메서드 활용
   - DTO 패턴
   - 예외 처리

---

## 🔮 향후 확장 가능한 기능

- 사용자 인증/인가 (Spring Security)
- 이메일 알림 발송
- 통계 대시보드 (월별/연도별)
- 구독 서비스 카테고리 분류
- 결제 내역 기록
- 구독 취소 알림

---

## 📄 라이선스

이 프로젝트는 학습 및 포트폴리오 목적으로 제작되었습니다.

---

**작성일:** 2024년 12월  
**작성자:** 개발자  
**버전:** 1.0.0

