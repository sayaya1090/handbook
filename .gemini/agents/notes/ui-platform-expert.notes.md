## 요청 로그

- 2026-04-27: 동적 툴 프로바이더 설계 및 구현 제안 → agent-bridge/activity 연동 설계 및 코드(ToolProvider, Bridges) 생성
- 2026-04-27: 반응형 데이터 흐름 통제(Unidirectional Data Flow) → LayoutProvider, LayoutList 캡슐화 완료 (Scope 4)
- 2026-04-27: 에이전트 커맨드 파싱 최적화 → AgentMutationHandler에 Strategy 패턴 도입 및 테스트 갱신 (Scope 3)
- 2026-04-27: 캔버스 모드 분기문 제거 → State 패턴 도입 및 Canvas/TypeElement 리팩토링 완료 (Scope 2)
- 2026-04-27: 생성자 과잉 주입 해결 → StatusHeaderElement 및 ShellInitializer 에 중첩 Context 클래스 도입 (Scope 1)
- 2026-04-27: 타입 편집기 UI 재설계 Phase 2 → DrawerTest 상단 상태바 검증 및 ToolTest 신설
- 2026-04-27: 온보딩 풀스크린 및 심리스 전환 구현 → data-onboarding 기반 Shell UI 가리기 및 WindowUriBridge.navigate 연동 추가
- 2026-04-27: 워크스페이스 URL 연동 구현 → PlaceholderResolver/SessionContext 도입
- 2026-04-23: 온보딩 레이스 컨디션 수정 -> 메뉴 로딩이 워크스페이스 체크보다 선행되도록 보장
- 2026-04-23: GWT 모듈 레이지 로딩 트리거 개선 -> Onboarding Bootstrapper 에서 window.location.hash 를 사용하여 workspace-ui 모듈이 정상적으로 로드되도록 수정
- 2026-04-23: UI 정렬 수정 및 온보딩 복원 -> .shell-app-bar CSS 수정, Bootstrapper 복원, 메뉴 호버 방어 로직 추가
- 2026-04-21: GWT 빌드 안정화 -> GraalVM 25 + proxy.crt 환경 전수 테스트 통과 확인 및 포트 충돌 자바 프로세스 정리 가이드 추가

---

# ui-platform-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

- **Host-Child 브릿지 분리 (Publisher/Subscriber) (2026-04-27)**: 쉘(Host)과 개별 모듈(Child) 간의 통신에서, 양측의 역할이 명확히 구분될 경우 `WindowXxxBridge` 를 `Publisher` 와 `Subscriber` 로 나누어 설계한다. Publisher 는 Child 모듈에서 `window` 객체에 상태를 쓰고 이벤트를 발행하며, Subscriber 는 Host 에서 이를 구독하여 반응한다.
- **GWT 테스트 포트 격리 (2026-04-27)**: 여러 GWT 모듈을 동시에 테스트할 때 `webPort` 충돌(예: 18086)이 발생하므로 모듈별로 고유 포트를 할당해야 함.
- **URL 정규식 매칭 전 예약어 치환 (2026-04-27)**: `UrlBasedMenuResolver` 등에서 메뉴 활성화 여부를 판단할 때, `menu.urlRegex` 를 그대로 쓰지 말고 `PlaceholderResolver` 를 통해 `{workspaceId}` 등 동적 세그먼트를 현재 컨텍스트 값으로 치환한 후 매칭해야 한다.

## 반복 함정

- **SessionContext 를 통한 반응형 컨텍스트 관리 (2026-04-27)**: 워크스페이스 전환처럼 전역 상태가 변경될 때 UI 가 즉각 반응해야 하는 경우, 단순 싱글톤 필드가 아닌 `SessionContext` (Observable 모델)를 사용한다.
- **PlaceholderResolver 와 {workspaceId} 예약어 규약 (2026-04-27)**: 프레임워크 수준에서 `{workspaceId}` 는 현재 선택된 워크스페이스의 고유 식별자로 규약한다.
- **비활성화된 메뉴의 호버 peek 차단 (2026-04-23)**: `MenuRailItemElement` 가 `[disabled]` 상태일 때도 `mouseover` 이벤트가 `MenuHover` 를 발행하면, 툴레일이 열리는 문제 가드 필수.
- **Drawer 에서 "rail 상태와 무관하게 항상 보여야 하는 컨트롤" 은 rail 의 자식이 아니라 drawer 직속에 mount** (2026-04-23)
- **AppBar `left: var(--shell-drawer-width)` + 좌측 slot mount 조합 주의** (2026-04-18)
- **Rail collapse/expand 는 "폭만 변하고 아이콘 x 좌표는 고정"** (2026-04-18)
- **`visibility:hidden` + `position:absolute` 오작동 → `display:none` 사용 권장** (2026-04-18)
- **자동 온보딩 UX 보존 (2026-04-23)**: 워크스페이스가 없는 신규 사용자를 위한 자동 진입 로직 유지.
- **GWT 레이지 로딩 모듈 트리거 시 window.location.hash 사용 (2026-04-23)**: History.newItem 대신 hash 직접 조작으로 이벤트 누락 방지.

## 내부 체크리스트

- [ ] 새 토큰 추가 시 → `docs/contracts/design-tokens.md` + global.css 라이트/다크 양쪽
- [ ] 새 메뉴 항목 추가 시 → urlRegex 매칭 로직 확인
- [ ] 모바일 레이아웃 변경 시 → 48dp 터치 타겟 + Safe Area

## 과거 실수

- **.gitignore 규칙 순서 오류 (2026-04-27)**: `**/src/test/webapp/*/` 와 같은 광범위한 제외 규칙이 하위의 `!**/src/test/webapp/js/` 포함 규칙보다 뒤에 오거나, 포함 규칙에 `!`가 누락되어 필수 테스트 자산이 커밋에서 제외됨.
  - *해결*: 제외 규칙을 먼저 선언하고, 유지해야 할 폴더/파일은 `!` 접두어를 사용하여 하단에 명시할 것.
- **테스트 HTML 필수 자산 누락 (2026-04-27)**: 신규 모듈 분리 시 RxJS, FontAwesome 등 라이브러리 임포트 누락으로 테스트 실패.
  - *해결*: 표준 테스트 템플릿 사용을 강제하고, `build.gradle.kts`에서 `gwtTestCompile` 태스크가 자산 복사 태스크에 의존하도록 설정함.
- **모듈 분리 시 배포 인프라 누락 (2026-04-27)**: 로직만 분리하고 Helm Chart, Kargo Warehouse 설정을 누락하여 배포 파이프라인 중단.
  - *해결*: 신규 모듈 생성 시 `charts/handbook/<module>` 생성 및 `values.yaml` 서비스 목록 추가를 필수 절차로 편입.
- **삭제된 테스트 소스 HTML 과잉 삭제 (2026-04-27)**: 빌드 생성 파일 삭제 시 수동 관리 자산까지 삭제. (복구 완료)
- **Write 툴 없는 서브에이전트 환경 인식 미흡** (2026-04-18)

## 아카이브 요약

- 2026-04-18: Phase C Shell UI 고도화 — 햄버거 위치 Drawer 이관, 메뉴 아이콘 가시성 제어 등 해결
