## 요청 로그

- 2026-05-15: 워크스페이스 URL 연동 구현 → PlaceholderResolver/SessionContext 도입
- 2026-04-18: 메뉴 클릭 URL 동기화 -> MenuSelected-HistoryManager 양방향 동기화 설계 확인
- 2026-04-23: 온보딩 레이스 컨디션 수정 -> 메뉴 로딩이 워크스페이스 체크보다 선행되도록 보장
- 2026-04-23: GWT 모듈 레이지 로딩 트리거 개선 -> Onboarding Bootstrapper 에서 window.location.hash 를 사용하여 workspace-ui 모듈이 정상적으로 로드되도록 수정
- 2026-04-23: UI 정렬 수정 및 온보딩 복원 -> .shell-app-bar CSS 수정, Bootstrapper 복원, 메뉴 호버 방어 로직 추가
- 2026-04-21: GWT 빌드 안정화 -> GraalVM 25 + proxy.crt 환경 전수 테스트 통과 확인 및 포트 충돌 자바 프로세스 정리 가이드 추가

---

# ui-platform-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

- **URL 정규식 매칭 전 예약어 치환 (2026-05-15)**: `UrlBasedMenuResolver` 등에서 메뉴 활성화 여부를 판단할 때, `menu.urlRegex` 를 그대로 쓰지 말고 `PlaceholderResolver` 를 통해 `{workspaceId}` 등 동적 세그먼트를 현재 컨텍스트 값으로 치환한 후 매칭해야 한다. 치환되지 않은 정규식은 가변 ID 경로(예: `/w/123/dashboard`)에서 항상 매칭 실패를 유발한다.

## 반복 함정

- **SessionContext 를 통한 반응형 컨텍스트 관리 (2026-05-15)**: 워크스페이스 전환처럼 전역 상태가 변경될 때 UI 가 즉각 반응해야 하는 경우, 단순 싱글톤 필드가 아닌 `SessionContext` (Observable 모델)를 사용한다. `UrlBasedMenuResolver` 는 이 컨텍스트를 구독하여 URL 변화뿐 아니라 "세션 상태 변화(예: 워크스페이스 ID 확정)" 시점에도 메뉴 선택 상태를 재계산함으로써 레이스 컨디션을 방지한다.
- **PlaceholderResolver 와 {workspaceId} 예약어 규약 (2026-05-15)**: 프레임워크 수준에서 `{workspaceId}` 는 현재 선택된 워크스페이스의 고유 식별자로 규약한다. 메뉴 정의(`menus.md`)의 URL 패턴에 이 예약어를 사용하면, `PlaceholderResolver` 가 `SessionContext` 의 현재 값을 주입하여 런타임 URL 을 생성한다. 신규 예약어 추가 시 `PlaceholderResolver` 에 치환 로직을 반드시 포함해야 한다.
- **비활성화된 메뉴의 호버 peek 차단 (2026-04-23)**: `MenuRailItemElement` 가 `[disabled]` 상태일 때도 `mouseover` 이벤트가 `MenuHover` 를 발행하면, 툴레일이 열려 비정상적인 UI 전환이 발생한다. 이벤트 핸들러 최상단에서 `element().hasAttribute("disabled")` 가드 필수.
- **Drawer 에서 "rail 상태와 무관하게 항상 보여야 하는 컨트롤" 은 rail 의 자식이 아니라 drawer 직속에 mount** — rail 에 `[hide]` (width:0 + overflow:hidden) 가 걸리면 자식도 함께 잘린다. 햄버거처럼 "menu-rail HIDE 여도 drawer 가 visible 인 모든 순간에는 노출" 이 요구사항이면 `.drawer > .body` 의 sibling (drawer flex-column 의 첫 자식) 으로 두고 rail 만 flex:1 min-height:0 으로 둔다. rail 상단 mount 는 "rail 이 보이는 동안만" 의 세트에 해당. (2026-04-18 C)
- **`@JsOverlay` 재귀 호출 금지** — GWT ReferenceError. static 헬퍼로 우회.
- **`backdrop-filter` 가 fixed 자손의 containing block** — 모바일 하단 네비 width=0 증상.
- **하드코딩 금지** — MD3 토큰만 사용 (GEMINI.md).
- **AppBar `left: var(--shell-drawer-width)` + 좌측 slot mount 조합 주의** — AppBar 가 drawer 우측부터 시작하는 MD3 Top App Bar + Navigation Rail 병치 디자인에서, AppBar leading slot 에 mount 된 요소는 drawer 상태에 따라 viewport 좌표가 같이 이동. drawer toggle 처럼 "drawer 에 소속된 것처럼" 보여야 하는 요소는 AppBar leading 금지, Rail 상단 mount 가 정답 (MD3 Navigation Rail 정석). (2026-04-18 햄버거 회귀)
- **부분 봉합 회귀 주의** — 모바일 한정 `display:none` 으로 증상 봉합 시 데스크톱 경로는 그대로 남음. 증상이 "뷰포트 한쪽에서만 안 보인다" 인지 "양쪽 다 고친 거" 인지 커밋 메시지에 명시 (3440cfe 의 "모바일 햄버거 숨김" 이 사용자에겐 "햄버거 위치 정리" 로 인식된 사례).
- **Rail collapse/expand 는 "폭만 변하고 아이콘 x 좌표는 고정"** 이 MD3 정석. `.item .collapse`(margin 8px, 40px 버튼) 과 `md-item` slot=start(padding-inline-start 16px, 24px 아이콘) 의 icon center 가 둘 다 x=28px 에 맞도록 설계됨. `.rail[expand]` 에 `padding-left` / `align-items:flex-start` 류의 오프셋을 추가하면 이 정렬이 깨져 아이콘이 전환 시 시프트된다. 햄버거도 `align-self:center` 대신 `align-self:flex-start + margin-left:8px` 로 icon center=28px 고정 필요. (2026-04-18)
- **`visibility:hidden` + `position:absolute` 로 숨기면 여전히 (0,0) 에 배치되어 "딱 붙어 보인다"** (2026-04-18). `.rail[expand] .item .collapse` 를 `position:absolute; visibility:hidden` 로 숨겼더니 사용자 눈에 "SVG 아이콘이 탑/레프트 마진 없이 딱 붙어 있다"고 인식됨. 원인: position:absolute 는 containing block (`.item` = position:relative) 의 top-left (0,0) 에 그대로 배치되고, visibility:hidden 은 *색 없음* 정도만 적용되므로 버튼 host 영역 자체가 (0,0)-size40px 에 클릭/호버 영역까지 실재. shadow DOM 내부 state layer · focus ring 이 잔상으로 비칠 수도 있음. **해결: `display: none` 이 유일하게 안전**. 모바일 override (`.rail[mobile][expand] .item .collapse`) 에는 반드시 `display: flex` 를 명시해 데스크톱 규칙을 되돌린다. `visibility` 는 "레이아웃은 유지하면서 색만 숨김" 이라 이 맥락에선 적합하지 않다.
- **자동 온보딩 UX 보존 (2026-04-23)**: `allowedSessionStates` 기반의 명시적 CTA (클릭 유도) 가 도입되더라도, 워크스페이스가 전혀 없는 신규 사용자를 위한 `WorkspaceOnboardingBootstrapper` 의 자동 진입 로직은 유지하는 것이 UX 측면에서 유리하다. "빈 화면에서 클릭 유도" 보다 "즉시 생성 화면 노출" 이 마찰이 적음.
- **GWT 레이지 로딩 모듈 트리거 시 window.location.hash 사용 (2026-04-23)**: OnboardingBootstrapper 에서 GWT `History.newItem` 대신 `window.location.hash = "#workspace/create"` 를 직접 사용해야 한다. `History.newItem` 은 현재 상태와 동일한 토큰일 경우 `ValueChangeEvent` 를 발행하지 않아, 모듈 주입 후 초기 화면 진입이 누락될 수 있다.
- **outline/filled 아이콘 absolute inset:0 오버랩 금지 (2026-04-18 철회)**. <i> 의 positioning context 는 light DOM nearest positioned ancestor — `md-icon-button` host 의 shadow 내부 `.touch-target` 이나 `md-item` 의 shadow 내부 `.list-item` 은 light DOM <i> 의 containing block 이 **되지 않는다**. 실제로는 `.rail .item` (position:relative) 이 우선 매치 → `inset:0` 이 rail item 전체(16rem×64px) 로 아이콘을 늘려 expand 모드에서 레이블과 겹침. `md-item[slot=start]` 안의 light DOM 자식도 slot 의 `<slot>` element 가 아니라 host 의 light DOM tree 로 이어지므로 동일. 해결: `display:none` 토글이 정석. 1px 시프트는 수용 가능한 절충 — rail item 전체 붕괴보다 훨씬 낮은 리스크. 대안으로 wrapper `<span class="rail-icon-stack">` 를 IconElementBuilder 레벨에서 삽입할 수 있으나 여러 호출 지점(MenuRailItem/ToolRailItem/MenuTabBuilder) 전수 수정 필요하므로 별도 과제로 분리.


## 내부 체크리스트

- [ ] 새 토큰 추가 시 → `docs/contracts/design-tokens.md` + global.css 라이트/다크 양쪽
- [ ] 새 메뉴 항목 추가 시 → urlRegex 매칭 로직 확인
- [ ] 모바일 레이아웃 변경 시 → 48dp 터치 타겟 + Safe Area

## 과거 실수

- **Write 툴 없는 서브에이전트 환경 인식 미흡** (2026-04-18) — 이슈 C 는 신규 파일 2개(WorkspaceRepository.java, WorkspaceApi.java) 생성 필요였으나 이 에이전트는 Edit/Read/Grep/Glob 만 보유 → 신규 파일 생성 불가. 설계 단계에서 "신규 파일이 포함되는 작업" 인지 먼저 확인하고, 필요하면 메인 Gemini 에 위임했어야 함. 부분 적용(User.workspaces 만 제거) 은 빌드 깨짐 → 원자성 유지 위해 이슈 C 전체 미착수.

## 원칙 갱신 제안

- **Drawer 에 소속된 것처럼 보여야 하는 컨트롤은 AppBar 가 아니라 Drawer/Rail 에 mount** — AppBar 가 drawer-width 오프셋을 받는 병치 레이아웃에서 mount slot 선택이 시각 위치를 좌우. 일반 규칙으로 승격 검토.

## 아카이브 요약

- 2026-04-18: Phase C Shell UI 고도화 — 햄버거 위치 Drawer 이관, 메뉴 아이콘 가시성 제어(display:none), SessionStateProvider observable 전환 등 레이아웃·상태 동기화 이슈 해결

---

마지막 감사: — (신규)
