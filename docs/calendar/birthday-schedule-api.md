# 캘린더 생일 조회 — API 명세서

`GET /api/calendar/birthdays`

# 개요

- 캘린더 화면에 보이는 기간의 생일을 조회하는 기능입니다.
- 로그인한 학생과 같은 학교 학생 중 생일을 공개한 학생만 반환합니다.

# 기술 설명/요구 사항

- **대상 판별**: 요청 값이 아니라 로그인한 학생의 `school_id`로 조회합니다. 전 학년이 대상입니다.
- **공개 여부**: `user_setting.birthday_public`이 `true`인 학생만 포함합니다. `user_setting`이 없는 학생은 포함하지 않습니다.
- **날짜**: 태어난 연도는 내려주지 않고, 조회 범위에 해당하는 연도의 날짜로 내려줍니다.
    - 조회 범위가 연말을 걸치면 12월 생일은 앞 해, 1월 생일은 다음 해 날짜로 내려갑니다.
    - 2월 29일생은 윤년이 아닌 해에 2월 28일로 내려갑니다.
- **표시 설정**: `calendar_setting.show_birthday`가 off면 목록을 조회하지 않습니다.

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

- 생일 표시가 ON일 경우

```json
{
  "startDate": "2026-09-27",
  "endDate": "2026-10-31",
  "enabled": true,
  "birthdays": [
    { "userId": 31, "name": "이서준",   "grade": 2, "classNumber": 3, "studentNumber": 15, "date": "2026-10-06" },
    { "userId": 58, "name": "박지우",   "grade": 1, "classNumber": 2, "studentNumber": 7,  "date": "2026-10-20" },
    { "userId": 74, "name": "남궁하린", "grade": 3, "classNumber": 1, "studentNumber": 4,  "date": "2026-10-20" }
  ]
}
```

- OFF

```json
{
  "startDate": "2026-09-27",
  "endDate": "2026-10-31",
  "enabled": false,
  "birthdays": []
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| startDate | String | O | 조회 시작일 (`yyyy-MM-dd`) |
| endDate | String | O | 조회 종료일 (`yyyy-MM-dd`) |
| enabled | Boolean | O | 생일 표시 설정. `false`면 `birthdays`는 빈 배열 |
| birthdays | Array | O | 생일 목록 (없으면 빈 배열) |
| birthdays[].userId | Long | O | 학생 아이디 |
| birthdays[].name | String | O | 학생 이름 (생략되지 않은 전체 이름) |
| birthdays[].grade | Integer | O | 학년 |
| birthdays[].classNumber | Integer | O | 반 |
| birthdays[].studentNumber | Integer | O | 번호 |
| birthdays[].date | String | O | 조회 범위 안의 생일 날짜 (`yyyy-MM-dd`). 태어난 연도가 아님 |

- **정렬**: 날짜 오름차순, 같으면 학년 → 반 → 번호 오름차순

## 400 Bad Request

- `startDate` 또는 `endDate` 누락
- 날짜 형식 오류 (예: `20261001`, `2026-13-01`)
- `startDate`가 `endDate`보다 늦음
- 조회 범위가 42일 초과 (시작일·종료일 포함 42일까지 허용)

## 401 Unauthorized

- 토큰 없음 / 만료 / 위조

## 403 Forbidden

- 선생님 토큰으로 호출

## 404 Not Found

- 토큰의 사용자가 존재하지 않음
- 학생의 `calendar_setting`이 없음 (회원가입 시 생성 누락)

## 500 Internal Server Error

# 데이터 구조

ERD 그대로이며, `user_setting` 엔티티를 이번에 추가했습니다.

**`user_setting`**

| 컬럼 | 타입 | NULL | 비고 |
| --- | --- | --- | --- |
| user_setting_id | BIGINT | NOT NULL | PK |
| user_id | BIGINT | NOT NULL | FK, 유니크 |
| birthday_public | BOOLEAN | NOT NULL | 생일 공개 여부 |
| main_image | VARCHAR | NULL | 메인페이지 이미지 |

- `user.birth`, `calendar_setting.show_birthday`는 기존 그대로 사용합니다.

# 결정사항

| 항목 | 결정 |
| --- | --- |
| 이름 생략 | 서버는 전체 이름을 내려주고, 3자 생략 표시는 화면에서 처리한다 |
| 본인 생일 | 본인도 생일을 공개했으면 목록에 포함된다 |
| 2월 29일생 | 윤년이 아닌 해에는 2월 28일로 내려준다 |
| 접근 권한 | 로그인한 사용자(authenticated). 학생만 조회할 수 있고 선생님은 403 |
