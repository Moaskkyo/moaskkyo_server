# CLAUDE.md

## 프로젝트 중심 규칙
코드에선 가독성과 기존의 코드베이스를 잘 지키는 것이 우선입니다.
아키텍쳐 적으로 바뀌는 부분이 있다면 작업전에 꼭 검토요청을 하세요.
API 스펙, DB 구조, 인증/인가 구조 변경은 승인 후 진행합니다.

## Working Rules
- default branch는 develop이다.
- 기능 작업은 최신 develop 기준으로 브랜치를 생성한 상태가 기본 설정이다.

## Project Structure
~/server/domain
~/server/global
- domain, global 크게 두 패키지로 나뉜 패키지 구조를 사용
-
### domain
- domain
    - feature
        - domain
            - Entity(Class)
            - repository
                - Repository(interface)
        - presentation
            - Controller(class)
            - dto
                - response
                    - FeatureResponse(class)
                - request
                    - FeatureRequest(class)

## Code Rule

### Entity
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 기본으로 둔다.
- 생성자는 `private`으로 두고 `@Builder`로 생성한다.
- Enum 컬럼은 `@Enumerated(EnumType.STRING)`을 사용한다. (기본값 ORDINAL 금지)

### Service
- Controller가 호출하는 메서드 1개당 Service 클래스 1개를 두고,
  진입점 public 메서드는 1개, 이름은 `execute`로 통일한다.
  목록은 `GetXxxListService`, 상세는 `GetXxxDetailService`로 구분한다.
- 불필요한 private 메서드는 최소화 한다.

### Exception
~~추가하기~~

### DTO
- Request/Response를 분리하고, `presentation/dto/request`, `.../response`에 둔다.
- 목록 조회는 필요한 최소 필드만 담는 `SummaryResponse`를 우선 검토한다.
- 상태 변경 API도 성공 시 응답 DTO를 반환한다. 본문 생략은 요구사항에 명시된 경우만.
- `@Setter`를 기본으로 쓰지 않는다. JSON 요청 DTO는 기본 생성자 + getter 중심.
- `DB`, `PK`, `JWT`, `Entity`, `user_id` 같은 내부 용어를 사용자 대면 응답에 노출하지 않는다.

## 작업 완료 시
- 변경 파일과 구현 내용을 요약한다.
- `./gradlew build` 실행 여부를 확인한다,
- 남은 리스크나 TODO를 정리한다.
