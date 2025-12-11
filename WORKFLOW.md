# 📋 구독 서비스 매니저 - 작업 순서 가이드

## 🎯 개요

이 문서는 구독 서비스 매니저 프로젝트의 효율적인 개발 순서를 제시합니다.
총 **16개의 작업**이 있으며, **백엔드 우선 개발** 후 **프론트엔드 개발** 순서로 진행합니다.

---

## 📊 작업 전체 현황

| 단계 | 작업 수 | 상태 |
|------|---------|------|
| Phase 1: 백엔드 기반 구축 | 8개 | ⏳ 대기 |
| Phase 2: 프론트엔드 개발 | 7개 | ⏳ 대기 |
| Phase 3: 통합 및 문서화 | 1개 | ⏳ 대기 |
| **총계** | **16개** | - |

---

## 🚀 Phase 1: 백엔드 기반 구축

> **중요**: 프론트엔드가 백엔드 API에 의존하므로 백엔드부터 완전히 개발합니다.

### 작업 1: Spring Boot 프로젝트 초기 설정
**ID**: `286bd5cb-a287-4f7e-a135-0940c72c1717`  
**의존성**: 없음 (최우선 작업)

**작업 내용**:
- Maven 프로젝트 구조 생성
- `pom.xml` 의존성 추가 (Spring Web, JPA, H2, Lombok, Scheduler)
- `application.properties` 설정 (H2 DB, JPA, 로깅)
- 기본 패키지 구조 생성 (controller, service, repository, entity, dto, util, config)

**검증 기준**:
- ✅ Maven 프로젝트가 정상적으로 빌드됨
- ✅ Spring Boot 애플리케이션이 실행됨
- ✅ H2 데이터베이스가 정상적으로 연결됨
- ✅ 모든 패키지 구조가 생성됨

---

### 작업 2: JPA 엔티티 및 Enum 설계
**ID**: `d1d5640c-4183-45c6-a5f1-58a94e623b4c`  
**의존성**: 작업 1 (Spring Boot 프로젝트 초기 설정)

**작업 내용**:
- `BillingCycle` Enum 생성 (MONTHLY, QUARTERLY, YEARLY)
- `User` 엔티티 생성 (1:N 관계의 1)
- `Subscription` 엔티티 생성 (1:N 관계의 N)
- 1:N 연관관계 매핑 (@OneToMany, @ManyToOne)

**중요 사항**:
- 모든 금액 필드는 `BigDecimal` 사용
- 날짜 필드는 `LocalDate` 사용
- 연관관계의 주인은 `Subscription` 엔티티

**검증 기준**:
- ✅ 엔티티 클래스가 정상적으로 컴파일됨
- ✅ H2 데이터베이스에 테이블이 자동 생성됨 (users, subscriptions)
- ✅ 연관관계가 정상적으로 매핑됨
- ✅ BigDecimal, LocalDate 타입이 올바르게 사용됨

---

### 작업 3: Repository 계층 구현
**ID**: `bf4c55e7-b3ef-41a0-8780-747654e3a6e3`  
**의존성**: 작업 2 (JPA 엔티티 및 Enum 설계)

**작업 내용**:
- `UserRepository` 인터페이스 생성
- `SubscriptionRepository` 인터페이스 생성
- 커스텀 쿼리 메서드 구현 (`findByNextBillingDateBetween`)

**중요 사항**:
- Repository는 데이터베이스 접근만 담당
- 비즈니스 로직은 Service 계층에 구현

**검증 기준**:
- ✅ Repository 인터페이스가 정상적으로 컴파일됨
- ✅ Spring Data JPA가 메서드를 정상적으로 인식함
- ✅ 커스텀 쿼리가 올바르게 작성됨

---

### 작업 4: 날짜 계산 유틸리티 클래스 구현
**ID**: `6a0c3fb6-680b-4369-8e2e-d6fd58369a90`  
**의존성**: 작업 2 (JPA 엔티티 및 Enum 설계)  
**병렬 가능**: 작업 5 (DTO 클래스 생성)와 동시 진행 가능

**작업 내용**:
- `DateCalculator` 유틸리티 클래스 생성
- `calculateNextBillingDate` 메서드 구현
- 호출 경로 주석 및 수학적 구조 주석 추가

**중요 사항**:
- 날짜 계산 로직은 이 유틸리티 클래스에만 구현
- Service 계층에서 직접 날짜 계산 금지
- static 메서드만 사용, 상태 없음

**검증 기준**:
- ✅ DateCalculator 클래스가 정상적으로 컴파일됨
- ✅ 각 결제 주기별로 올바른 날짜가 계산됨
- ✅ 호출 경로 주석 및 수학적 구조 주석이 포함됨

---

### 작업 5: DTO 클래스 생성
**ID**: `ab5ceb7a-3291-4057-9bea-18a312e54f64`  
**의존성**: 작업 2 (JPA 엔티티 및 Enum 설계)  
**병렬 가능**: 작업 4 (날짜 계산 유틸리티)와 동시 진행 가능

**작업 내용**:
- `SubscriptionRequestDto` 생성 (등록/수정용)
- `SubscriptionResponseDto` 생성 (조회용)
- `MonthlyExpenseDto` 생성 (월별 지출용)
- `@Valid` 검증 어노테이션 추가

**중요 사항**:
- 모든 금액 필드는 `BigDecimal` 사용
- DTO는 계층 간 데이터 전송만 담당

**검증 기준**:
- ✅ 모든 DTO 클래스가 정상적으로 컴파일됨
- ✅ 검증 어노테이션이 올바르게 적용됨
- ✅ BigDecimal 타입이 올바르게 사용됨

---

### 작업 6: Service 계층 비즈니스 로직 구현
**ID**: `bda9da23-ab9c-4fbb-8a73-89231885a1dd`  
**의존성**: 작업 3 (Repository 계층), 작업 4 (날짜 계산 유틸리티), 작업 5 (DTO 클래스)

**작업 내용**:
- `SubscriptionService` 클래스 생성
- `createSubscription` 메서드 (다음 결제일 자동 계산)
- `getMonthlyExpense` 메서드 (월별 지출액 계산)
- `getUpcomingSubscriptions` 메서드 (3일 이내 결제 예정 조회)
- DTO ↔ Entity 변환 메서드

**중요 사항**:
- Service는 Repository와 DateCalculator만 호출
- Controller는 Service만 호출
- 모든 금액 계산은 `BigDecimal` 사용
- 호출 경로 주석 및 수학적 구조 주석 필수

**검증 기준**:
- ✅ Service 클래스가 정상적으로 컴파일됨
- ✅ 모든 메서드가 올바르게 구현됨
- ✅ 호출 경로 주석 및 수학적 구조 주석이 포함됨
- ✅ BigDecimal을 사용한 금액 계산이 정확함
- ✅ 날짜 계산이 DateCalculator를 통해 수행됨

---

### 작업 7: REST API Controller 구현
**ID**: `369b866f-c295-4ae1-9db2-57c8eda81d2b`  
**의존성**: 작업 6 (Service 계층 비즈니스 로직 구현)

**작업 내용**:
- `SubscriptionController` 클래스 생성
- REST API 엔드포인트 구현:
  - `POST /api/subscriptions` (등록)
  - `GET /api/subscriptions?userId={id}` (목록)
  - `GET /api/subscriptions/{id}` (상세)
  - `PUT /api/subscriptions/{id}` (수정)
  - `DELETE /api/subscriptions/{id}` (삭제)
  - `GET /api/subscriptions/upcoming?userId={id}` (임박 목록)
  - `GET /api/subscriptions/monthly-expense?userId={id}` (월별 지출)
- `@Valid` 검증, `ResponseEntity` 반환, 예외 처리

**중요 사항**:
- Controller는 Service만 호출, Repository 직접 호출 금지
- `@Valid`로 요청 검증 필수

**검증 기준**:
- ✅ Controller 클래스가 정상적으로 컴파일됨
- ✅ 모든 엔드포인트가 올바르게 구현됨
- ✅ `@Valid` 검증이 작동함
- ✅ `ResponseEntity`가 올바르게 반환됨
- ✅ API 테스트 가능 (Postman 등)

---

### 작업 8: 배치 작업 구현
**ID**: `54729e70-aafd-4efc-b393-ef8042ed58a8`  
**의존성**: 작업 3 (Repository 계층 구현)  
**병렬 가능**: 작업 7 (REST API Controller)과 병렬 가능

**작업 내용**:
- `SchedulerConfig` 클래스 생성 (`@EnableScheduling`)
- `BillingScheduler` 서비스 생성
- `@Scheduled(cron = "0 0 9 * * *")` 배치 작업 구현
- 결제 임박 서비스 콘솔 로그 출력

**중요 사항**:
- 배치 작업은 매일 오전 9시에 실행
- 테스트 시 cron 표현식을 임시로 변경하여 즉시 실행 가능
- 호출 경로 주석 필수

**검증 기준**:
- ✅ SchedulerConfig가 정상적으로 컴파일됨
- ✅ BillingScheduler가 정상적으로 컴파일됨
- ✅ 배치 작업이 정상적으로 실행됨 (테스트용 cron 변경 후)
- ✅ 콘솔에 로그가 올바르게 출력됨

---

## 🎨 Phase 2: 프론트엔드 개발

> **중요**: 백엔드 API가 완성된 후 프론트엔드 개발을 시작합니다.

### 작업 9: React 프로젝트 초기화 및 설정
**ID**: `6eb918ac-e432-4467-b4b2-39344f3ad558`  
**의존성**: 작업 7 (REST API Controller 구현)

**작업 내용**:
- Vite + TypeScript 프로젝트 생성
- 필수 의존성 추가 (axios, framer-motion, recharts)
- 기본 디렉토리 구조 생성 (components, pages, services, hooks)
- TypeScript 타입 정의 (Subscription, BillingCycle, User, Request/Response DTO)

**중요 사항**:
- React Bits 스타일 애니메이션을 위해 framer-motion 필수

**검증 기준**:
- ✅ React 프로젝트가 정상적으로 생성됨
- ✅ 모든 의존성이 정상적으로 설치됨
- ✅ 프로젝트가 정상적으로 실행됨 (`npm run dev`)
- ✅ TypeScript 타입 정의가 올바르게 작성됨

---

### 작업 10: API 서비스 레이어 구현
**ID**: `243f97ab-f886-4cea-86e4-a76ba18257d4`  
**의존성**: 작업 9 (React 프로젝트 초기화 및 설정)

**작업 내용**:
- `services/api.ts` 파일 생성
- 모든 API 호출 함수 정의:
  - `createSubscription`
  - `getSubscriptions`
  - `getSubscriptionById`
  - `updateSubscription`
  - `deleteSubscription`
  - `getUpcomingSubscriptions`
  - `getMonthlyExpense`
- axios 사용, 에러 처리 (try-catch), 호출 경로 주석 필수

**중요 사항**:
- 모든 API 호출은 이 파일에서만 수행
- 컴포넌트에서 직접 axios 호출 금지

**검증 기준**:
- ✅ api.ts 파일이 정상적으로 컴파일됨
- ✅ 모든 API 호출 함수가 올바르게 구현됨
- ✅ 에러 처리가 올바르게 구현됨
- ✅ 호출 경로 주석이 포함됨

---

### 작업 11: 구독 목록 컴포넌트 구현 (React Bits 스타일)
**ID**: `a561358d-c542-428e-944b-e8d3853a3c8d`  
**의존성**: 작업 10 (API 서비스 레이어 구현)

**작업 내용**:
- `SubscriptionList` 컴포넌트 생성
- framer-motion 애니메이션:
  - 리스트 아이템 등장 시 stagger 효과 (`delay: index * 0.1`)
  - hover 시 scale 효과 (`whileHover: scale 1.02`)
  - click 시 scale 효과 (`whileTap: scale 0.98`)
- API 호출은 `services/api.ts` 사용
- 호출 경로 주석 필수

**중요 사항**:
- React Bits 스타일 애니메이션 필수
- 모든 인터랙티브 요소에 애니메이션 적용

**검증 기준**:
- ✅ 컴포넌트가 정상적으로 렌더링됨
- ✅ 리스트 아이템이 stagger 효과로 등장함
- ✅ hover 시 scale 효과가 작동함
- ✅ API 호출이 정상적으로 작동함
- ✅ 호출 경로 주석이 포함됨

---

### 작업 12: 구독 등록/수정 폼 컴포넌트 구현 (React Bits 스타일)
**ID**: `c3874fd8-3c3c-49b3-bcff-e4f15f8dbf89`  
**의존성**: 작업 10 (API 서비스 레이어 구현)

**작업 내용**:
- `SubscriptionForm` 컴포넌트 생성
- 제출 시 로딩 스피너 표시
- 성공/실패 피드백 애니메이션 (슬라이드 인, 페이드)
- framer-motion 애니메이션 적용
- API 호출은 `services/api.ts` 사용
- 호출 경로 주석 필수

**중요 사항**:
- 제출 시 로딩 스피너, 성공/실패 피드백 애니메이션 필수
- React Bits 스타일 준수

**검증 기준**:
- ✅ 폼이 정상적으로 렌더링됨
- ✅ 제출 시 로딩 스피너가 표시됨
- ✅ 성공/실패 피드백 애니메이션이 작동함
- ✅ API 호출이 정상적으로 작동함
- ✅ 호출 경로 주석이 포함됨

---

### 작업 13: 결제 임박 알림 컴포넌트 구현 (React Bits 스타일)
**ID**: `2c815aab-ab22-4421-a901-894799ae7718`  
**의존성**: 작업 10 (API 서비스 레이어 구현)

**작업 내용**:
- `UpcomingBilling` 컴포넌트 생성
- framer-motion 슬라이드 인 애니메이션
- 카드 형태의 UI
- 결제일까지 남은 일수 표시
- API 호출은 `services/api.ts` 사용
- 호출 경로 주석 필수

**중요 사항**:
- 슬라이드 인 애니메이션 필수
- 결제일까지 남은 일수를 명확히 표시

**검증 기준**:
- ✅ 컴포넌트가 정상적으로 렌더링됨
- ✅ 슬라이드 인 애니메이션이 작동함
- ✅ 결제일까지 남은 일수가 올바르게 계산됨
- ✅ API 호출이 정상적으로 작동함
- ✅ 호출 경로 주석이 포함됨

---

### 작업 14: 월별 지출 차트 컴포넌트 구현
**ID**: `7ef5ab8a-c59b-4503-9792-24da3835a61a`  
**의존성**: 작업 10 (API 서비스 레이어 구현)  
**병렬 가능**: 작업 11, 12, 13과 동시 진행 가능

**작업 내용**:
- `MonthlyExpenseChart` 컴포넌트 생성
- Recharts 라이브러리 사용
- framer-motion 등장 애니메이션
- API 호출은 `services/api.ts` 사용
- 호출 경로 주석 필수

**중요 사항**:
- Recharts를 사용한 차트 시각화
- 애니메이션 효과 필수

**검증 기준**:
- ✅ 차트가 정상적으로 렌더링됨
- ✅ 등장 애니메이션이 작동함
- ✅ API 호출이 정상적으로 작동함
- ✅ 차트 데이터가 올바르게 표시됨
- ✅ 호출 경로 주석이 포함됨

---

### 작업 15: 메인 페이지 및 컴포넌트 통합
**ID**: `9503b61b-293d-402a-a914-eec2f00209c5`  
**의존성**: 작업 11, 12, 13, 14 (모든 프론트엔드 컴포넌트)

**작업 내용**:
- `App.tsx` 수정 또는 `pages/Home.tsx` 생성
- 모든 컴포넌트 통합:
  - `SubscriptionList`
  - `SubscriptionForm`
  - `UpcomingBilling`
  - `MonthlyExpenseChart`
- 레이아웃 구성
- 전역 스타일 설정
- 호출 경로 주석 필수

**중요 사항**:
- 모든 컴포넌트를 통합하여 완성된 UI 구성
- 사용자 경험을 고려한 레이아웃 설계

**검증 기준**:
- ✅ 모든 컴포넌트가 정상적으로 통합됨
- ✅ 레이아웃이 올바르게 구성됨
- ✅ 컴포넌트 간 상호작용이 정상적으로 작동함
- ✅ 전체 UI가 일관성 있게 표시됨

---

## ✅ Phase 3: 통합 및 문서화

### 작업 16: 통합 테스트 및 문서화
**ID**: `0328070d-3269-412c-a86a-e97f56ab01d2`  
**의존성**: 작업 15 (메인 페이지 및 컴포넌트 통합)

**작업 내용**:
- 통합 테스트 수행:
  - 백엔드 API 테스트 (Postman 또는 curl)
  - 프론트엔드-백엔드 연동 테스트
  - 날짜 계산 로직 검증
  - 월별 지출 계산 검증
  - 배치 작업 테스트
- `README.md` 작성 (프로젝트 소개, 실행 방법)
- API 문서 작성 (선택사항)
- 버그 수정 및 최종 점검

**중요 사항**:
- 모든 기능이 정상적으로 작동하는지 확인
- 문서는 향후 유지보수를 위해 중요함

**검증 기준**:
- ✅ 모든 통합 테스트가 통과됨
- ✅ README.md가 완성됨
- ✅ 프로젝트가 정상적으로 실행됨
- ✅ 모든 기능이 정상적으로 작동함

---

## 📈 작업 의존성 그래프

```
작업 1 (Spring Boot 초기 설정)
  ↓
작업 2 (JPA 엔티티)
  ↓
작업 3 (Repository) ──┐
작업 4 (DateCalculator) │
작업 5 (DTO)            │
  └─────────────────────┼─→ 작업 6 (Service)
                        │
작업 3 (Repository) ────┘
  ↓
작업 6 (Service) ──→ 작업 7 (Controller)
작업 3 (Repository) ──→ 작업 8 (배치 작업)

작업 7 (Controller) ──→ 작업 9 (React 초기화)
  ↓
작업 10 (API 서비스)
  ↓
작업 11 (구독 목록) ──┐
작업 12 (구독 폼)    │
작업 13 (임박 알림)  │
작업 14 (지출 차트)  │
  └──────────────────┼─→ 작업 15 (메인 페이지)
                     │
                     └─→ 작업 16 (통합 테스트)
```

---

## 🚦 병렬 작업 가능한 작업

다음 작업들은 서로 독립적이므로 **동시에 진행**할 수 있습니다:

1. **작업 4 (날짜 계산 유틸리티)** ↔ **작업 5 (DTO 클래스)**
   - 둘 다 작업 2에만 의존

2. **작업 7 (REST API Controller)** ↔ **작업 8 (배치 작업)**
   - 작업 7은 작업 6에 의존
   - 작업 8은 작업 3에 의존

3. **작업 11, 12, 13, 14 (프론트엔드 컴포넌트들)**
   - 모두 작업 10에만 의존하므로 동시 진행 가능

---

## 📝 작업 진행 체크리스트

### Phase 1: 백엔드 기반 구축
- [ ] 작업 1: Spring Boot 프로젝트 초기 설정
- [ ] 작업 2: JPA 엔티티 및 Enum 설계
- [ ] 작업 3: Repository 계층 구현
- [ ] 작업 4: 날짜 계산 유틸리티 클래스 구현
- [ ] 작업 5: DTO 클래스 생성
- [ ] 작업 6: Service 계층 비즈니스 로직 구현
- [ ] 작업 7: REST API Controller 구현
- [ ] 작업 8: 배치 작업 구현

### Phase 2: 프론트엔드 개발
- [ ] 작업 9: React 프로젝트 초기화 및 설정
- [ ] 작업 10: API 서비스 레이어 구현
- [ ] 작업 11: 구독 목록 컴포넌트 구현
- [ ] 작업 12: 구독 등록/수정 폼 컴포넌트 구현
- [ ] 작업 13: 결제 임박 알림 컴포넌트 구현
- [ ] 작업 14: 월별 지출 차트 컴포넌트 구현
- [ ] 작업 15: 메인 페이지 및 컴포넌트 통합

### Phase 3: 통합 및 문서화
- [ ] 작업 16: 통합 테스트 및 문서화

---

## 🎯 핵심 원칙

1. **백엔드 우선 개발**: 프론트엔드가 백엔드 API에 의존하므로 백엔드를 먼저 완성
2. **계층별 순차 개발**: Entity → Repository → Service → Controller
3. **핵심 기능 우선**: CRUD → 계산 로직 → 배치 작업
4. **React Bits 스타일**: 모든 컴포넌트에 framer-motion 애니메이션 적용
5. **주석 규칙 준수**: 복잡한 로직에 호출 경로 주석 필수
6. **shrimp-rules.md 준수**: 모든 작업은 프로젝트 표준 문서를 따라야 함

---

## 📚 참고 문서

- [PROJECT_PLAN.md](./PROJECT_PLAN.md) - 프로젝트 계획서
- [shrimp-rules.md](./shrimp-rules.md) - 개발 가이드라인

---

**작성일**: 2024년 12월  
**버전**: 1.0.0

