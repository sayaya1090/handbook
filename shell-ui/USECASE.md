# Shell-UI 유스케이스

## 초기 로딩 → 메뉴 선택 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant App as Application
    participant UA as UserApi
    participant UP as UserProvider
    participant ML as MenuList
    participant MA as MenuApi
    participant UR as UrlBasedMenuResolver
    participant MS as MenuSelected
    participant MSM as ModuleScriptManager

    App->>UA: find()
    UA->>UA: GET /user
    UA-->>UP: User 발행
    UP-->>ML: 사용자 변경 감지
    ML->>MA: findAll()
    MA->>MA: GET /menus
    MA-->>ML: Menu[] 발행
    UR->>UR: 현재 URL과 메뉴 urlRegex 매칭
    UR->>MS: 매칭된 메뉴 선택
    MS-->>MSM: 메뉴 변경 감지
    MSM->>MSM: <script> 동적 주입
    Note over MSM: type/type.nocache.js 등
```

## 메뉴 클릭 → 모듈 로딩 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant MR as MenuRailElement
    participant MS as MenuSelected
    participant TS as ToolSelected
    participant TEM as ToolExecutionManager
    participant MSM as ModuleScriptManager
    participant HM as HistoryManager
    participant DM as DrawerMode

    User->>MR: 메뉴 아이템 클릭
    MR->>MS: next(menu)
    alt 도구 1개
        MS->>TS: 자동 선택
        TS->>TEM: exec()
        TEM->>TEM: DOM 준비 대기 (100ms 재시도)
    else 도구 여러 개
        MS-->>MR: Tool Rail EXPAND
    end
    MS-->>MSM: 스크립트 주입
    MS-->>HM: pushState(url)
    MS-->>DM: COLLAPSE
```

## 에이전트 화면 이동 시퀀스

```mermaid
sequenceDiagram
    participant Agent as 에이전트 (SSE)
    participant NH as NavigateHandler
    participant URI as Observer<URI>
    participant UR as UrlBasedMenuResolver
    participant MS as MenuSelected
    participant MSM as ModuleScriptManager

    Agent->>NH: NavigateCommand (menu, url)
    NH->>URI: URL 발행
    URI-->>UR: URL 변경 감지
    UR->>UR: urlRegex 매칭
    UR->>MS: 메뉴 자동 선택
    MS-->>MSM: 모듈 스크립트 주입
```

## Drawer 토글 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Btn as MenuToggleButton
    participant DM as DrawerMode
    participant MRM as MenuRailMode
    participant TRM as ToolRailMode
    participant DE as DrawerElement
    participant MRE as MenuRailElement
    participant TRE as ToolRailElement

    User->>Btn: 햄버거 버튼 클릭
    Btn->>DM: next(EXPAND 또는 COLLAPSE)
    DM-->>DE: state 변경
    DM-->>MRM: update(drawerState, toolCount)
    DM-->>TRM: update(drawerState, menuRailState, toolCount)
    MRM-->>MRE: expand() / collapse() / hide()
    TRM-->>TRE: expand() / collapse() / hide()
    Note over Btn: SVG 햄버거 ↔ X 애니메이션
```

## 토큰 자동 갱신 시퀀스

```mermaid
sequenceDiagram
    participant UA as UserApi
    participant Auth as /auth/refresh
    participant UP as UserProvider

    loop 10분 주기
        UA->>Auth: POST /auth/refresh
        alt 갱신 성공
            Auth-->>UA: 새 토큰
        else 갱신 실패
            UA-->>UP: null 발행 (로그아웃)
        end
    end
```

## i18n (다국어) 시퀀스

```mermaid
sequenceDiagram
    participant BD as BrowserLanguageDetector
    participant LP as LabelProvider
    participant FR as FetchLanguagePackRepository
    participant Server as /js/language.{lang}.json
    participant UI as UI 컴포넌트들

    BD->>BD: localStorage 'lang' 또는 navigator.language
    BD-->>LP: "ko" 감지
    LP->>FR: load("ko")
    FR->>Server: fetch language.ko.json
    alt 성공
        Server-->>FR: Labels JSON
    else 실패
        FR->>Server: fetch language.en.json (폴백)
        Server-->>FR: Labels JSON
    end
    FR-->>LP: Labels 발행
    LP-->>UI: subscribe() → 라벨 갱신
    Note over UI: 버튼 텍스트, 다이얼로그 제목 등 자동 갱신
```

## UC-S1: 사용자 인증 및 초기 로딩

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **정상 흐름** | 1. 페이지 로드 시 `UserApi`가 `/user` 엔드포인트에서 사용자 정보를 가져온다.<br>2. `UserProvider`가 사용자를 발행하면 `MenuList`가 `/menus` 엔드포인트에서 메뉴 목록을 로딩한다.<br>3. `WorkspaceList`가 사용자의 워크스페이스 목록을 추출한다.<br>4. `DrawerMode`가 COLLAPSE로 전환되고, Drawer UI가 렌더링된다.<br>5. `UrlBasedMenuResolver`가 현재 URL을 메뉴 정규식과 매칭하여 해당 메뉴를 자동 선택한다. |
| **대안 흐름** | 인증 실패 시(401) 사용자 정보가 null로 발행되고, DrawerMode가 HIDE로 전환된다. |

## UC-S2: 메뉴 선택 및 모듈 로딩

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 메뉴 목록 로딩 완료 |
| **정상 흐름** | 1. Menu Rail에서 메뉴 아이템을 클릭한다.<br>2. `MenuSelected`에 선택된 메뉴가 발행된다.<br>3. 도구가 1개뿐이면 `ToolSelected`에 자동 선택된다.<br>4. `ModuleScriptManager`가 메뉴의 `script` 필드에 지정된 JavaScript를 동적으로 `<script>` 태그로 주입한다.<br>5. `HistoryManager`가 `pushState()`로 URL을 업데이트한다.<br>6. `DrawerMode`가 COLLAPSE로 전환된다. |
| **대안 흐름** | 도구가 여러 개이면 Tool Rail이 EXPAND되어 도구 목록을 표시한다. |
| **선택 시각 표현** | 선택된 아이템은 `[selected]` 속성이 붙고 (1) 아이콘이 `fa-light` outline → `fa-solid` filled 로 교체되며, (2) `md-item` headline 라벨 색이 `--md-sys-color-primary` 로 변한다. 배경 채움은 사용하지 않는다 (MD3 nav rail 가이드: "선택 시 filled, 미선택 시 outlined"). 아이콘 스왑은 두 weight 를 동시 렌더해 두고 CSS 로 가시성을 토글하는 방식이라 JS 재렌더가 필요 없다. |

## UC-S3: 도구 선택 및 실행

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 메뉴 선택 완료, 도구 2개 이상 |
| **정상 흐름** | 1. Tool Rail에서 도구 아이템을 클릭한다.<br>2. `ToolSelected`에 선택된 도구가 발행된다.<br>3. `ToolExecutionManager`가 도구의 `ToolFunction.exec()`를 실행한다.<br>4. DOM 준비가 안 되었으면 100ms 후 재시도한다.<br>5. `ToolBasedMenuResolver`가 도구의 부모 메뉴를 역추적하여 `MenuSelected`를 동기화한다. |

## UC-S4: URL 기반 딥링크

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (URL 직접 입력 또는 뒤로가기) |
| **정상 흐름** | 1. 브라우저 URL이 변경된다 (직접 입력 또는 popstate 이벤트).<br>2. `UrlBasedMenuResolver`가 모든 메뉴의 `urlRegex` 패턴과 현재 URL을 매칭한다.<br>3. 매칭된 메뉴가 자동 선택된다.<br>4. UC-S2의 3~6단계가 실행된다. |

## UC-S5: Drawer 토글

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **정상 흐름** | 1. 햄버거 버튼(MenuToggleButton)을 클릭한다.<br>2. `DrawerMode`가 EXPAND ↔ COLLAPSE를 토글한다.<br>3. Menu Rail이 반응: EXPAND(아이콘+텍스트) 또는 COLLAPSE(아이콘만).<br>4. Tool Rail이 반응: EXPAND → COLLAPSE, 또는 도구 개수/MenuRail 상태에 따라 HIDE. |
| **대안 흐름 (모바일)** | 뷰포트 < 768px일 때 `DrawerMode`가 OVERLAY 상태로 전환된다. EXPAND/COLLAPSE 대신 OVERLAY ↔ HIDE를 토글하며, 메뉴 선택 시 자동으로 HIDE된다. 왼쪽 가장자리 스와이프로도 열 수 있다. |
| **SVG 애니메이션** | 햄버거 아이콘이 EXPAND↔COLLAPSE 전환 시 부드럽게 변형된다. |

## UC-S6: 메뉴 호버 시 도구 미리보기

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | Drawer가 EXPAND 상태, 도구가 2개 이상인 메뉴 |
| **정상 흐름** | 1. 메뉴 아이템에 마우스를 올린다.<br>2. `MenuHover`가 호버된 메뉴를 발행한다.<br>3. `ToolList`가 호버 메뉴의 도구 목록으로 업데이트된다.<br>4. `MenuHoverElementProvider`가 호버된 아이템의 위치를 추적한다.<br>5. Tool Rail이 해당 메뉴 아이템 옆에 정렬되어 도구를 미리 표시한다. |

## UC-S7: 워크스페이스 전환

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 워크스페이스가 2개 이상 |
| **정상 흐름** | 1. Drawer 상단의 워크스페이스 셀렉트 드롭다운을 클릭한다.<br>2. 목록에서 워크스페이스를 선택한다.<br>3. 컨텍스트가 전환되어 해당 워크스페이스의 데이터가 로딩된다. |

## UC-S8: 토큰 자동 갱신

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (자동) |
| **정상 흐름** | 1. `UserApi`가 10분 주기로 `/auth/refresh`를 호출하여 토큰을 갱신한다.<br>2. 갱신 실패 시 사용자를 null로 발행하여 로그아웃 상태로 전환한다. |

## UC-S9: 에이전트에 의한 화면 이동

| 항목 | 내용 |
|------|------|
| **액터** | AI 에이전트 |
| **정상 흐름** | 1. agent-ui의 `NavigateHandler`가 `Observer<String> uri`에 URL을 발행한다.<br>2. `HostSharedModule`의 `BehaviorSubject<String> uri`가 변경을 전파한다.<br>3. `UrlBasedMenuResolver`가 URL을 메뉴에 매칭한다.<br>4. 메뉴가 자동 선택되고, 해당 모듈이 로딩된다. |
| **특이사항** | 에이전트의 navigate 커맨드가 Shell의 일반 URL 기반 메뉴 해석과 동일한 경로를 사용한다. |

## UC-S14: 실시간 협업 — 다른 사용자의 변경 이벤트 수신

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (자동) |
| **선행조건** | 워크스페이스 SSE(`/workspace/{id}/messages`) 스트림 연결 상태 |
| **정상 흐름** | 1. 같은 워크스페이스의 다른 사용자가 데이터를 변경하면 DOCUMENT_CREATED, TYPE_CREATED 등의 이벤트가 Kafka를 통해 발행된다.<br>2. event-broadcaster가 동일한 SSE 스트림으로 브로드캐스트한다.<br>3. Shell-UI가 이벤트 타입에 따라 해당 모듈(메뉴, 워크스페이스 목록 등)의 데이터를 자동 갱신한다. |
| **특이사항** | 사용자 변경 이벤트와 에이전트 커맨드(AGENT_COMMAND)가 모두 같은 SSE 스트림으로 전달된다. 모든 참여자(사용자 + 에이전트)가 동일한 이벤트 채널을 공유한다. |

## UC-S10: 다국어 (i18n)

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (자동) |
| **정상 흐름** | 1. `BrowserLanguageDetector`가 localStorage 또는 navigator.language에서 언어를 감지한다.<br>2. `FetchLanguagePackRepository`가 `language.{lang}.json`을 fetch한다. 실패 시 `language.en.json`으로 폴백.<br>3. `LabelProvider`가 Labels를 발행하면 모든 구독 컴포넌트(버튼, 다이얼로그, 메뉴 아이템)의 텍스트가 자동 갱신된다. |
| **특이사항** | type-ui, workspace-ui 등 별도 GWT 모듈도 각자 LabelProvider를 구독하여 i18n 적용. |

## UC-S11: 프레임 전환

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (메뉴 선택 시 자동) |
| **선행조건** | 메뉴 선택으로 모듈 스크립트가 주입됨 |
| **정상 흐름** | 1. 로딩된 모듈이 `Observer<Render>`에 렌더 콜백을 발행한다.<br>2. `FrameUpdater`가 `FrameFactory`로 새 `FrameElement`를 생성한다.<br>3. 이전 프레임에 fadeOut 적용 (100ms) 후 DOM에서 제거.<br>4. 새 프레임에 fadeIn 적용하여 `ContentElement`에 추가. |

## UC-S12: 진행률 표시

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (API 호출 또는 에이전트 진행) |
| **정상 흐름** | 1. `MenuApi`/`UserApi`가 API 호출 시 `Observer<Progress>`에 `Progress.indeterminate()`를 발행한다.<br>2. `ProgressElement`가 구독하여 프로그레스 바를 표시한다.<br>3. 응답 수신 시 `Progress.hide()`를 발행하여 숨긴다.<br>4. 에이전트의 `ProgressHandler`도 동일한 `Observer<Progress>`를 사용하여 진행률 표시. |
| **특이사항** | API 로딩과 에이전트 진행률이 단일 프로그레스 바를 공유한다. |

## 모바일 Drawer 전환 시퀀스 (드릴인 패턴)

모바일에서는 하단 바 한 줄을 컨텍스트에 따라 스왑한다: 평소에는 `MenuRail` 을 하단
네비게이션으로 보여주고, 도구가 2개 이상인 메뉴를 탭하면 같은 자리에서 `ToolRail`
(← 아이콘 포함) 로 교체된다. 돌아가기는 `CloseToolRailButton` 이 `MenuSelected` 를
초기화해 도구 목록을 비워서 다시 `MenuRail` 이 올라오게 한다.

```mermaid
sequenceDiagram
    actor User as 사용자 (모바일)
    participant VP as ViewportObserver
    participant MS as MenuSelected
    participant TL as ToolList
    participant DM as DrawerMode
    participant MRM as MenuRailMode
    participant TRM as ToolRailMode
    participant MR as MenuRailElement
    participant TR as ToolRailElement
    participant Close as CloseToolRailButton

    Note over VP: 뷰포트 < 768px 감지 (또는 초기 로드)
    VP->>DM: next(HIDE)
    VP->>MR: setAttribute(mobile) — 레이아웃 고정
    VP->>TR: setAttribute(mobile) — 레이아웃 고정
    VP->>MRM: mobile=true → EXPAND (tools ≤ 1)
    VP->>TRM: mobile=true → HIDE (tools ≤ 1)
    MRM-->>MR: expand() → [mobile][expand] 하단 바에 메뉴 렌더

    User->>MR: 도구가 여러 개인 메뉴 탭
    MR->>MS: next(menu)
    MS->>TL: 도구 목록 갱신 (size > 1)
    TL-->>MRM: tools>1 → HIDE
    TL-->>TRM: tools>1 → EXPAND
    MRM-->>MR: hide() → [mobile][hide] (translateY 100%, opacity 0)
    TRM-->>TR: expand() → [mobile][expand] (translateY 0, opacity 1)
    Note over TR: 첫 아이템으로 CloseToolRailButton(←) 포함

    User->>Close: ← 탭
    Close->>MS: next(null)
    MS->>TL: 도구 목록 clear
    TL-->>MRM: tools≤1 → EXPAND
    TL-->>TRM: tools≤1 → HIDE
    MRM-->>MR: expand() → [mobile][expand] 복귀
```

## UC-S13: 모바일 반응형 레이아웃 (단일 하단 바 드릴인)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (모바일/태블릿 디바이스) |
| **선행조건** | 뷰포트 너비 < 768px |
| **레이아웃 모델** | 뷰포트(모바일/데스크톱) 와 가시성(expand/collapse/hide) 을 두 축으로 분리. `ViewportObserver` 는 양쪽 rail 에 `[mobile]` 속성을 상시 부여해 `position: fixed; bottom: 0; width: 100%` 레이아웃을 고정시키고, 상태 머신(`MenuRailMode`/`ToolRailMode`) 은 `EXPAND/COLLAPSE/HIDE` 가시성만 다룬다. |
| **정상 흐름 (MenuRail)** | 1. `ViewportObserver` 가 `mobile=true` 를 발행하면 두 rail 에 `[mobile]` 속성이 붙는다.<br>2. `MenuRailMode` 는 `toolList.size ≤ 1` 일 때 `EXPAND` 로 고정되어 하단 바에 메뉴를 보인다 (`[mobile][expand]`).<br>3. `ToolRailMode` 는 같은 조건에서 `HIDE` 로 유지된다 (`[mobile][hide]` — slide-down).<br>4. `DrawerMode` 는 `HIDE` 로 유지된다 (사이드 drawer 는 숨김). |
| **드릴인 흐름 (ToolRail)** | 1. 사용자가 도구가 2개 이상인 메뉴를 탭해 `MenuSelected` 가 갱신되면 `ToolList` 가 채워진다.<br>2. `MenuRailMode` 는 `tools > 1 && mobile` 일 때 `HIDE` 로 전환된다 → MenuRail slide-down.<br>3. `ToolRailMode` 는 같은 조건에서 `EXPAND` 로 전환되어 `ToolRail` 이 하단 바 자리를 차지한다 → slide-up (MD3 emphasized-decelerate, 300ms).<br>4. `[mobile]` 속성은 계속 유지되므로 fixed 포지션이 동일해 drill-in 시 "좌측 컬럼 → 하단 바" 이동 flash 가 없다.<br>5. `ToolRail` 의 첫 아이템으로 `CloseToolRailButton` (← 아이콘) 이 표시된다. |
| **드릴백 흐름** | 1. 사용자가 `CloseToolRailButton` 을 탭하면 `MenuSelected.next(null)` 이 호출된다.<br>2. `ToolList` 가 비어 `ToolRailMode → HIDE`, `MenuRailMode → EXPAND` 로 복귀한다.<br>3. 사용자는 다시 메뉴를 고를 수 있다. |
| **특이사항** | (1) 모바일에서는 MenuRail 과 ToolRail 이 동시에 보이지 않는다 — 한 번에 한 컨텍스트. (2) `MenuRailState`/`ToolRailState` 는 `EXPAND/COLLAPSE/HIDE` 세 가지만 가지며 모바일 여부는 상태 머신과 직교. (3) 도구가 1개인 메뉴를 탭하면 드릴인 없이 바로 해당 도구로 이동하고 MenuRail 은 유지된다. |
| **터치 지원** | 화면 왼쪽 가장자리에서 오른쪽으로 스와이프하면 `DrawerMode.toggleOverlay()` 로 OVERLAY drawer 를 열 수 있다 (워크스페이스 셀렉터 등 secondary UI 진입). |

## UC-S20: 브릿지 게시 (모듈 간 통신 초기화)

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (자동) |
| **선행조건** | shell-ui 초기화 완료 (UC-S1 이후) |
| **정상 흐름** | 1. `ShellInitializer.publishBridges()`가 `WindowProgressBridge.register()`, `WindowUriBridge.register()`, `WindowLabelBridge.publish()`를 호출하여 shell의 Progress/URI/Label 상태를 window 객체에 등록한다.<br>2. `handbook-shell-ready` CustomEvent를 dispatch한다.<br>3. agent-ui 등 독립 GWT 모듈이 이 이벤트를 수신하고 브릿지를 통해 shell 상태에 접근한다. |
| **특이사항** | shell-ui와 agent-ui는 각각 독립된 GWT 컴파일 결과물(nocache.js)을 갖는다. Java 레벨 인터페이스 공유가 불가능하므로 agent-bridge 모듈이 제공하는 window 브릿지를 사용한다. |

## UC-S15: 사용자 설정 — 언어/테마 퍼시스턴스

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 인증 완료, Shell 로딩 완료 |
| **정상 흐름** | 1. 사용자가 설정 패널(Drawer 내 또는 별도 모달)을 연다.<br>2. 언어(ko/en)를 변경하면 localStorage에 저장되고 `LabelProvider`가 즉시 갱신된다.<br>3. **MenuRail 의 ThemeToggle 버튼** (sun/moon 아이콘) 을 클릭하면 `<html>` 의 `color-theme` 속성이 light↔dark 로 토글되고 localStorage 에 저장된다. 동시에 `<html>` 에 `theme-changing` 클래스가 500ms 동안 부착되어, 그 구간에만 sun/moon morph 애니메이션이 재생된다. |
| **요구사항** | 6.8 사용자 설정 |
| **상태** | 부분 구현 (UserPreferences, ThemeToggle 구현 완료. 언어 설정 패널 UI 미완) |
| **레이아웃** | `ThemeToggle` 은 `NavigationRailItemElement` 를 상속하여 일반 메뉴와 동일한 `.item > .collapse + md-item` 구조를 갖는다. MenuRail 의 마지막 자식으로 append 되며 `.rail-bottom` 클래스(+ `margin-top: auto`, `order: 1`) 로 하단에 고정. `bottom=true` 메뉴는 `.bottom-menu` 클래스(`order: 2`) 가 붙어 ThemeToggle 의 **아래쪽** 에 배치된다 — 즉 시각 순서는 일반 메뉴(0) → ThemeToggle(1) → bottom 메뉴(2). 모바일 `[mobile]` 에서는 row 방향이라 `margin-top: auto` 가 의미 없어지지만 `order: 1` 만으로 horizontal navbar 의 일반 메뉴와 bottom 메뉴 사이에 자연스럽게 배치되어 그대로 노출된다. |
| **i18n** | Headline 텍스트는 `LabelProvider` 를 구독하여 `darkMode` 상태에 따라 `theme.switch_to_dark` / `theme.switch_to_light` 키를 동적으로 바꿔 표시한다. expand 모드에서만 보이며 collapse 모드에서는 아이콘만 노출. |
| **애니메이션** | (1) 색 전환 — 전역 CSS 트랜지션으로 background/color/fill/stroke 가 점진 변화. (2) 아이콘 morph — sun/moon path 를 SVG 안에 동시 렌더. `:root.theme-changing[color-theme='...']` 조합 셀렉터로 rise/set keyframes 를 200ms 적용, 200ms stagger 로 교체. `.theme-changing` 는 클릭 시에만 500ms 부착되므로 드로어 expand/collapse 전환에는 재생되지 않는다. |

## UC-S16: 사용자 설정 패널 UI

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | Shell 로딩 완료 |
| **정상 흐름** | 1. Drawer 하단 또는 톱바의 설정 아이콘을 클릭한다.<br>2. 설정 패널이 열리고 언어 선택, 테마 토글 등의 옵션이 표시된다.<br>3. 변경 사항은 즉시 반영된다. |
| **요구사항** | 6.8 사용자 설정 |
| **상태** | 부분 구현 (ThemeToggle 구현 완료. 독립 설정 패널 UI 미완) |

## UC-S17: 세션 관리

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (자동) |
| **선행조건** | 사용자 인증 완료 |
| **정상 흐름** | 1. 토큰 만료 전 refresh token을 사용하여 자동 갱신한다.<br>2. 비활성 타임아웃 5분 전 경고 알림을 표시한다.<br>3. 세션 만료 시 로그인 페이지로 리다이렉트하고 알림을 표시한다. |
| **요구사항** | 6.11 세션 관리 |
| **상태** | 구현 완료 (SessionManager) |

---

## 트레이서빌리티 매트릭스

| UC | 시퀀스 다이어그램 | 클래스 다이어그램 섹션 | 주요 클래스 | 테스트 |
|----|---|---|---|---|
| UC-S1 (인증) | 초기 로딩 → 메뉴 선택 | 유스케이스, Frame+API | Application, UserApi, UserProvider, MenuList, MenuApi, UrlBasedMenuResolver, MenuSelected, ModuleScriptManager, WorkspaceList, DrawerMode | DrawerTest: 메뉴 초기화, Drawer DOM 존재, 메뉴 토글 버튼, 메뉴 레일 아이템 수 = 메뉴 수, 아이콘 수 = 아이템 수 |
| UC-S2 (메뉴선택) | 메뉴 클릭 → 모듈 로딩 | 유스케이스, Drawer UI | MenuRailElement, MenuRailItemElement, MenuSelected, ToolSelected, ModuleScriptManager, HistoryManager, DrawerMode | DrawerTest: URL 버튼 클릭 → selected 아이템 정확히 1개, 다른 URL 클릭 → 이전 선택 해제 + 새 선택, 선택 아이템에서 `.icon-outline` 숨김·`.icon-filled` 표시, 미선택 아이템은 그 반대 |
| UC-S3 (도구실행) | 메뉴 클릭 → 모듈 로딩 (alt) | 유스케이스, Drawer UI | ToolRailElement, ToolRailItemElement, ToolSelected, ToolExecutionManager | DrawerTest: 메뉴1 도구 ≤1, 메뉴2 도구 >1, 같은 메뉴 다른 Tool URL → 선택 아이템 유지 |
| UC-S4 (딥링크) | 초기 로딩과 동일 경로 | 유스케이스 | UrlBasedMenuResolver, HistoryManager, MenuSelected | DrawerTest: URL hash 변경 → Drawer/레일 아이템 수 유지 |
| UC-S5 (Drawer토글) | Drawer 토글 | 유스케이스, Drawer UI | MenuToggleButton, DrawerMode, MenuRailMode, ToolRailMode, DrawerElement, MenuRailElement, ToolRailElement | DrawerModeTest: MenuRailState/ToolRailState 상태 전이 검증 |
| UC-S6 (호버) | — (단순) | 유스케이스, Drawer UI | MenuHover, MenuHoverElementProvider, ToolList, ToolRailElement | DrawerTest: Tool Rail 영역(.tool-rail) 존재 확인 |
| UC-S7 (워크스페이스) | — (단순) | Drawer UI | WorkspaceSelectElement, WorkspaceList | DrawerTest: 워크스페이스 선택 요소(.workspace-select) 존재 확인 |
| UC-S8 (토큰갱신) | 토큰 자동 갱신 | Frame+API | UserApi(periodicRefresh, REFRESH_INTERVAL) | ApiTest: 사용자 정보 로드, 사용자 ID/이름 표시, 주기적 갱신 설정 확인 |
| UC-S9 (에이전트) | 에이전트 화면 이동 | 유스케이스 | UrlBasedMenuResolver, MenuSelected, ModuleScriptManager, HostSharedModule(uri) | UrlBasedMenuResolverTest: 에이전트 navigate 커맨드 URL 패턴 처리 검증 |
| UC-S10 (i18n) | i18n (다국어) | Frame+API | BrowserLanguageDetector, FetchLanguagePackRepository, LabelProvider | DrawerTest: 메뉴 아이템에 텍스트 라벨 존재 확인 |
| UC-S11 (프레임전환) | — (단순) | Frame+API | FrameUpdater, FrameFactory, FrameElement, ContentElement | FrameTest: 컨테이너 존재, 초기 프레임 0개, 렌더러1 → 프레임 1개 + 텍스트 "Hello, World!!", 렌더러2 → 교체 + "2nd Renderer rendered", 재전환 → 프레임 1개 유지 |
| UC-S12 (진행률) | — (단순) | Frame+API | ProgressElement, Observer\<Progress\> | ProgressTest: 컨테이너/라벨 존재, 초기 opacity=0, indeterminate → opacity=1 + 라벨 숨김, 30% → "처리 중" + "3/10", 70% → "거의 완료" + "7/10", 100% → "완료" + "10/10", hide → opacity=0, 재표시 검증 |
| UC-S13 (모바일) | 모바일 드릴인/드릴백 | Drawer UI | ViewportObserver(isMobile → `[mobile]` 속성), MenuRailMode/ToolRailMode(EXPAND/HIDE 드릴인 스왑), CloseToolRailButton, MenuRailElement, ToolRailElement | DrawerModeTest: 드릴인/드릴백 상태 전이 + 상호 배타성 검증 / DrawerTest: 초기 모바일 로드, [mobile] 속성 부여, 드릴인(MenuRail HIDE, ToolRail EXPAND), 드릴백(원복) 검증 |
| UC-S14 (실시간협업) | — (SSE 이벤트 수신) | Frame+API | SSE /workspace/{id}/messages, 이벤트 타입별 UI 갱신 | UrlBasedMenuResolverTest: SSE 이벤트 기반 메뉴 갱신 검증 |
| UC-S15 (언어/테마) | — | Drawer UI | UserPreferences (activity), ThemeToggle (NavigationRailItemElement 상속, `.rail-bottom` 하단 고정, i18n headline, theme-changing 500ms 애니메이션 트리거), BrowserLanguageDetector, LabelProvider | DrawerTest: `.rail .rail-bottom.item` 존재, `.collapse` SVG 와 `md-item` start slot SVG 동시 렌더, 초기 color-theme light/dark, 클릭 시 토글 + theme-changing 클래스 일시 부착, expand 시 `.collapse` 숨김 / `md-item` 표시, bottom-menu 가 rail-bottom 아래 순서 |
| UC-S16 (설정패널) | — | Drawer UI | ThemeToggle (DrawerElement 내 통합), UserPreferences | ❌ 테스트 미작성 (ThemeToggle 구현 완료, 설정 패널 UI 미완) |
| UC-S17 (세션관리) | — | Frame+API | SessionManager, FetchApi, ToastContainer, LabelProvider | ❌ 테스트 미작성 (SessionManager 구현 완료) |
| UC-S18 (빈 상태 UI) | — | Frame+API | EmptyStateElement, ContentElement | ❌ 미구현 (계획) |
| UC-S19 (성공 피드백) | — | Frame+API | ToastContainer | ❌ 미구현 (계획) |
| UC-S20 (브릿지게시) | — | 조합 (DI) | ShellInitializer, WindowProgressBridge, WindowUriBridge, WindowLabelBridge | ❌ 테스트 미작성 (구현 완료) |
