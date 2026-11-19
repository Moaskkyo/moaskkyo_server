# 캘린더 학사일정 조회 — API 명세서

`GET /api/calendar/academics`

# 개요

- 캘린더 화면에 보이는 기간의 학사일정을 조회하는 기능입니다.
- 공휴일, NEIS 학사일정, 선생님이 등록한 학사일정을 합쳐서 반환합니다.

# 기술 설명/요구 사항

- **학교 판별**: 요청 값이 아니라 로그인한 사용자(학생·선생님)의 `school_id`로 조회합니다.

**데이터 출처**

| type | 출처 | on/off | 세부 내용(메모) |
| --- | --- | --- | --- |
| `HOLIDAY` | 공휴일 API (서버 호출, 공공데이터포털 한국천문연구원 특일 정보 `getRestDeInfo`) | 없음, 항상 반환 | 불가 |
| `NEIS` | NEIS 학사일정 API (서버 호출, `school.atpt_code`·`school_code` 사용) | `show_academic` | 가능 (`neisKey`) |
| `REGISTERED` | `academic` 테이블 | `show_academic` | 가능 (`academicId`) |

- **선생님**: 캘린더 설정이 없으므로 항상 on으로 조회합니다.
- **기간 일정**: 조회 범위와 하루라도 겹치면 포함합니다.
- 외부 데이터
    - 캘린더에 필요 없는 NEIS 항목(예: 토요휴업일)은 제외합니다.
    - `HOLIDAY`와 `NEIS`가 같은 날짜·같은 이름이면 `HOLIDAY`만 남깁니다.
- **`neisKey` 생성 규칙**
    - `시작일(yyyyMMdd)_제목` (예: `20261013_중간고사`)

# Request

## Header

| 이름 | 요구 | 형식 |
| --- | --- | --- |
| Authorization | O | Bearer {accessToken} |

## Query Parameter

| 이름 | 타입 | 필수 | 기본값 | 예시 |
| --- | --- | --- | --- | --- |
| startDate | String (`yyyy-MM-dd`) | O | - | `2026-09-27` |
| endDate | String (`yyyy-MM-dd`) | O | - | `2026-10-31` |

# Response

## 200 Ok

### Body

> 공휴일과 학사일정을 하나의 API에서 같이 답변 하기 때문에 이런 구조를 사용.

- 학사일정이 ON일 경우

```json
{
  "startDate": "2026-09-27",
  "endDate": "2026-10-31",
  "enabled": true,
  "schedules": [
    { "type": "HOLIDAY",    "academicId": null, "neisKey": null,                "title": "개천절",   "startDate": "2026-10-03", "endDate": "2026-10-03" },
    { "type": "HOLIDAY",    "academicId": null, "neisKey": null,                "title": "한글날",   "startDate": "2026-10-09", "endDate": "2026-10-09" },
    { "type": "NEIS",       "academicId": null, "neisKey": "20261013_중간고사", "title": "중간고사", "startDate": "2026-10-13", "endDate": "2026-10-15" },
    { "type": "REGISTERED", "academicId": 3,    "neisKey": null,                "title": "체육대회", "startDate": "2026-10-22", "endDate": "2026-10-22" },
    { "type": "REGISTERED", "academicId": 9,    "neisKey": null,                "title": "수학여행", "startDate": "2026-10-28", "endDate": "2026-10-30" }
  ]
}
```

- OFF

```json
{
  "startDate": "2026-09-27",
  "endDate": "2026-10-31",
  "enabled": false,
  "schedules": [
    { "type": "HOLIDAY", "academicId": null, "neisKey": null, "title": "개천절", "startDate": "2026-10-03", "endDate": "2026-10-03" },
    { "type": "HOLIDAY", "academicId": null, "neisKey": null, "title": "한글날", "startDate": "2026-10-09", "endDate": "2026-10-09" }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| startDate | String | O | 조회 시작일 (`yyyy-MM-dd`) |
| endDate | String | O | 조회 종료일 (`yyyy-MM-dd`) |
| enabled | Boolean | O | 학사일정 표시 설정 (선생님은 항상 `true`) |
| schedules | Array | O | 일정 목록 (없으면 빈 배열) |
| schedules[].type | String (ENUM) | O | `HOLIDAY`(공휴일), `NEIS`(NEIS 학사일정), `REGISTERED`(등록 학사일정) |
| schedules[].academicId | Long | X | 등록 학사일정 아이디. `REGISTERED`만 값이 있고 나머지는 `null` |
| schedules[].neisKey | String | X | NEIS 일정 식별 키. `NEIS`만 값이 있고 나머지는 `null` |
| schedules[].title | String | O | 일정 제목 (생략되지 않은 전체 제목) |
| schedules[].startDate | String | O | 시작일 (`yyyy-MM-dd`) |
| schedules[].endDate | String | O | 종료일 (`yyyy-MM-dd`), 하루 일정이면 시작일과 같음 |

## 400 Bad Request

- `startDate` 또는 `endDate` 누락
- 날짜 형식 오류 (예: `20261001`, `2026-13-01`)
- `startDate`가 `endDate`보다 늦음
- 조회 범위가 42일 초과

## 401 Unauthorized

- 토큰 없음 / 만료 / 위조

## 403 Forbidden

- 선생님 토큰으로 호출 (선생님 조회가 구현되기 전까지)

## 404 Not Found

- 토큰의 사용자 또는 학교가 존재하지 않음
- 학생의 `calendar_setting`이 없음 (회원가입 시 생성 누락)

## 500 Internal Server Error

- 공휴일 API 또는 NEIS API 호출 실패 (`code`: `EXTERNAL_API_ERROR`). 일부만 담은 응답은 내려주지 않습니다.

# 구현 결정사항

명세에 없어 구현하면서 정한 내용입니다.

| 항목 | 결정 |
| --- | --- |
| 접근 권한 | 로그인한 사용자(authenticated). 현재는 학생만 조회할 수 있고 선생님은 403 |
| 연속 일정 기준 | 같은 이름이 달력상 하루도 끊기지 않고 이어질 때만 하나로 묶는다. 주말로 끊기면 별개 일정 |
| 공휴일 묶기 | 공휴일도 같은 이름이 연속되면(예: 추석 연휴) 하나로 묶는다 |
| 범위 경계 일정 | 외부 API는 조회 범위 앞뒤 14일을 넓혀 호출해 묶은 뒤, 조회 범위와 겹치는 일정만 반환한다. 화면 범위가 달라도 `neisKey`와 `startDate`가 같게 유지된다 |
| 중복 제거 | 묶기 전에 날짜 단위로 비교해, 공휴일과 날짜·이름이 같은 NEIS 항목을 뺀다 |
| 제외하는 NEIS 항목 | `토요휴업일` |
| 42일 계산 | 시작일·종료일을 포함해 42일까지 허용 |
| 정렬 | 시작일 오름차순, 같으면 `HOLIDAY` → `NEIS` → `REGISTERED` |
| 캐싱 | 없음. 요청마다 외부 API를 호출한다 |
