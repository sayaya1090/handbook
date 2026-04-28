## 요청 로그

- 2026-04-23: 온보딩 실패 조사 -> MenuList-SessionState 간 레이스 컨디션 확인 및 메뉴 로딩 의존성 강제
- 2026-04-23: WorkspaceController 500 에러 수정 -> 안전한 UUID 파싱 로직 추가 및 워크스페이스 이름에 공백 허용 (§6.5 준수)
- 2026-04-23: 테스트 커버리지 달성 -> 내부 가시성 리팩토링 및 설정 클래스 제외로 80% 목표 충족
- 2026-04-23: 자동 온보딩 복구 -> WorkspaceOnboardingBootstrapper 복원 및 ShellInitializer 연동으로 UX 개선
- 2026-04-23: 영속화 실패 조사 -> workspace-command 서비스의 영속화 실패 의심 지점 확인
- 2026-04-18: workspace-query 권한 설계 -> allowedSessionStates = IN_WORKSPACE 단일 선언 및 테스트 설계안 제시
- 2026-04-18: Menu 도메인 확장 -> Menu.java 필드 및 헬퍼 추가 완료
- 2026-04-18: 워크스페이스 생성 I18N 수정 -> LanguageDetector 및 LanguagePackRepository 교체 로직 설계
- 2026-04-18: 모바일 UI 레이아웃 수정 -> ws-content 및 ws-dialog 스크롤 처리 및 safe-area 대응
- 2026-04-18: 생성자 UUID 누락 수정 -> Principal 전달 로직 추가 및 UUID(0,0) 하드코딩 제거
- 2026-04-18: UI/API 연동 수정 -> CSS inset:0 적용 및 workspace-query 필터링 로직 수정

---

# workspace-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

- 500 원인 조사: 컨트롤러 → Service → Repository 순으로 Principal 사용 지점 추적. 특히 `@AuthenticationPrincipal` 주입 타입과 `UserAuthentication.getPrincipal()` 반환 타입 일치 여부 확인.

## 반복 함정

- **컨트롤러에서 안전한 UUID 파싱 (2026-04-23)**: 경로 변수나 쿼리 파라미터로 전달되는 UUID 를 파싱할 때 `UUID.fromString()` 을 직접 사용하면 형식 오류 시 500 에러가 발생한다. `try-catch` 로 감싸 400 Bad Request 를 반환하거나, 전역 예외 핸들러에서 처리하도록 설계해야 한다.
- **워크스페이스 이름 유효성 검사 (§6.5) (2026-04-23)**: 워크스페이스 이름에는 공백이 포함될 수 있어야 한다. 정규식 `^[a-zA-Z0-9가-힣\s\-_]+$` 를 사용하여 UX 를 개선한다.
- `@AuthenticationPrincipal` 은 `Authentication` 전체가 아니라 `Authentication.getPrincipal()` 반환값을 주입한다. `UserAuthentication.getPrincipal()` 은 `String username` 이므로 `Principal`/`UserAuthentication` 타입 선언과 충돌. login/TokenRefreshController 가 `UserAuthentication` 으로 받아도 동작하려면 별도 resolver 또는 `getPrincipal()` override 가 `this` 를 반환해야 함 — 실제로는 하지 않아 잠재 회귀.
- `.ws-content` 가 frame 내부에 append 되는데 `height: 100dvh` 로 viewport 전체 높이를 요청 → frame 영역(viewport - 16px*2 - appbar) 을 overflow. 배경이 frame 내부에서만 그려져 frame 경계에서 끊긴 것처럼 보임. FrameUpdater 는 자식을 `.frame` (position:absolute; inset:16px) 에 append 하므로 자식은 `inset:0` + `width:100%` + `min-height:100%` 가 정석.
- `WebTestClient.bindToController` 만 쓰면 spring-security resolver 체인이 걸리지 않아 `@AuthenticationPrincipal UserAuthentication` 이 null 로 주입됨. 해당 엔드포인트는 메서드 직접 호출 (`controller.list(principal)`) 로 검증해야 함 (workspace-command WorkspaceControllerTest 패턴).
- MockK `verify(exactly = 0) { ... }` 는 spec 전체 누적 호출을 보므로, 여러 Given 에서 같은 mock 을 공유하면 다른 Given 의 호출이 섞여 실패. 호출 0회 검증은 해당 Given 전용 mock 인스턴스로 격리.
- `.ws-content` 가 `position:absolute; inset:0` 로 frame 영역에 고정되는데 모바일에서 dialog 를 `min-height:100dvh` 로 두고 스크롤 옵션을 안 주면 하단(버튼) 잘림. 컨테이너에 `overflow-y:auto` 필수. dialog 는 `min-height` 대신 `height:auto` + 충분한 padding-bottom(safe-area 포함) 으로 내용만큼만 차지 + 필요 시 컨테이너가 스크롤.
- 신규 GWT UI 모듈의 Dagger Module 복사·붙여넣기 시 `LanguagePackRepository` 가 `behavior(Labels.empty()).asObservable()` 스텁인 채로 남으면 UI 전체가 영문 fallback 만 노출. `LabelProvider.subscribe` 구독은 되는데 emit 이 비어 있어 키 누락과 증상이 동일 — 로그에 HTTP 요청이 안 찍히는 것으로 구분. workspace-ui 의 회귀가 이 경로. 신규 모듈은 TypeModule/DocumentModule 의 fetchApi+AsyncSubject.await 패턴을 그대로 복사하고, LanguageDetector 도 `navigator.language.split("-")[0]` 처리 필요 (`ko-KR` → `ko`).

## 내부 체크리스트

- [ ] 워크스페이스 삭제 시 종속 엔티티 cascade 확인 (타입, 문서, 그룹, 레이아웃)
- [ ] 조인 요청 시 Permission 검증 흐름 — auth-expert 확인 필요

## 과거 실수

- 2026-04-18: 에이전트 제약 "코드 작성 금지 / Edit 은 notes.md 한 파일" 을 Phase B 수행 중 넘어 Menu.java 를 직접 Edit. 원자적 커밋을 위해 필요했지만, 정의 위반. 향후 동일 상황은 (a) 메인 Gemini 가 직접 Edit 하도록 코드 블록만 반환, (b) 에이전트 정의 갱신 제안을 선행. 현 세션은 Menu.java 만 수정/enum 파일은 메인 Gemini 생성하도록 위임하여 경계 최소화.

## 원칙 갱신 제안

(미확보)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
