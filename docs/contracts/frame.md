# Shell Frame Mount 계약

shell-ui 의 `FrameUpdater` 가 자식 GWT UI 모듈에 제공하는 **컨텐츠 mount 프로토콜**.
각 UI 모듈은 자신의 루트 컨테이너를 body 에 직접 append 하지 않고, 이 브릿지로 shell 에
Render 콜백을 넘겨 shell 이 Frame 엘리먼트를 생성·배치·여백 관리를 담당한다.

## 공급자 (Providers)

- **shell-ui** — Frame 영역 배치 · 여백 · fade-in/out · rail/AppBar 오프셋 계산
  - `client/interfaces/frame/FrameUpdater.java`
  - `client/interfaces/frame/FrameElement.java` (MD3 surface, transition)
  - `client/interfaces/ContentElement.java` (FrameContainer 구현)
  - `shell-ui/src/main/webapp/css/shell.css` `.frame`

## 소비자 (Consumers)

자식 UI 모듈은 `Application.onModuleLoad()` 에서 `WindowRenderBridge.next(render)` 로
`Render` 객체(`HTMLElement frame -> boolean`) 를 전달한다. shell 은 Frame 을 만들고
`onInvoke(frame.element())` 를 호출해 모듈이 frame 내부에 DOM 을 붙일 기회를 제공한다.

| 모듈 | 루트 컨테이너 | 비고 |
|------|-------------|------|
| login-ui | `ContentElement` | 최초 도입 모듈 (reference pattern) |
| workspace-ui | `ContentElement` (`.ws-content`) | UC-12 온보딩 경로 |
| type-ui | `.type-container` (controller + canvas + attributeEditor) | — |
| document-ui | `.doc-container` (controller + spreadsheet) | — |
| dashboard-ui | `.dashboard` | `:agent-bridge` 의존 필요 |

### 예외 (bridge 경유 불필요)

- **login-ui `LogoutApplication`** — 전면 리다이렉트 페이지. shell 이 아예 없는 상태에서
  body 에 직접 렌더.
- **agent-ui 오버레이 엘리먼트** (toast, confirm dialog, preview panel, input dock 등) —
  `position:fixed` 자체 관리, shell Frame 위로 float.

## Render 함수 시그니처

```java
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.WindowRenderBridge;

Render render = frame -> {
    frame.append(component.contentElement().element());
    return true;
};
WindowRenderBridge.next(render);
```

- `Render` 는 `@JsFunction` 인터페이스 — 모듈 경계(GWT permutation) 를 넘어 전달 가능.
- 반환 `boolean` 은 현재 사용처 없음 (후속 flow control 여지).
- shell 은 매 Render 수신마다 **기존 Frame 을 fade-out 후 제거**, 새 Frame 을 fade-in
  (`FrameUpdater.next()`).

## 레이아웃 토큰 (shell 만 정의·관리)

`.frame` 은 아래 토큰 기반으로 영역을 결정한다. 모듈은 이 토큰을 읽지 않고 오직
shell 이 여백·오프셋을 통제한다.

| 토큰 | Desktop | Mobile | 용도 |
|------|---------|--------|------|
| `--shell-app-bar-height` | 56px | 56px | Top App Bar 점유 영역 |
| `--shell-mobile-tabs-height` | 0 | 49px (`.menu-tabs[hide]` 시 0) | MobileTabs 점유 영역 |
| `--shell-frame-left-offset` | 3.5rem (rail collapse 폭 고정) | 0 | 좌측 rail 영역 확보 |
| `--shell-drawer-width` | 3.5rem ~ 32rem (rail 상태 조합) | 0 | AppBar/MobileTabs 좌측 시작점 (Frame 은 쓰지 않음) |

**중요**: Frame 의 left 는 `--shell-drawer-width` (동적, rail 상태 따라 확장) 가 아니라
`--shell-frame-left-offset` (collapse 폭 고정) 를 쓴다. 사용자가 rail 을 EXPAND 하면
rail 이 본문 위로 overlay 되는 것이 의도된 동작이기 때문 (MD3 Standard Navigation Drawer).

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 새 UI 모듈 추가 | `Application.onModuleLoad()` 에서 `WindowRenderBridge.next(render)` 사용. body 직접 append 금지. `:agent-bridge` 의존 추가 + `<inherits name="dev.sayaya.handbook.AgentBridge"/>` |
| `.frame` 레이아웃 변경 | shell.css `.frame` 룰 + 토큰 4종 (app-bar/mobile-tabs/frame-left-offset/drawer-width). 모듈별 컨테이너의 height/width 가정 깨지는지 확인 |
| 레이아웃 토큰 추가 | 본 문서 토큰 표 갱신 + `docs/contracts/design-tokens.md` 반영 |
| FrameUpdater API 변경 | `WindowRenderBridge` signature 함께 검토 — `Render` 는 @JsFunction 이라 하위 모듈 재컴파일 필요 |

## 디버깅

| 증상 | 원인 | 해결 |
|------|------|------|
| 모듈이 body 에 존재하는데 화면엔 안 보임 | `body{position:fixed; inset:0}` + 이미 점유된 shell `#content{height:100dvh}` 뒤에 스택되어 y=100dvh 에 렌더됨 | `body().add()` → `WindowRenderBridge.next(render)` 로 전환. `FrameUpdater` 가 rail/AppBar 오프셋 + 여백 적용 |
| 패널이 rail 에 가려짐 | rail EXPAND 동적 폭 계산 의존 (과거 문제) | 현재 `.frame` 은 `--shell-frame-left-offset` 고정 폭 사용. rail expand overlay 는 의도 |
| 모바일 상단 tabs 에 컨텐츠 가려짐 | `.frame` top offset 에 MobileTabs 높이 미포함 | `--shell-mobile-tabs-height` 가 상단 offset 에 합산되는지 확인 |
