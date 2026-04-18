# ui-platform-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: 실제 Edit 수행 — A(햄버거 drawer 직속)·B(.workspace max-width 24rem) 완료, C(WorkspaceRepository) 는 Write 툴 부재로 신규 파일 생성 불가 → 미착수. 이슈 A 는 DrawerElement/MenuRailElement/shell.css(main+test)/DrawerTest.kt 모두 동기화, 회귀 가드(rail[hide]에서 햄버거 visible) 신설
- 2026-04-18: 직전 세션 설계만 반환 누락 → 이번엔 실제 파일 Edit 수행. 3건(A: 햄버거 drawer 직속, B: workspace max-width 24rem, C: WorkspaceRepository 신설 + User.workspaces 제거) 전부 main/test 경로 동기 적용
- 2026-04-18: 3건 병렬 (햄버거 rail-hide 가시성 + WS 드롭다운 폭 + User.workspaces 제거) → 햄버거 DOM 을 drawer 직속 (drawer > .body 앞) 으로 이관해 menu-rail[hide] 와 독립 + .workspace max-width 16→22rem / `.shell-app-bar-center` gap 정리 + `WorkspaceListRepository` 신설 (search-workspace `/workspaces` 구독) + `User.workspaces` 제거
- 2026-04-18: expand 에서 .collapse 가 좌상단 노출 + 햄버거 마진 없음 지적 → .rail[expand] .item .collapse 를 display:none 으로 (visibility:hidden + absolute 는 position:absolute 가 흐름에서 빼내 (0,0) 에 잔류 렌더), 모바일 override 도 display 키 추가
- 2026-04-18: 메뉴 아이콘 absolute inset:0 → display 토글 롤백 → inset:0 이 md-item 전체(16rem×48px) 로 늘어나 레이블과 겹침 확인, (A) 원복 채택 / 햄버거 flex-start 유지
- 2026-04-18: outline/filled 아이콘 display:none → opacity+visibility 크로스페이드 전환 → main/test shell.css 동기화 + DrawerTest display → visibility 어설션 업데이트, 방식 변형 C (absolute inset:0 스택)
- 2026-04-18: 햄버거 고정 재확인 + 메뉴 아이콘 2개 오버랩 → shell.css 에 `align-self:flex-start + margin-left:8px` 적용 확인, md-item start slot 단일 아이콘 구조 확인 (outlined/filled 2-icon 없음)
- 2026-04-18: MenuRail collapse/expand 아이콘 시프트 → `.rail[expand] padding 0 0.6rem + align-items:flex-start` 제거 + 햄버거 `align-self:center → flex-start + margin-left:8px` 로 icon center x=28px 고정
- 2026-04-18: 햄버거 드로어 우측 밀림 회귀 진단 → AppBar leading mount + AppBar `left:var(--drawer-width)` 조합 원인, MenuRail 상단 이관 권고 (사용자 승인 후 수정 완료)

## 탐색 패턴

(미확보)

## 반복 함정

- **Drawer 에서 "rail 상태와 무관하게 항상 보여야 하는 컨트롤" 은 rail 의 자식이 아니라 drawer 직속에 mount** — rail 에 `[hide]` (width:0 + overflow:hidden) 가 걸리면 자식도 함께 잘린다. 햄버거처럼 "menu-rail HIDE 여도 drawer 가 visible 인 모든 순간에는 노출" 이 요구사항이면 `.drawer > .body` 의 sibling (drawer flex-column 의 첫 자식) 으로 두고 rail 만 flex:1 min-height:0 으로 둔다. rail 상단 mount 는 "rail 이 보이는 동안만" 의 세트에 해당. (2026-04-18 C)
- **`@JsOverlay` 재귀 호출 금지** — GWT ReferenceError. static 헬퍼로 우회.
- **`backdrop-filter` 가 fixed 자손의 containing block** — 모바일 하단 네비 width=0 증상.
- **하드코딩 금지** — MD3 토큰만 사용 (CLAUDE.md).
- **AppBar `left: var(--shell-drawer-width)` + 좌측 slot mount 조합 주의** — AppBar 가 drawer 우측부터 시작하는 MD3 Top App Bar + Navigation Rail 병치 디자인에서, AppBar leading slot 에 mount 된 요소는 drawer 상태에 따라 viewport 좌표가 같이 이동. drawer toggle 처럼 "drawer 에 소속된 것처럼" 보여야 하는 요소는 AppBar leading 금지, Rail 상단 mount 가 정답 (MD3 Navigation Rail 정석). (2026-04-18 햄버거 회귀)
- **부분 봉합 회귀 주의** — 모바일 한정 `display:none` 으로 증상 봉합 시 데스크톱 경로는 그대로 남음. 증상이 "뷰포트 한쪽에서만 안 보인다" 인지 "양쪽 다 고친 거" 인지 커밋 메시지에 명시 (3440cfe 의 "모바일 햄버거 숨김" 이 사용자에겐 "햄버거 위치 정리" 로 인식된 사례).
- **Rail collapse/expand 는 "폭만 변하고 아이콘 x 좌표는 고정"** 이 MD3 정석. `.item .collapse`(margin 8px, 40px 버튼) 과 `md-item` slot=start(padding-inline-start 16px, 24px 아이콘) 의 icon center 가 둘 다 x=28px 에 맞도록 설계됨. `.rail[expand]` 에 `padding-left` / `align-items:flex-start` 류의 오프셋을 추가하면 이 정렬이 깨져 아이콘이 전환 시 시프트된다. 햄버거도 `align-self:center` 대신 `align-self:flex-start + margin-left:8px` 로 icon center=28px 고정 필요. (2026-04-18)
- **`visibility:hidden` + `position:absolute` 로 숨기면 여전히 (0,0) 에 배치되어 "딱 붙어 보인다"** (2026-04-18). `.rail[expand] .item .collapse` 를 `position:absolute; visibility:hidden` 로 숨겼더니 사용자 눈에 "SVG 아이콘이 탑/레프트 마진 없이 딱 붙어 있다"고 인식됨. 원인: position:absolute 는 containing block (`.item` = position:relative) 의 top-left (0,0) 에 그대로 배치되고, visibility:hidden 은 *색 없음* 정도만 적용되므로 버튼 host 영역 자체가 (0,0)-size40px 에 클릭/호버 영역까지 실재. shadow DOM 내부 state layer · focus ring 이 잔상으로 비칠 수도 있음. **해결: `display: none` 이 유일하게 안전**. 모바일 override (`.rail[mobile][expand] .item .collapse`) 에는 반드시 `display: flex` 를 명시해 데스크톱 규칙을 되돌린다. `visibility` 는 "레이아웃은 유지하면서 색만 숨김" 이라 이 맥락에선 적합하지 않다.
- **outline/filled 아이콘 absolute inset:0 오버랩 금지 (2026-04-18 철회)**. `<i>` 의 positioning context 는 light DOM nearest positioned ancestor — `md-icon-button` host 의 shadow 내부 `.touch-target` 이나 `md-item` 의 shadow 내부 `.list-item` 은 light DOM `<i>` 의 containing block 이 **되지 않는다**. 실제로는 `.rail .item` (position:relative) 이 우선 매치 → `inset:0` 이 rail item 전체(16rem×64px) 로 아이콘을 늘려 expand 모드에서 레이블과 겹침. `md-item[slot=start]` 안의 light DOM 자식도 slot 의 `<slot>` element 가 아니라 host 의 light DOM tree 로 이어지므로 동일. 해결: `display:none` 토글이 정석. 1px 시프트는 수용 가능한 절충 — rail item 전체 붕괴보다 훨씬 낮은 리스크. 대안으로 wrapper `<span class="rail-icon-stack">` 를 IconElementBuilder 레벨에서 삽입할 수 있으나 여러 호출 지점(MenuRailItem/ToolRailItem/MenuTabBuilder) 전수 수정 필요하므로 별도 과제로 분리.

## 내부 체크리스트

- [ ] 새 토큰 추가 시 → `docs/contracts/design-tokens.md` + global.css 라이트/다크 양쪽
- [ ] 새 메뉴 항목 추가 시 → urlRegex 매칭 로직 확인
- [ ] 모바일 레이아웃 변경 시 → 48dp 터치 타겟 + Safe Area

## 과거 실수

- **Write 툴 없는 서브에이전트 환경 인식 미흡** (2026-04-18) — 이슈 C 는 신규 파일 2개(WorkspaceRepository.java, WorkspaceApi.java) 생성 필요였으나 이 에이전트는 Edit/Read/Grep/Glob 만 보유 → 신규 파일 생성 불가. 설계 단계에서 "신규 파일이 포함되는 작업" 인지 먼저 확인하고, 필요하면 메인 Claude 에 위임했어야 함. 부분 적용(User.workspaces 만 제거) 은 빌드 깨짐 → 원자성 유지 위해 이슈 C 전체 미착수.

## 원칙 갱신 제안

- **Drawer 에 소속된 것처럼 보여야 하는 컨트롤은 AppBar 가 아니라 Drawer/Rail 에 mount** — AppBar 가 drawer-width 오프셋을 받는 병치 레이아웃에서 mount slot 선택이 시각 위치를 좌우. 일반 규칙으로 승격 검토.

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
