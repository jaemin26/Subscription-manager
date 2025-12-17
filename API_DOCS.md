# 📡 API 문서

구독 서비스 매니저 REST API 상세 문서입니다.

## 기본 정보

- **Base URL**: `http://localhost:8090`
- **Content-Type**: `application/json`
- **인코딩**: UTF-8

## 공통 응답 형식

### 성공 응답
- **200 OK**: 조회 성공
- **201 Created**: 생성 성공
- **204 No Content**: 삭제 성공

### 에러 응답
- **400 Bad Request**: 잘못된 요청 (검증 실패)
- **404 Not Found**: 리소스를 찾을 수 없음
- **500 Internal Server Error**: 서버 내부 오류

## API 엔드포인트

### 1. 구독 서비스 등록

**POST** `/api/subscriptions`

구독 서비스를 등록합니다. 다음 결제일은 자동으로 계산됩니다.

#### 요청 본문

```json
{
  "userId": 1,
  "serviceName": "넷플릭스",
  "price": 9500,
  "billingCycle": "MONTHLY",
  "billingDate": "2024-12-15"
}
```

#### 필드 설명

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Long | ✅ | 사용자 ID (양수) |
| serviceName | String | ✅ | 서비스 이름 (최대 100자) |
| price | BigDecimal | ✅ | 가격 (0보다 큰 값, 소수점 2자리) |
| billingCycle | String | ✅ | 결제 주기 (MONTHLY, QUARTERLY, YEARLY) |
| billingDate | String | ✅ | 결제일 (YYYY-MM-DD 형식) |

#### 응답 예시

**201 Created**

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

#### 다음 결제일 계산 규칙

- **MONTHLY**: 결제일 + 1개월
- **QUARTERLY**: 결제일 + 3개월
- **YEARLY**: 결제일 + 1년

#### cURL 예시

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

---

### 2. 구독 서비스 목록 조회

**GET** `/api/subscriptions?userId={userId}`

사용자의 모든 구독 서비스 목록을 조회합니다.

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### 응답 예시

**200 OK**

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
  },
  {
    "id": 2,
    "serviceName": "유튜브 프리미엄",
    "price": 11900,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-20",
    "nextBillingDate": "2025-01-20",
    "userId": 1
  }
]
```

#### cURL 예시

```bash
curl http://localhost:8090/api/subscriptions?userId=1
```

---

### 3. 구독 서비스 상세 조회

**GET** `/api/subscriptions/{id}`

특정 구독 서비스의 상세 정보를 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 구독 서비스 ID |

#### 응답 예시

**200 OK**

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

#### 에러 응답

**404 Not Found**

```json
{
  "timestamp": "2024-12-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "구독 서비스를 찾을 수 없습니다. ID: 999",
  "path": "/api/subscriptions/999"
}
```

#### cURL 예시

```bash
curl http://localhost:8090/api/subscriptions/1
```

---

### 4. 구독 서비스 수정

**PUT** `/api/subscriptions/{id}`

구독 서비스 정보를 수정합니다. 수정 시 다음 결제일이 자동으로 재계산됩니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 구독 서비스 ID |

#### 요청 본문

```json
{
  "userId": 1,
  "serviceName": "넷플릭스 프리미엄",
  "price": 14500,
  "billingCycle": "MONTHLY",
  "billingDate": "2024-12-15"
}
```

#### 응답 예시

**200 OK**

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

#### cURL 예시

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

---

### 5. 구독 서비스 삭제

**DELETE** `/api/subscriptions/{id}`

구독 서비스를 삭제합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 구독 서비스 ID |

#### 응답 예시

**204 No Content**

응답 본문 없음

#### cURL 예시

```bash
curl -X DELETE http://localhost:8090/api/subscriptions/1
```

---

### 6. 결제 임박 구독 서비스 조회

**GET** `/api/subscriptions/upcoming?userId={userId}`

오늘 날짜 기준 3일 이내 결제 예정인 구독 서비스 목록을 조회합니다.

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### 조회 조건

- `today <= nextBillingDate <= today + 3일`

#### 응답 예시

**200 OK**

```json
[
  {
    "id": 1,
    "serviceName": "넷플릭스",
    "price": 9500,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15",
    "nextBillingDate": "2024-12-18",
    "userId": 1
  }
]
```

#### cURL 예시

```bash
curl http://localhost:8090/api/subscriptions/upcoming?userId=1
```

---

### 7. 월별 지출액 조회

**GET** `/api/subscriptions/monthly-expense?userId={userId}`

사용자의 모든 구독 서비스를 월별로 환산한 총 지출액을 조회합니다.

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### 월 환산 계산 공식

- **MONTHLY**: `price` (그대로)
- **QUARTERLY**: `price ÷ 3`
- **YEARLY**: `price ÷ 12`

#### 응답 예시

**200 OK**

```json
{
  "userId": 1,
  "totalMonthlyExpense": 21416.67
}
```

#### 계산 예시

사용자가 다음 구독 서비스를 가지고 있다고 가정:

1. 넷플릭스: 9,500원 (MONTHLY) → 월 9,500원
2. 유튜브 프리미엄: 11,900원 (MONTHLY) → 월 11,900원
3. 헬스장: 300,000원 (YEARLY) → 월 25,000원 (300,000 ÷ 12)
4. 음악 스트리밍: 15,000원 (QUARTERLY) → 월 5,000원 (15,000 ÷ 3)

**총 월별 지출액**: 9,500 + 11,900 + 25,000 + 5,000 = 51,400원

#### cURL 예시

```bash
curl http://localhost:8090/api/subscriptions/monthly-expense?userId=1
```

---

## 데이터 타입

### BillingCycle (Enum)

| 값 | 설명 |
|----|------|
| MONTHLY | 매월 |
| QUARTERLY | 분기별 (3개월) |
| YEARLY | 연간 (12개월) |

### 날짜 형식

- **형식**: `YYYY-MM-DD`
- **예시**: `2024-12-15`
- **타입**: `LocalDate` (Java), `string` (JSON)

### 금액 형식

- **타입**: `BigDecimal` (Java), `number` (JSON)
- **소수점**: 최대 2자리
- **예시**: `9500.00`, `14500.50`

---

## 에러 처리

### 검증 실패 (400 Bad Request)

요청 데이터가 검증 규칙을 위반한 경우:

```json
{
  "timestamp": "2024-12-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "검증 실패",
  "errors": [
    {
      "field": "price",
      "message": "가격은 0보다 커야 합니다."
    },
    {
      "field": "serviceName",
      "message": "서비스 이름은 필수입니다."
    }
  ],
  "path": "/api/subscriptions"
}
```

### 리소스 없음 (404 Not Found)

요청한 리소스를 찾을 수 없는 경우:

```json
{
  "timestamp": "2024-12-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "구독 서비스를 찾을 수 없습니다. ID: 999",
  "path": "/api/subscriptions/999"
}
```

---

## 테스트 시나리오

### 시나리오 1: 구독 서비스 등록 및 조회

1. 구독 서비스 등록
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

2. 구독 목록 조회
```bash
curl http://localhost:8090/api/subscriptions?userId=1
```

3. 상세 조회
```bash
curl http://localhost:8090/api/subscriptions/1
```

### 시나리오 2: 월별 지출액 계산

1. 여러 구독 서비스 등록
   - 넷플릭스: 9,500원 (MONTHLY)
   - 헬스장: 300,000원 (YEARLY)
   - 음악 스트리밍: 15,000원 (QUARTERLY)

2. 월별 지출액 조회
```bash
curl http://localhost:8090/api/subscriptions/monthly-expense?userId=1
```

예상 결과: `9,500 + 25,000 + 5,000 = 39,500원`

### 시나리오 3: 결제 임박 알림

1. 결제일이 3일 이내인 구독 서비스 등록
```bash
curl -X POST http://localhost:8090/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "유튜브 프리미엄",
    "price": 11900,
    "billingCycle": "MONTHLY",
    "billingDate": "2024-12-15"
  }'
```

2. 결제 임박 목록 조회
```bash
curl http://localhost:8090/api/subscriptions/upcoming?userId=1
```

---

## 주의사항

1. **사용자 ID**: 현재 버전에서는 사용자 인증이 없으므로, 임시로 `userId=1`을 사용합니다.
2. **날짜 계산**: 다음 결제일은 등록/수정 시 자동으로 계산됩니다.
3. **금액 정밀도**: 모든 금액 계산은 `BigDecimal`을 사용하여 정밀도를 보장합니다.
4. **월 환산**: 분기별/연간 구독은 월별로 환산하여 계산됩니다.

---

**작성일:** 2024년 12월  
**버전:** 1.0.0

