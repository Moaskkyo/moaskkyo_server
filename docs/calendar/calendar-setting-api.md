# 캘린더 조회 on/off 설정 변경 — API 명세서

`PATCH /api/calendar/settings`

# 개요

- 캘린더에 학사일정, 수행평가, 생일을 표시할지 여부를 변경하는 기능입니다.
- 바꿀 항목만 보내는 부분 수정이며, 변경 후 전체 설정을 반환합니다.

# Request

## Header

| 이름 | 요구 | 형식 |
| --- | --- | --- |
| Content-Type | O | application/json |
| Authorization | O | Bearer {accessToken} |

## Body

```json
{
  "showAssessment": false
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| showAcademic | Boolean | X | 학사일정 표시 여부 |
| showAssessment | Boolean | X | 수행평가 표시 여부 |
| showBirthday | Boolean | X | 생일 표시 여부 |

- 세 필드 중 **최소 하나**는 있어야 합니다.

# Response

## 200 OK

```json
{
  "showAcademic": true,
  "showAssessment": false,
  "showBirthday": true
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| showAcademic | Boolean | O | 변경 후 학사일정 표시 여부 |
| showAssessment | Boolean | O | 변경 후 수행평가 표시 여부 |
| showBirthday | Boolean | O | 변경 후 생일 표시 여부 |

- 변경하지 않은 항목까지 **전체 설정**을 반환합니다.
- 이미 같은 값이어도 에러 없이 200을 반환합니다.

## 에러

| 상태 | 경우 |
| --- | --- |
| 400 | 세 필드가 모두 없음, 값이 Boolean이 아님 |
| 401 | 토큰 없음 / 만료 / 위조 |
| 403 | 선생님 토큰으로 호출 |
| 404 | 사용자 없음, 학생의 캘린더 설정 없음 |

# 구현 결정사항

명세에 없어 구현하면서 정한 내용입니다.

| 항목 | 결정 |
| --- | --- |
| 메서드·URL | `PATCH /api/calendar/settings` |
| 접근 권한 | 로그인한 사용자(authenticated). 선생님 토큰은 서비스에서 403 |
| Boolean 검증 | JSON의 `true`/`false`만 허용한다. `"true"`(문자열), `1`(숫자)은 400 |
| `null` 값 | 필드를 `null`로 보내면 보내지 않은 것과 같게 본다. 세 필드가 모두 없거나 `null`이면 400 |
| 모르는 필드 | 무시한다. 아는 필드가 하나도 없으면 400 |
| 400 응답 코드 | 모두 `VALIDATION_FAILED` |
