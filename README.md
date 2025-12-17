# 💸 나의 구독 서비스 매니저 (Subscription Manager)

넷플릭스, 유튜브, 헬스장 등 정기 결제 서비스를 효율적으로 관리하는 풀스택 웹 애플리케이션입니다.

## 📋 프로젝트 개요

**프로젝트명:** 나의 구독 서비스 매니저 (Subscription Manager)  
**목적:** 정기 결제 서비스를 효율적으로 관리하고 월별 지출을 추적  
**개발 기간:** 7일  
**버전:** 1.0.0

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

## 🛠 기술 스택

### 백엔드
- **Spring Boot 3.2.0** - 웹 애플리케이션 프레임워크
- **Java 17** - 프로그래밍 언어
- **Spring Data JPA** - 데이터베이스 접근 계층
- **H2 Database** - 개발용 인메모리 데이터베이스
- **Lombok** - 보일러플레이트 코드 감소
- **Spring Web** - RESTful API 개발
- **Spring Boot Scheduler** - 배치 작업
- **Maven** - 빌드 도구

### 프론트엔드
- **React 19** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Vite** - 빠른 개발 환경 및 빌드 도구
- **Axios** - HTTP 클라이언트
- **Recharts** - 차트 시각화 (월별 지출)
- **Framer Motion** - 애니메이션 라이브러리

## 📁 프로젝트 구조

```
subscription-manager/
├── src/main/java/com/subscription/
│   ├── SubscriptionManagerApplication.java    # 메인 애플리케이션
│   ├── config/
│   │   └── SchedulerConfig.java               # 스케줄러 설정
│   ├── controller/
│   │   ├── SubscriptionController.java       # REST API 컨트롤러
│   │   └── GlobalExceptionHandler.java        # 전역 예외 처리
│   ├── service/
│   │   ├── SubscriptionService.java          # 비즈니스 로직
│   │   └── BillingScheduler.java             # 배치 작업
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── SubscriptionRepository.java
│   ├── entity/
│   │   ├── User.java                         # 사용자 엔티티
│   │   ├── Subscription.java                 # 구독 엔티티
│   │   └── BillingCycle.java                 # 결제 주기 Enum
│   ├── dto/
│   │   ├── SubscriptionRequestDto.java
│   │   ├── SubscriptionResponseDto.java
│   │   └── MonthlyExpenseDto.java
│   └── util/
│       └── DateCalculator.java               # 날짜 계산 유틸리티
├── src/main/resources/
│   └── application.properties                 # 설정 파일
├── frontend/                                  # React 프로젝트
│   ├── src/
│   │   ├── components/
│   │   │   ├── SubscriptionList.tsx
│   │   │   ├── SubscriptionForm.tsx
│   │   │   ├── UpcomingBilling.tsx
│   │   │   └── MonthlyExpenseChart.tsx
│   │   ├── services/
│   │   │   └── api.ts                         # API 호출 함수
│   │   ├── types/
│   │   │   └── index.ts                       # TypeScript 타입 정의
│   │   ├── App.tsx                            # 메인 컴포넌트
│   │   └── main.tsx                           # 진입점
│   └── package.json
├── pom.xml                                    # Maven 의존성
├── README.md                                  # 프로젝트 문서
├── WORKFLOW.md                                # 작업 순서 가이드
└── PROJECT_PLAN.md                            # 프로젝트 계획서
```

## 🚀 실행 방법

### 사전 요구사항
- Java 17 이상
- Node.js 18 이상
- Maven 3.6 이상 (또는 프로젝트에 포함된 Maven Wrapper 사용)

### 백엔드 실행

1. 프로젝트 루트 디렉토리로 이동:
```bash
cd subscription-manager
```

2. Maven을 사용하여 애플리케이션 실행:
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

또는 Maven이 설치되어 있다면:
```bash
mvn spring-boot:run
```

3. 백엔드 서버가 실행되면:
- API 서버: `http://localhost:8090`
- H2 콘솔: `http://localhost:8090/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (비워두기)

### 프론트엔드 실행

1. 프론트엔드 디렉토리로 이동:
```bash
cd frontend
```

2. 의존성 설치:
```bash
npm install
```

3. 개발 서버 실행:
```bash
npm run dev
```

4. 프론트엔드가 실행되면:
- 개발 서버: `http://localhost:5173` (Vite 기본 포트)

### 프로덕션 빌드

#### 백엔드 빌드
```bash
mvn clean package
java -jar target/subscription-manager-1.0.0.jar
```

#### 프론트엔드 빌드
```bash
cd frontend
npm run build
```

빌드된 파일은 `frontend/dist` 디렉토리에 생성됩니다.

## 📡 API 엔드포인트

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

자세한 API 문서는 [API_DOCS.md](./API_DOCS.md)를 참고하세요.

## 🧪 테스트

### 백엔드 API 테스트

#### 1. 구독 서비스 등록
```bash
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "넷플릭스",
    "price": 9500,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15"
  }'
```

#### 2. 구독 목록 조회
```bash
curl http://localhost:8090/api/subscriptions?userId=1
```

#### 3. 결제 임박 목록 조회
```bash
curl http://localhost:8090/api/subscriptions/upcoming?userId=1
```

#### 4. 월별 지출액 조회
```bash
curl http://localhost:8090/api/subscriptions/monthly-expense?userId=1
```

### 배치 작업 테스트

배치 작업을 즉시 테스트하려면 `BillingScheduler.java`의 cron 표현식을 임시로 변경:

```java
// 원래: @Scheduled(cron = "0 0 9 * * *")  // 매일 오전 9시
// 테스트용: @Scheduled(fixedRate = 60000)  // 1분마다 실행
```

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

## 📚 주요 학습 포인트

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

6. **React + TypeScript**
   - 함수형 컴포넌트
   - 상태 관리
   - API 통신

7. **Framer Motion 애니메이션**
   - React Bits 스타일 애니메이션
   - 인터랙티브 UI 구현

## 🔮 향후 확장 가능한 기능

- 사용자 인증/인가 (Spring Security)
- 이메일 알림 발송
- 통계 대시보드 (월별/연도별)
- 구독 서비스 카테고리 분류
- 결제 내역 기록
- 구독 취소 알림
- 다국어 지원
- 다크 모드

## 📝 개발 가이드라인

프로젝트 개발 시 다음 문서를 참고하세요:

- [WORKFLOW.md](./WORKFLOW.md) - 작업 순서 가이드
- [PROJECT_PLAN.md](./PROJECT_PLAN.md) - 프로젝트 계획서
- [.cursorrules.mdc](./.cursorrules.mdc) - 프론트엔드 개발 규칙

## 🐛 문제 해결

### 백엔드 포트 충돌
`application.properties`에서 포트를 변경:
```properties
server.port=8090
```

### 프론트엔드 API 연결 오류
`frontend/src/services/api.ts`에서 API 베이스 URL 확인:
```typescript
const API_BASE_URL = 'http://localhost:8090';
```

### H2 데이터베이스 접속
H2 콘솔 접속 정보:
- URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비워두기)

## 📄 라이선스

이 프로젝트는 학습 및 포트폴리오 목적으로 제작되었습니다.

## 👥 기여자

- 개발자

---

**작성일:** 2024년 12월  
**버전:** 1.0.0

