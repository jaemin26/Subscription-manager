# 🧪 통합 테스트 가이드

구독 서비스 매니저 프로젝트의 통합 테스트 가이드입니다.

## 목차

1. [사전 준비](#사전-준비)
2. [백엔드 API 테스트](#백엔드-api-테스트)
3. [프론트엔드-백엔드 연동 테스트](#프론트엔드-백엔드-연동-테스트)
4. [날짜 계산 로직 검증](#날짜-계산-로직-검증)
5. [월별 지출 계산 검증](#월별-지출-계산-검증)
6. [배치 작업 테스트](#배치-작업-테스트)
7. [전체 시나리오 테스트](#전체-시나리오-테스트)

---

## 사전 준비

### 1. 백엔드 서버 실행

```bash
cd subscription-manager
mvnw.cmd spring-boot:run
# 또는
mvn spring-boot:run
```

서버가 정상적으로 실행되면:
- API 서버: `http://localhost:8090`
- H2 콘솔: `http://localhost:8090/h2-console`

### 2. 프론트엔드 서버 실행

```bash
cd frontend
npm install
npm run dev
```

프론트엔드가 정상적으로 실행되면:
- 개발 서버: `http://localhost:5173`

### 3. 테스트 도구 준비

- **cURL**: 명령줄에서 API 테스트
- **Postman** (선택사항): GUI 기반 API 테스트
- **브라우저**: 프론트엔드 테스트

---

## 백엔드 API 테스트

### 테스트 1: 구독 서비스 등록

#### 요청

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

#### 예상 응답

```json
{
  "id": 1,
  "serviceName": "넷플릭스",
  "price": 9500,
  "billingCycle": "MONTHLY",
  "billingDate": "2024-12-15",
  "nextBillingDate": "2025-01-15",
  "userId": 1
}
```

#### 검증 포인트

- ✅ HTTP 상태 코드: 201 Created
- ✅ `nextBillingDate`가 자동으로 계산됨 (billingDate + 1개월)
- ✅ 모든 필드가 올바르게 저장됨

---

### 테스트 2: 구독 목록 조회

#### 요청

```bash
curl http://localhost:8090/api/subscriptions?userId=1
```

#### 예상 응답

```json
[
  {
    "id": 1,
    "serviceName": "넷플릭스",
    "price": 9500,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15",
    "nextBillingDate": "2025-01-15",
    "userId": 1
  }
]
```

#### 검증 포인트

- ✅ HTTP 상태 코드: 200 OK
- ✅ 배열 형태로 반환됨
- ✅ 등록한 구독 서비스가 목록에 포함됨

---

### 테스트 3: 구독 서비스 수정

#### 요청

```bash
curl -X PUT http://localhost:8090/api/subscriptions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "넷플릭스 프리미엄",
    "price": 14500,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15"
  }'
```

#### 예상 응답

```json
{
  "id": 1,
  "serviceName": "넷플릭스 프리미엄",
  "price": 14500,
  "billingCycle": "MONTHLY",
  "billingDate": "2024-12-15",
  "nextBillingDate": "2025-01-15",
  "userId": 1
}
```

#### 검증 포인트

- ✅ HTTP 상태 코드: 200 OK
- ✅ 수정된 정보가 반영됨
- ✅ `nextBillingDate`가 재계산됨

---

### 테스트 4: 구독 서비스 삭제

#### 요청

```bash
curl -X DELETE http://localhost:8090/api/subscriptions/1
```

#### 예상 응답

- HTTP 상태 코드: 204 No Content
- 응답 본문 없음

#### 검증 포인트

- ✅ HTTP 상태 코드: 204 No Content
- ✅ 삭제 후 목록 조회 시 해당 항목이 없음

---

### 테스트 5: 결제 임박 목록 조회

#### 사전 준비

결제일이 3일 이내인 구독 서비스를 등록:

```bash
# 오늘 날짜 기준 2일 후 결제 예정
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "유튜브 프리미엄",
    "price": 11900,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-13"
  }'
```

#### 요청

```bash
curl http://localhost:8090/api/subscriptions/upcoming?userId=1
```

#### 예상 응답

```json
[
  {
    "id": 2,
    "serviceName": "유튜브 프리미엄",
    "price": 11900,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-13",
    "nextBillingDate": "2024-12-15",
    "userId": 1
  }
]
```

#### 검증 포인트

- ✅ HTTP 상태 코드: 200 OK
- ✅ 오늘부터 3일 이내 결제 예정인 구독만 반환됨
- ✅ 3일 이후 결제 예정인 구독은 제외됨

---

### 테스트 6: 월별 지출액 조회

#### 사전 준비

다양한 결제 주기의 구독 서비스를 등록:

```bash
# 매월 구독
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "넷플릭스",
    "price": 9500,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15"
  }'

# 연간 구독
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "헬스장",
    "price": 300000,
    "billingCycle": "YEARLY",
    "billingDate": "2024-12-15"
  }'

# 분기별 구독
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "음악 스트리밍",
    "price": 15000,
    "billingCycle": "QUARTERLY",
    "billingDate": "2024-12-15"
  }'
```

#### 요청

```bash
curl http://localhost:8090/api/subscriptions/monthly-expense?userId=1
```

#### 예상 응답

```json
{
  "userId": 1,
  "totalMonthlyExpense": 39000.00
}
```

#### 계산 검증

- 넷플릭스 (MONTHLY): 9,500원 → 월 9,500원
- 헬스장 (YEARLY): 300,000원 → 월 25,000원 (300,000 ÷ 12)
- 음악 스트리밍 (QUARTERLY): 15,000원 → 월 5,000원 (15,000 ÷ 3)
- **총 월별 지출액**: 9,500 + 25,000 + 5,000 = 39,500원

#### 검증 포인트

- ✅ HTTP 상태 코드: 200 OK
- ✅ 월 환산 계산이 정확함
- ✅ 소수점 2자리까지 정확하게 계산됨

---

## 프론트엔드-백엔드 연동 테스트

### 테스트 1: 구독 서비스 등록 (프론트엔드)

1. 브라우저에서 `http://localhost:5173` 접속
2. 구독 등록 폼에 다음 정보 입력:
   - 서비스명: "넷플릭스"
   - 가격: 9500
   - 결제 주기: 매월
   - 결제일: 2024-12-15
3. "등록" 버튼 클릭

#### 검증 포인트

- ✅ 폼 제출 시 로딩 스피너 표시
- ✅ 성공 시 성공 메시지 애니메이션 표시
- ✅ 구독 목록에 새로 등록한 항목이 표시됨
- ✅ 월별 지출 차트가 업데이트됨

---

### 테스트 2: 구독 목록 조회 (프론트엔드)

1. 페이지 로드 시 구독 목록이 자동으로 표시됨
2. 등록한 구독 서비스들이 리스트로 표시됨

#### 검증 포인트

- ✅ 페이지 로드 시 자동으로 데이터 조회
- ✅ 리스트 아이템이 stagger 효과로 등장
- ✅ hover 시 scale 효과 작동
- ✅ 각 항목에 서비스명, 가격, 결제일 등이 표시됨

---

### 테스트 3: 결제 임박 알림 (프론트엔드)

1. 결제일이 3일 이내인 구독 서비스를 등록
2. 상단의 "결제 임박 알림" 섹션 확인

#### 검증 포인트

- ✅ 결제 임박 구독이 카드 형태로 표시됨
- ✅ 결제일까지 남은 일수가 표시됨
- ✅ 슬라이드 인 애니메이션이 작동함

---

### 테스트 4: 월별 지출 차트 (프론트엔드)

1. 여러 구독 서비스를 등록
2. "월별 지출 차트" 섹션 확인

#### 검증 포인트

- ✅ 차트가 정상적으로 렌더링됨
- ✅ 등장 애니메이션이 작동함
- ✅ 차트 데이터가 올바르게 표시됨
- ✅ 월별 지출액이 정확하게 계산됨

---

## 날짜 계산 로직 검증

### 테스트 1: MONTHLY (매월)

#### 입력
- billingDate: `2024-12-15`
- billingCycle: `MONTHLY`

#### 예상 결과
- nextBillingDate: `2025-01-15`

#### 검증

```bash
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "테스트 서비스",
    "price": 10000,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15"
  }'
```

응답에서 `nextBillingDate`가 `2025-01-15`인지 확인

---

### 테스트 2: QUARTERLY (분기별)

#### 입력
- billingDate: `2024-12-15`
- billingCycle: `QUARTERLY`

#### 예상 결과
- nextBillingDate: `2025-03-15`

#### 검증

```bash
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "테스트 서비스",
    "price": 30000,
    "billingCycle": "QUARTERLY",
    "billingDate": "2024-12-15"
  }'
```

응답에서 `nextBillingDate`가 `2025-03-15`인지 확인

---

### 테스트 3: YEARLY (연간)

#### 입력
- billingDate: `2024-12-15`
- billingCycle: `YEARLY`

#### 예상 결과
- nextBillingDate: `2025-12-15`

#### 검증

```bash
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "테스트 서비스",
    "price": 120000,
    "billingCycle": "YEARLY",
    "billingDate": "2024-12-15"
  }'
```

응답에서 `nextBillingDate`가 `2025-12-15`인지 확인

---

## 월별 지출 계산 검증

### 테스트 케이스 1: 단일 구독 (MONTHLY)

#### 입력
- 넷플릭스: 9,500원 (MONTHLY)

#### 예상 결과
- totalMonthlyExpense: 9,500.00

---

### 테스트 케이스 2: 단일 구독 (YEARLY)

#### 입력
- 헬스장: 300,000원 (YEARLY)

#### 예상 결과
- totalMonthlyExpense: 25,000.00 (300,000 ÷ 12)

---

### 테스트 케이스 3: 단일 구독 (QUARTERLY)

#### 입력
- 음악 스트리밍: 15,000원 (QUARTERLY)

#### 예상 결과
- totalMonthlyExpense: 5,000.00 (15,000 ÷ 3)

---

### 테스트 케이스 4: 복합 구독

#### 입력
- 넷플릭스: 9,500원 (MONTHLY)
- 헬스장: 300,000원 (YEARLY)
- 음악 스트리밍: 15,000원 (QUARTERLY)

#### 계산 과정
1. 넷플릭스: 9,500원 (그대로)
2. 헬스장: 300,000 ÷ 12 = 25,000원
3. 음악 스트리밍: 15,000 ÷ 3 = 5,000원

#### 예상 결과
- totalMonthlyExpense: 39,500.00

---

## 배치 작업 테스트

### 테스트 방법

배치 작업을 즉시 테스트하려면 `BillingScheduler.java`의 cron 표현식을 임시로 변경:

```java
// 원래: @Scheduled(cron = "0 0 9 * * *")  // 매일 오전 9시
// 테스트용: @Scheduled(fixedRate = 60000)  // 1분마다 실행
```

### 테스트 단계

1. `BillingScheduler.java` 수정
2. 애플리케이션 재시작
3. 결제일이 3일 이내인 구독 서비스 등록
4. 1분 대기
5. 콘솔 로그 확인

### 예상 로그 출력

```
=== 결제 임박 구독 서비스 확인 배치 작업 시작 ===
조회 기간: 2024-12-15 ~ 2024-12-18
결제 임박 구독 서비스 1개를 찾았습니다:
  - 서비스명: 유튜브 프리미엄, 가격: 11900원, 결제일: 2024-12-17 (2일 후), 사용자 ID: 1
=== 결제 임박 구독 서비스 확인 배치 작업 종료 ===
```

### 검증 포인트

- ✅ 배치 작업이 정상적으로 실행됨
- ✅ 3일 이내 결제 예정인 구독이 조회됨
- ✅ 로그가 올바르게 출력됨
- ✅ 테스트 후 cron 표현식을 원래대로 복구

---

## 전체 시나리오 테스트

### 시나리오: 완전한 사용자 플로우

1. **구독 서비스 등록**
   - 넷플릭스: 9,500원 (MONTHLY), 결제일: 2024-12-15
   - 유튜브 프리미엄: 11,900원 (MONTHLY), 결제일: 2024-12-20
   - 헬스장: 300,000원 (YEARLY), 결제일: 2024-12-15

2. **구독 목록 확인**
   - 등록한 3개의 구독이 목록에 표시됨
   - 각 구독의 다음 결제일이 올바르게 계산됨

3. **월별 지출액 확인**
   - 넷플릭스: 9,500원
   - 유튜브 프리미엄: 11,900원
   - 헬스장: 25,000원 (300,000 ÷ 12)
   - **총 월별 지출액**: 46,400원

4. **결제 임박 알림 확인**
   - 오늘 날짜 기준 3일 이내 결제 예정인 구독 확인

5. **구독 수정**
   - 넷플릭스 가격을 14,500원으로 수정
   - 월별 지출액이 업데이트됨

6. **구독 삭제**
   - 유튜브 프리미엄 삭제
   - 목록에서 제거됨
   - 월별 지출액이 업데이트됨

### 검증 포인트

- ✅ 모든 기능이 정상적으로 작동함
- ✅ 데이터 일관성이 유지됨
- ✅ 프론트엔드와 백엔드가 올바르게 연동됨
- ✅ 애니메이션이 정상적으로 작동함

---

## 문제 해결

### 백엔드 서버가 시작되지 않는 경우

1. 포트 충돌 확인: `application.properties`에서 포트 변경
2. Java 버전 확인: Java 17 이상 필요
3. Maven 의존성 확인: `mvn clean install`

### 프론트엔드가 API를 호출하지 못하는 경우

1. API 베이스 URL 확인: `frontend/src/services/api.ts`
2. CORS 설정 확인: 백엔드에서 CORS 허용 설정
3. 네트워크 탭에서 에러 확인

### 날짜 계산이 잘못된 경우

1. `DateCalculator.java` 확인
2. `BillingCycle` Enum 값 확인
3. 로그에서 실제 계산 값 확인

### 월별 지출 계산이 잘못된 경우

1. `SubscriptionService.getMonthlyExpense()` 확인
2. 각 구독의 `billingCycle` 확인
3. BigDecimal 계산 로직 확인

---

**작성일:** 2024년 12월  
**버전:** 1.0.0

