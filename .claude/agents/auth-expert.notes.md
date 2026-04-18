# auth-expert Operational Notes

에이전트 자신이 갱신하는 업무 노트. 작업 패턴·반복 함정·내부 체크리스트.
이 파일은 에이전트가 직접 편집한다. 정의 파일(`auth-expert.md`) 은 건드리지 않는다.

도메인 사실은 여기가 아니라 정식 문서(요구사항/계약) 로.

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: GET /user 에 workspace 없음 → UserController 는 JWT claim 만 반환, workspace 조회 미구현 (가설 A).
- 2026-04-18: POST /workspace 500 원인 → @AuthenticationPrincipal 는 getPrincipal() 값 주입, 현재 String username → 어댑터 `is UserAuthentication` 분기 영구 miss.

## 탐색 패턴

(미확보)

## 반복 함정

- `@AuthenticationPrincipal` 의 주입 대상은 `Authentication` 자체가 아닌 `Authentication.getPrincipal()` 반환값. `UserAuthentication.getPrincipal()` 이 `String username` 을 반환하면 컨트롤러에서 `UserAuthentication`/`Principal` 타입으로 선언해도 실제 주입 타입은 String → 런타임 ClassCastException 또는 분기 miss. 컨벤션: `getPrincipal() = this` 로 바꾸거나 컨트롤러 선언을 `String` 으로 통일.

## 내부 체크리스트

- [ ] Permission 추가 시 → `docs/contracts/permissions.md` 갱신 + 모든 persist-*/search-* 영향도 보고
- [ ] JWT claim 구조 변경 시 → authentication 라이브러리 검증 로직 + 모든 소비자
- [ ] PAT 신규 발급 UI 필요 시 → shell-ui 사용자 설정 영역 (§6.8) 과 조율

## 과거 실수

(미확보)

## 원칙 갱신 제안 (메인 Claude 감사 대상)

(미확보)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규 생성, 감사 전)
