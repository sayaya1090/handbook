# workspace-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: 워크스페이스 생성 UI 리디자인 → handbook-old JoinApplication 기반 stepper/list 이식
- 2026-04-18: POST /workspace 500 조사 → @AuthenticationPrincipal Principal 타입 바인딩 실패 의심 (UserAuthentication.getPrincipal()=String)

## 탐색 패턴

- 500 원인 조사: 컨트롤러 → Service → Repository 순으로 Principal 사용 지점 추적. 특히 `@AuthenticationPrincipal` 주입 타입과 `UserAuthentication.getPrincipal()` 반환 타입 일치 여부 확인.

## 반복 함정

- `@AuthenticationPrincipal` 은 `Authentication` 전체가 아니라 `Authentication.getPrincipal()` 반환값을 주입한다. `UserAuthentication.getPrincipal()` 은 `String username` 이므로 `Principal`/`UserAuthentication` 타입 선언과 충돌. login/TokenRefreshController 가 `UserAuthentication` 으로 받아도 동작하려면 별도 resolver 또는 `getPrincipal()` override 가 `this` 를 반환해야 함 — 실제로는 하지 않아 잠재 회귀.

## 내부 체크리스트

- [ ] 워크스페이스 삭제 시 종속 엔티티 cascade 확인 (타입, 문서, 그룹, 레이아웃)
- [ ] 조인 요청 시 Permission 검증 흐름 — auth-expert 확인 필요

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
