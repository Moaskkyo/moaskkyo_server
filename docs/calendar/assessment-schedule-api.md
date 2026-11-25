# 캘린더 수행평가 일정 조회 — API 명세서

`GET /api/calendar/assessments`

# 개요

- 캘린더 화면에 보이는 기간의 수행평가 일정을 조회하는 기능입니다.
- 로그인한 학생의 반이 대상인 현재 학년도 수행평가만 반환합니다.

# 기술 설명/요구 사항

- **대상 판별**: 요청 값이 아니라 로그인한 학생의 `school_id`, `grade`, `class_number`로 조회합니다.
- **학년도**: 조회 시점의 서버 날짜(한국 시간) 기준으로 계산합니다. 3월 1일부터 그 해의 학년도이고, 1~2월은 전년도 학년도입니다.
    - 조회 범위가 아니라 오늘 기준이므로, 지난 학년도에 해당하는 달을 보면 빈 목록이 내려갑니다.
- **기간 일정**: 조회 범위와 하루라도 겹치면 포함합니다.
- **표시 설정**: `calendar_setting.show_assessment`가 off면 목록을 조회하지 않습니다.

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

- 수행평가 표시가 ON일 경우

```json
{
  "startDate": "2026-09-27",
  "endDate": "2026-10-31",
  "enabled": true,
  "assessments": [
    { "assessmentId": 12, "subjectName": "국어", "title": "독서 감상문", "startDate": "2026-10-08", "endDate": "2026-10-08" },
    { "assessmentId": 15, "subjectName": "과학", "title": "실험 보고서", "startDate": "2026-10-20", "endDate": "2026-10-23" }
  ]
}
```

- OFF

```json
{
  "startDate": "2026-09-27",
  "endDate": "2026-10-31",
  "enabled": false,
  "assessments": []
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| startDate | String | O | 조회 시작일 (`yyyy-MM-dd`) |
| endDate | String | O | 조회 종료일 (`yyyy-MM-dd`) |
| enabled | Boolean | O | 수행평가 표시 설정. `false`면 `assessments`는 빈 배열 |
| assessments | Array | O | 수행평가 목록 (없으면 빈 배열) |
| assessments[].assessmentId | Long | O | 수행평가 아이디 (메모 연결용) |
| assessments[].subjectName | String | O | 과목 이름 |
| assessments[].title | String | O | 제목 |
| assessments[].startDate | String | O | 시작일 (`yyyy-MM-dd`) |
| assessments[].endDate | String | O | 종료일 (`yyyy-MM-dd`), 하루 일정이면 시작일과 같음 |

- **정렬**: 시작일 오름차순, 같으면 아이디 오름차순

## 400 Bad Request

- `startDate` 또는 `endDate` 누락
- 날짜 형식 오류 (예: `20261001`, `2026-13-01`)
- `startDate`가 `endDate`보다 늦음
- 조회 범위가 42일 초과 (시작일·종료일 포함 42일까지 허용)

## 401 Unauthorized

- 토큰 없음 / 만료 / 위조

## 403 Forbidden

- 선생님 토큰으로 호출 (선생님 조회가 구현되기 전까지)

## 404 Not Found

- 토큰의 사용자 또는 학교가 존재하지 않음
- 학생의 `calendar_setting`이 없음 (회원가입 시 생성 누락)

## 500 Internal Server Error

# 데이터 구조

처음 ERD에서 아래와 같이 바꿨습니다.

**`assessment`**

| 컬럼 | 타입 | NULL | 변경 |
| --- | --- | --- | --- |
| assessment_id | BIGINT | NOT NULL | 유지 |
| school_id | BIGINT | NOT NULL | 유지 |
| subject_id | BIGINT | NOT NULL | 유지 |
| school_year | INT | NOT NULL | 추가 (예: 2026) |
| grade | INT | NOT NULL | 유지 |
| title | VARCHAR | NOT NULL | 유지 |
| start_date | DATE | NOT NULL | `deadline`을 대체 |
| end_date | DATE | NOT NULL | 추가, 하루 일정이면 시작일과 같음 |
| ~~class_number~~ | | | 삭제, `assessment_class`로 이동 |
| ~~deadline~~ | | | 삭제 |

**`assessment_class` (신규)** — 수행평가의 대상 반

| 컬럼 | 타입 | NULL | 비고 |
| --- | --- | --- | --- |
| assessment_class_id | BIGINT | NOT NULL | PK |
| assessment_id | BIGINT | NOT NULL | FK |
| class_number | INT | NOT NULL | `(assessment_id, class_number)` 유니크 |

- `subject`, `calendar_setting.show_assessment`, `calendar_detail.assessment_id`는 ERD 그대로입니다.
- 등록한 선생님(`teacher_id`)은 아직 두지 않습니다.

# 결정사항

| 항목 | 결정 |
| --- | --- |
| 여러 반 대상 | 수행평가는 한 행으로 두고 대상 반을 `assessment_class`에 여러 행으로 둔다 |
| 날짜 | 시작일·종료일 두 개만 둔다. 실시일과 제출 마감일은 구분하지 않는다 |
| 학년도 | `school_year` 컬럼을 두고, 3월 1일 기준 현재 학년도 것만 조회한다 |
| 지난 학년도 | 학생의 과거 학년·반 기록이 없어 조회하지 않는다 |
| 접근 권한 | 로그인한 사용자(authenticated). 현재는 학생만 조회할 수 있고 선생님은 403 |
