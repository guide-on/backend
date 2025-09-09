# 신용평가 API 사용 예제

## 1. 신용평가 데이터 생성 (POST)

### URL: `POST /api/credit-evaluation`

```json
{
    "userId": "user003",
    "totalOverdueCount": 1,
    "recent12mOverdueCount": 0,
    "maxOverdueDays": 7,
    "currentOverdueAmount": 0.00,
    "loanDefaultHistory": 0,
    "creditCardDelayRate": 2.50,
    "paymentConsistencyScore": 88,
    "totalDebtAmount": 35000000.00,
    "monthlyIncome": 4500000.00,
    "debtToIncomeRatio": 93.33,
    "creditCardUtilizationRate": 28.00,
    "securedVsUnsecuredRatio": 2.20,
    "creditHistoryMonths": 42,
    "oldestCreditAccountMonths": 48,
    "newCreditInquiries6m": 2,
    "activeCreditCardCount": 3,
    "totalCreditLimit": 18000000.00,
    "loanTypeDiversity": 2,
    "financialInstitutionCount": 4,
    "alternativeCreditScore": 80
}
```

## 2. 신용평가 데이터 수정 (PUT)

### URL: `PUT /api/credit-evaluation`

```json
{
    "userId": "user003",
    "evaluationDate": "2024-01-15 10:30:00",
    "totalOverdueCount": 0,
    "recent12mOverdueCount": 0,
    "maxOverdueDays": 0,
    "currentOverdueAmount": 0.00,
    "loanDefaultHistory": 0,
    "creditCardDelayRate": 1.20,
    "paymentConsistencyScore": 92,
    "totalDebtAmount": 33000000.00,
    "monthlyIncome": 4500000.00,
    "debtToIncomeRatio": 88.00,
    "creditCardUtilizationRate": 25.00,
    "securedVsUnsecuredRatio": 2.50,
    "creditHistoryMonths": 43,
    "oldestCreditAccountMonths": 49,
    "newCreditInquiries6m": 1,
    "activeCreditCardCount": 3,
    "totalCreditLimit": 18000000.00,
    "loanTypeDiversity": 2,
    "financialInstitutionCount": 4,
    "alternativeCreditScore": 83
}
```

## 3. 특정 평가일자 신용평가 데이터 조회 (GET)

### URL: `GET /api/credit-evaluation/user001?evaluationDate=2024-01-15 10:30:00`

## 4. 최신 신용평가 데이터 조회 (GET)

### URL: `GET /api/credit-evaluation/user001/latest`

## 5. 신용평가 데이터 목록 조회 (GET)

### URL: `GET /api/credit-evaluation?page=1&limit=10&sortBy=evaluationDate&sortOrder=DESC`

### 필터링 예제:
- 특정 사용자: `GET /api/credit-evaluation?userId=user001&page=1&limit=5`
- 날짜 범위: `GET /api/credit-evaluation?startDate=2024-01-01 00:00:00&endDate=2024-12-31 23:59:59`

## 6. 사용자 신용평가 이력 조회 (GET)

### URL: `GET /api/credit-evaluation/user001/history?page=1&limit=10`

## 7. 신용평가 데이터 삭제 (DELETE)

### URL: `DELETE /api/credit-evaluation/user001?evaluationDate=2024-01-15 10:30:00`

## 8. 사용자의 모든 신용평가 데이터 삭제 (DELETE)

### URL: `DELETE /api/credit-evaluation/user001/all`

## 응답 예제

### 성공 응답:
```json
{
    "success": true,
    "message": "신용평가 데이터가 성공적으로 생성되었습니다.",
    "data": {
        "userId": "user003",
        "evaluationDate": "2024-03-15T14:30:00.000+00:00",
        "totalOverdueCount": 1,
        "recent12mOverdueCount": 0,
        "maxOverdueDays": 7,
        "currentOverdueAmount": 0.00,
        "loanDefaultHistory": 0,
        "creditCardDelayRate": 2.50,
        "paymentConsistencyScore": 88,
        "totalDebtAmount": 35000000.00,
        "monthlyIncome": 4500000.00,
        "debtToIncomeRatio": 93.33,
        "creditCardUtilizationRate": 28.00,
        "securedVsUnsecuredRatio": 2.20,
        "creditHistoryMonths": 42,
        "oldestCreditAccountMonths": 48,
        "newCreditInquiries6m": 2,
        "activeCreditCardCount": 3,
        "totalCreditLimit": 18000000.00,
        "loanTypeDiversity": 2,
        "financialInstitutionCount": 4,
        "alternativeCreditScore": 80,
        "createdAt": "2024-03-15T14:30:00.000+00:00",
        "updatedAt": "2024-03-15T14:30:00.000+00:00"
    }
}
```

### 에러 응답:
```json
{
    "success": false,
    "message": "해당 신용평가 데이터를 찾을 수 없습니다.",
    "data": null
}
```

## 유효성 검증 규칙

- **userId**: 필수, 비어있지 않은 문자열
- **creditCardDelayRate**: 0-100% 범위
- **paymentConsistencyScore**: 0-100 범위
- **creditCardUtilizationRate**: 0-100% 범위
- **loanTypeDiversity**: 0-5 범위
- **alternativeCreditScore**: 0-100 범위
- **loanDefaultHistory**: 0(없음) 또는 1(있음)
- **금액 필드들**: 0 이상
- **개수 필드들**: 0 이상의 정수
