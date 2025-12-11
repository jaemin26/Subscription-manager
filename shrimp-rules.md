# Subscription Manager - 개발 가이드라인

## 프로젝트 개요

### 기술 스택
- **백엔드**: Spring Boot 3.x + Java 17
- **프론트엔드**: React 18 + TypeScript + Vite
- **데이터베이스**: H2 (개발) / MySQL (프로덕션)
- **ORM**: Spring Data JPA
- **애니메이션**: framer-motion 또는 CSS transitions (React Bits 스타일)

### 핵심 기능
- 구독 서비스 등록/수정/삭제/조회
- 월별 지출액 자동 계산 (결제 주기별 환산)
- 결제일 임박 알림 (3일 이내)
- 다음 결제일 자동 계산
- 배치 작업 (매일 오전 9시)

---

## 프로젝트 아키텍처

### 디렉토리 구조
```
subscription-manager/
├── src/main/java/com/subscription/
│   ├── controller/          # REST API 엔드포인트
│   ├── service/             # 비즈니스 로직
│   ├── repository/          # 데이터 접근 계층
│   ├── entity/              # JPA 엔티티
│   ├── dto/                 # 데이터 전송 객체
│   ├── util/                # 유틸리티 클래스
│   └── config/              # 설정 클래스
├── src/main/resources/
│   ├── application.properties
│   └── data.sql
└── frontend/
    ├── src/
    │   ├── components/      # React 컴포넌트
    │   ├── pages/           # 페이지 컴포넌트
    │   ├── services/        # API 호출 함수
    │   └── hooks/          # 커스텀 훅
    └── package.json
```

### 계층 분리 규칙
- **Controller**: HTTP 요청/응답 처리만 담당, Service 호출
- **Service**: 비즈니스 로직 처리, Repository 및 DateCalculator 호출
- **Repository**: 데이터베이스 접근만 담당
- **Entity**: 데이터베이스 테이블 매핑
- **DTO**: 계층 간 데이터 전송

---

## 코드 표준

### 네이밍 규칙
- **Java 클래스**: PascalCase (예: `SubscriptionService`)
- **Java 메서드/변수**: camelCase (예: `getMonthlyExpense`)
- **상수**: UPPER_SNAKE_CASE (예: `MAX_RETRY_COUNT`)
- **React 컴포넌트**: PascalCase (예: `SubscriptionList`)
- **React 함수/변수**: camelCase (예: `handleSubmit`)
- **파일명**: 클래스명과 동일 (예: `SubscriptionService.java`)

### 포맷팅 규칙
- Java: 4 spaces 들여쓰기
- TypeScript/React: 2 spaces 들여쓰기
- 줄 길이: 최대 120자
- 파일 끝: 빈 줄 1개

### 주석 규칙

#### 복잡한 구조 주석 (필수)
복잡한 로직이나 다중 계층 호출 시 반드시 호출 경로를 주석으로 명시:

```java
/**
 * 호출 경로: SubscriptionController.create() 
 *          → SubscriptionService.createSubscription()
 *          → SubscriptionRepository.save()
 *          → DateCalculator.calculateNextBillingDate()
 * 
 * 수학적 구조: 다음 결제일 = 현재 결제일 + 결제 주기
 * - MONTHLY: +1개월
 * - QUARTERLY: +3개월  
 * - YEARLY: +1년
 */
public SubscriptionResponseDto createSubscription(SubscriptionRequestDto request) {
    // ...
}
```

```typescript
/**
 * 호출 경로: SubscriptionForm.handleSubmit()
 *          → api.createSubscription()
 *          → POST /api/subscriptions
 *          → SubscriptionList.refresh() (상태 업데이트)
 * 
 * 애니메이션: 제출 시 로딩 스피너 표시, 성공 시 슬라이드 인 애니메이션
 */
const handleSubmit = async (data: SubscriptionFormData) => {
    // ...
}
```

#### 수학적 구조 주석
금액 계산, 날짜 계산 등 수학적 로직은 주석으로 설명:

```java
/**
 * 월별 지출액 계산 로직:
 * - MONTHLY: price (그대로)
 * - QUARTERLY: price ÷ 3 (분기 금액을 월 단위로 환산)
 * - YEARLY: price ÷ 12 (연간 금액을 월 단위로 환산)
 * 
 * 반올림: HALF_UP (0.5 이상 올림)
 */
private BigDecimal calculateMonthlyExpense(Subscription sub) {
    // ...
}
```

### 예외 처리 규칙
- **리소스 관리**: try-with-resources 사용 (안전한 경우)
- **안전하지 않은 경우**: try-catch 사용
- **예외 전파**: Service 계층에서 예외 처리, Controller는 HTTP 상태 코드 변환

```java
// ✅ 올바른 예시
try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    // 리소스 사용
} catch (IOException e) {
    log.error("파일 읽기 실패", e);
    throw new ServiceException("파일 처리 중 오류 발생");
}

// ✅ 안전하지 않은 경우
try {
    repository.save(subscription);
} catch (DataAccessException e) {
    log.error("데이터 저장 실패", e);
    throw new ServiceException("구독 서비스 저장 실패", e);
}
```

---

## 기능 구현 표준

### 백엔드 구현 규칙

#### Controller 계층
- **역할**: HTTP 요청/응답 처리, DTO 변환
- **규칙**:
  - Service만 호출, Repository 직접 호출 금지
  - `@Valid`로 요청 검증
  - ResponseEntity로 응답 반환
  - 예외는 `@ExceptionHandler`로 처리

```java
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    
    private final SubscriptionService service;
    
    // ✅ 올바른 예시: Service 호출
    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> create(
        @Valid @RequestBody SubscriptionRequestDto request
    ) {
        SubscriptionResponseDto response = service.createSubscription(request);
        return ResponseEntity.ok(response);
    }
    
    // ❌ 금지: Repository 직접 호출
    // @Autowired
    // private SubscriptionRepository repository; // 금지!
}
```

#### Service 계층
- **역할**: 비즈니스 로직 처리
- **규칙**:
  - Repository와 DateCalculator만 호출
  - DTO ↔ Entity 변환 수행
  - 모든 금액 계산은 BigDecimal 사용
  - 날짜 계산은 DateCalculator 사용

```java
@Service
public class SubscriptionService {
    
    private final SubscriptionRepository repository;
    
    /**
     * 호출 경로: SubscriptionController.create()
     *          → SubscriptionService.createSubscription()
     *          → DateCalculator.calculateNextBillingDate()
     *          → SubscriptionRepository.save()
     */
    public SubscriptionResponseDto createSubscription(SubscriptionRequestDto request) {
        // Entity 변환
        Subscription subscription = convertToEntity(request);
        
        // 다음 결제일 계산 (DateCalculator 사용)
        LocalDate nextBillingDate = DateCalculator.calculateNextBillingDate(
            subscription.getBillingDate(),
            subscription.getBillingCycle()
        );
        subscription.setNextBillingDate(nextBillingDate);
        
        // 저장
        Subscription saved = repository.save(subscription);
        
        // DTO 변환
        return convertToDto(saved);
    }
    
    /**
     * 수학적 구조: 월별 지출액 = Σ(각 구독의 월 환산 금액)
     * - MONTHLY: price
     * - QUARTERLY: price ÷ 3
     * - YEARLY: price ÷ 12
     */
    public BigDecimal getMonthlyExpense(Long userId) {
        List<Subscription> subscriptions = repository.findByUserId(userId);
        return subscriptions.stream()
            .map(this::calculateMonthlyExpense)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

#### Repository 계층
- **역할**: 데이터베이스 접근
- **규칙**:
  - JpaRepository 상속
  - 커스텀 쿼리는 `@Query` 사용
  - 메서드명은 Spring Data JPA 규칙 따름

```java
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByUserId(Long userId);
    
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate BETWEEN :start AND :end")
    List<Subscription> findByNextBillingDateBetween(
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );
}
```

#### Entity 계층
- **역할**: 데이터베이스 테이블 매핑
- **규칙**:
  - `@Entity`, `@Table` 사용
  - 연관관계는 `@OneToMany`, `@ManyToOne` 사용
  - 금액 필드는 BigDecimal 사용
  - 날짜 필드는 LocalDate 사용

```java
@Entity
@Table(name = "subscriptions")
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String serviceName;
    
    // ✅ BigDecimal 사용 (금액 계산 정확도)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;
    
    // ✅ LocalDate 사용 (날짜 계산)
    private LocalDate billingDate;
    private LocalDate nextBillingDate;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
```

#### 유틸리티 클래스
- **DateCalculator**: 날짜 계산 로직만 담당
- **규칙**: static 메서드만 사용, 상태 없음

```java
public class DateCalculator {
    
    /**
     * 호출 경로: SubscriptionService.createSubscription()
     *          → DateCalculator.calculateNextBillingDate()
     * 
     * 수학적 구조: 다음 결제일 = 현재 결제일 + 결제 주기
     */
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

### 프론트엔드 구현 규칙

#### React 컴포넌트 구조
- **컴포넌트**: `components/` 디렉토리
- **페이지**: `pages/` 디렉토리
- **API 호출**: `services/api.ts`에서만 수행
- **애니메이션**: framer-motion 또는 CSS transitions 사용

#### 애니메이션 구현 규칙 (React Bits 스타일)
- **모든 인터랙티브 요소**: hover, click 애니메이션 포함
- **리스트 아이템**: 등장 애니메이션 (stagger 효과)
- **폼 제출**: 로딩 스피너, 성공/실패 피드백 애니메이션
- **모달/드로어**: 슬라이드/페이드 애니메이션

```typescript
// ✅ framer-motion 사용 예시
import { motion } from 'framer-motion';

/**
 * 호출 경로: App.tsx
 *          → SubscriptionList (렌더링)
 *          → SubscriptionItem (각 아이템)
 *          → api.getSubscriptions() (데이터 로드)
 * 
 * 애니메이션: 리스트 아이템 등장 시 stagger 효과
 */
export const SubscriptionList: React.FC = () => {
    const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
    
    useEffect(() => {
        // API 호출은 services/api.ts에서만
        api.getSubscriptions(userId).then(setSubscriptions);
    }, [userId]);
    
    return (
        <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.3 }}
        >
            {subscriptions.map((sub, index) => (
                <motion.div
                    key={sub.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.1 }}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                >
                    {/* 구독 아이템 내용 */}
                </motion.div>
            ))}
        </motion.div>
    );
};
```

#### API 호출 규칙
- **위치**: `services/api.ts`에서만 API 호출 함수 정의
- **에러 처리**: try-catch 사용, 사용자 피드백 제공

```typescript
// services/api.ts
/**
 * 호출 경로: SubscriptionForm.handleSubmit()
 *          → api.createSubscription()
 *          → POST /api/subscriptions
 */
export const createSubscription = async (
    data: SubscriptionRequestDto
): Promise<SubscriptionResponseDto> => {
    try {
        const response = await axios.post('/api/subscriptions', data);
        return response.data;
    } catch (error) {
        console.error('구독 서비스 생성 실패', error);
        throw error;
    }
};
```

#### 상태 관리 규칙
- **로컬 상태**: useState 사용
- **전역 상태**: Context API 사용 (필요한 경우)
- **서버 상태**: useEffect + useState 또는 React Query 사용

---

## 프레임워크 사용 표준

### Spring Boot 규칙
- **의존성 주입**: 생성자 주입 사용 (`@Autowired` 필드 주입 금지)
- **트랜잭션**: `@Transactional`은 Service 계층에만 사용
- **예외 처리**: `@ControllerAdvice`로 전역 예외 처리

### JPA 규칙
- **연관관계**: 1:N 관계는 `@OneToMany`(mappedBy) + `@ManyToOne` 사용
- **지연 로딩**: 기본값 사용, 필요시 `@EntityGraph` 사용
- **쿼리**: 복잡한 쿼리는 `@Query` 사용

### React 규칙
- **함수 컴포넌트**: 함수형 컴포넌트만 사용
- **훅**: 커스텀 훅은 `hooks/` 디렉토리
- **타입**: TypeScript 타입 정의 필수

---

## 워크플로우 표준

### 데이터 흐름
```
사용자 입력
  ↓
React Component (프론트엔드)
  ↓
services/api.ts (API 호출)
  ↓
Controller (백엔드)
  ↓
Service (비즈니스 로직)
  ↓
Repository (데이터 접근)
  ↓
Database
```

### 요청 처리 흐름
1. **프론트엔드**: 사용자 입력 → API 호출
2. **Controller**: 요청 검증 → Service 호출
3. **Service**: 비즈니스 로직 → Repository 호출 → DateCalculator 호출
4. **Repository**: 데이터베이스 접근
5. **응답**: Repository → Service → Controller → 프론트엔드

---

## 주요 파일 상호작용 표준

### 동시 수정 필요 파일

#### 구독 서비스 생성 기능 추가 시
- `SubscriptionController.java` (엔드포인트 추가)
- `SubscriptionService.java` (비즈니스 로직 추가)
- `SubscriptionRepository.java` (필요시 쿼리 추가)
- `SubscriptionRequestDto.java` (요청 DTO)
- `SubscriptionResponseDto.java` (응답 DTO)
- `frontend/src/services/api.ts` (API 호출 함수)
- `frontend/src/components/SubscriptionForm.tsx` (폼 컴포넌트)

#### 날짜 계산 로직 변경 시
- `DateCalculator.java` (계산 로직)
- `SubscriptionService.java` (호출 부분)
- 관련 테스트 파일 (있는 경우)

#### 애니메이션 추가 시
- 해당 컴포넌트 파일 (예: `SubscriptionList.tsx`)
- `package.json` (framer-motion 의존성 추가 필요 시)

---

## AI 의사결정 표준

### 우선순위 판단 기준

#### 1. 기능 구현 우선순위
1. **핵심 기능**: 구독 CRUD, 월별 지출 계산
2. **부가 기능**: 결제 임박 알림, 배치 작업
3. **UI 개선**: 애니메이션, 인터랙티브 요소

#### 2. 버그 수정 우선순위
1. **치명적 오류**: 데이터 손실, 보안 취약점
2. **기능 오류**: 계산 오류, 날짜 오류
3. **UI 오류**: 레이아웃 깨짐, 애니메이션 오류

#### 3. 코드 수정 시 고려사항
- **계층 분리**: Controller는 Service만 호출
- **의존성 방향**: 상위 계층 → 하위 계층
- **재사용성**: 공통 로직은 유틸리티 클래스로 분리

---

## 금지 사항

### 절대 하지 말아야 할 것

#### 백엔드
- ❌ **Controller에서 Repository 직접 호출 금지**
- ❌ **금액 계산에 double/float 사용 금지** (BigDecimal 사용)
- ❌ **날짜 계산을 Service에 직접 구현 금지** (DateCalculator 사용)
- ❌ **필드 주입 사용 금지** (생성자 주입 사용)
- ❌ **Entity를 DTO로 직접 반환 금지** (DTO 변환 필수)

#### 프론트엔드
- ❌ **컴포넌트에서 직접 axios 호출 금지** (services/api.ts 사용)
- ❌ **애니메이션 없는 인터랙티브 요소 금지** (React Bits 스타일 필수)
- ❌ **복잡한 로직에 주석 없이 구현 금지** (호출 경로 주석 필수)
- ❌ **any 타입 사용 금지** (TypeScript 타입 정의 필수)

#### 공통
- ❌ **일반적인 개발 지식을 주석으로 설명 금지** (프로젝트 특정 규칙만)
- ❌ **설명적 주석 금지** (명령형 주석만, "호출 경로:", "수학적 구조:" 형식)

---

## 예시: 올바른 구현 vs 잘못된 구현

### 예시 1: 구독 서비스 생성

#### ✅ 올바른 구현
```java
// Controller
@PostMapping
public ResponseEntity<SubscriptionResponseDto> create(
    @Valid @RequestBody SubscriptionRequestDto request
) {
    // Service 호출
    SubscriptionResponseDto response = service.createSubscription(request);
    return ResponseEntity.ok(response);
}

// Service
/**
 * 호출 경로: SubscriptionController.create()
 *          → SubscriptionService.createSubscription()
 *          → DateCalculator.calculateNextBillingDate()
 *          → SubscriptionRepository.save()
 */
public SubscriptionResponseDto createSubscription(SubscriptionRequestDto request) {
    Subscription subscription = convertToEntity(request);
    LocalDate nextBillingDate = DateCalculator.calculateNextBillingDate(
        subscription.getBillingDate(),
        subscription.getBillingCycle()
    );
    subscription.setNextBillingDate(nextBillingDate);
    Subscription saved = repository.save(subscription);
    return convertToDto(saved);
}
```

#### ❌ 잘못된 구현
```java
// Controller에서 Repository 직접 호출 (금지!)
@Autowired
private SubscriptionRepository repository; // ❌

@PostMapping
public ResponseEntity<Subscription> create(@RequestBody SubscriptionRequestDto request) {
    Subscription sub = new Subscription();
    // ... 직접 변환
    sub.setNextBillingDate(sub.getBillingDate().plusMonths(1)); // ❌ DateCalculator 미사용
    Subscription saved = repository.save(sub); // ❌ Controller에서 Repository 호출
    return ResponseEntity.ok(saved); // ❌ Entity 직접 반환
}
```

### 예시 2: React 컴포넌트

#### ✅ 올바른 구현
```typescript
/**
 * 호출 경로: App.tsx
 *          → SubscriptionList (렌더링)
 *          → api.getSubscriptions() (데이터 로드)
 *          → SubscriptionItem (각 아이템 렌더링)
 * 
 * 애니메이션: 리스트 아이템 등장 시 stagger 효과, hover 시 scale 효과
 */
export const SubscriptionList: React.FC = () => {
    const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
    
    useEffect(() => {
        // ✅ services/api.ts에서 API 호출
        api.getSubscriptions(userId).then(setSubscriptions);
    }, [userId]);
    
    return (
        <motion.div>
            {subscriptions.map((sub, index) => (
                <motion.div
                    key={sub.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.1 }}
                    whileHover={{ scale: 1.02 }}
                >
                    {/* 내용 */}
                </motion.div>
            ))}
        </motion.div>
    );
};
```

#### ❌ 잘못된 구현
```typescript
// ❌ 컴포넌트에서 직접 axios 호출
export const SubscriptionList: React.FC = () => {
    const [subscriptions, setSubscriptions] = useState<any[]>([]); // ❌ any 타입
    
    useEffect(() => {
        // ❌ services/api.ts 미사용
        axios.get('/api/subscriptions').then(res => setSubscriptions(res.data));
    }, []);
    
    // ❌ 애니메이션 없음
    return (
        <div>
            {subscriptions.map(sub => (
                <div key={sub.id}>{sub.serviceName}</div>
            ))}
        </div>
    );
};
```

---

## 참고 사항

### 프로젝트 특정 규칙만 포함
- 이 문서는 프로젝트 특정 규칙만 포함
- 일반적인 Spring Boot, React 지식은 포함하지 않음
- AI Agent가 작업 시 이 규칙을 우선적으로 따름

### 규칙 업데이트
- 새로운 기능 추가 시 관련 규칙 추가
- 기존 코드 변경 시 규칙 검토 및 업데이트
- 사용하지 않는 규칙은 제거

