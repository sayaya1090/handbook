# Agent-UI 클래스 다이어그램

## 도메인

```mermaid
classDiagram
    class AgentSessionState {
        <<enum>>
        IDLE
        PLANNING
        EXECUTING
        AWAITING_CONFIRM
        COMPLETED
        ABORTED
    }
    class NavigateInfo {
        -String menu
        -String tool
        -String url
        +menu(): String
        +tool(): String
        +url(): String
    }
    class ProgressInfo {
        -String description
        -double value
        -double max
        +percentage(): double
        +isComplete(): boolean
    }
    class OverlayRequest {
        -String target
        -AttentionStyle style
        -String message
        -String position
        -boolean dismissable
    }
    class ConfirmRequest {
        -String description
        -String[] options
    }
    class NotifyInfo {
        -String level
        -String message
    }
```

## 유스케이스 (포트 인터페이스)

```mermaid
classDiagram
    class AgentSession {
        <<interface>>
        +state(): Observable~AgentSessionState~
        +stateObserver(): Observer~AgentSessionState~
    }
    class AgentApiPort {
        <<interface>>
        +startSession(workspace: String, request: String)
        +respond(workspace: String, response: String)
        +abort(workspace: String)
    }
    class AgentCommandDispatcher {
        <<interface>>
        +overlayRequests(): Observable~OverlayRequest~
        +confirmRequests(): Observable~ConfirmRequest~
        +progressUpdates(): Observable~ProgressInfo~
        +previewRequests(): Observable~String[]~
        +completions(): Observable~String~
        +highlights(): Observable~String~
        +scrollTargets(): Observable~String~
        +navigations(): Observable~NavigateInfo~
        +mutations(): Observable~String[]~
        +notifications(): Observable~NotifyInfo~
    }
```

## 인터페이스 구현

```mermaid
classDiagram
    class AgentSessionImpl {
        <<@Singleton>>
        -BehaviorSubject~AgentSessionState~ subject
        초기값: IDLE
    }
    class AgentSseClient {
        <<@Singleton>>
        -CommandRouter router
        -AgentSession session
        -EventSource eventSource
        +startSession(workspace, request)
        +respond(workspace, response)
        +abort(workspace)
        -connectSse(workspace)
        -closeSse()
    }
    class CommandRouter {
        <<@Singleton>>
        -AgentSession session
        -BehaviorSubject~OverlayRequest~ overlaySubject
        -BehaviorSubject~ConfirmRequest~ confirmSubject
        -BehaviorSubject~ProgressInfo~ progressSubject
        -BehaviorSubject~String[]~ previewSubject
        -BehaviorSubject~String~ completeSubject
        -BehaviorSubject~String~ highlightSubject
        -BehaviorSubject~String~ scrollSubject
        -BehaviorSubject~NavigateInfo~ navigateSubject
        -BehaviorSubject~String[]~ mutateSubject
        -BehaviorSubject~NotifyInfo~ notifySubject
        +route(json: String)
        -routeNative(json) «JSNI»
        -toStringArray(jsArray): String[]
    }

    AgentSessionImpl ..|> AgentSession
    AgentSseClient ..|> AgentApiPort
    CommandRouter ..|> AgentCommandDispatcher
    AgentSseClient --> CommandRouter : route() 위임
    CommandRouter --> AgentSession : 상태 전환
```

## 핸들러 + UI 컴포넌트

```mermaid
classDiagram
    class AgentInputElement {
        <<@Singleton, IsElement>>
        -OutlinedTextFieldElementBuilder textField
        -HTMLElement sendBtn, abortBtn
        -AgentApiPort api
        -Labels labels
        -AgentSessionState currentState
        -String workspace
        +setWorkspace(workspace)
        -send()
        -abort()
        -onStateChange(state)
        -applyLabels()
    }
    class HighlightHandler {
        <<@Singleton>>
        -HighlightEffect effect
        구독: highlights()
    }
    class ScrollHandler {
        <<@Singleton>>
        -ScrollEffect effect
        구독: scrollTargets()
    }
    class OverlayElement {
        <<@Singleton, IsElement>>
        -OverlayContainer overlay
        구독: overlayRequests()
    }
    class ConfirmDialogElement {
        <<@Singleton, IsElement>>
        -ConfirmDialog dialog
        -ResponseCallback callback
        +onResponse(callback)
        구독: confirmRequests()
    }
    class PreviewPanelElement {
        <<@Singleton, IsElement>>
        -DiffPanel panel
        구독: previewRequests()
    }
    class NavigateHandler {
        <<@Singleton, IsElement>>
        구독: navigations()
        Observer~String~ uri에 발행
    }
    class NotifyHandler {
        <<@Singleton, IsElement>>
        -ToastContainer toast
        구독: notifications()
    }
    class ProgressHandler {
        <<@Singleton>>
        구독: progressUpdates()
        Observer~Progress~ progress에 발행
    }
    class CompleteHandler {
        <<@Singleton, IsElement>>
        -ToastContainer toast
        구독: completions()
        진행률 숨김 + 토스트 5초
    }
    class MutateHandler {
        <<@Singleton, IsElement>>
        구독: mutations()
        WindowMutationBridge.publish() 호출
        변경 로그 표시 + 3초 페이드아웃
    }

    AgentInputElement --> AgentApiPort : start/abort
    AgentInputElement --> AgentSession : 상태 구독
    AgentInputElement --> ConfirmDialogElement : 응답 콜백

    HighlightHandler --> AgentCommandDispatcher
    ScrollHandler --> AgentCommandDispatcher
    OverlayElement --> AgentCommandDispatcher
    ConfirmDialogElement --> AgentCommandDispatcher
    PreviewPanelElement --> AgentCommandDispatcher
    NavigateHandler --> AgentCommandDispatcher
    NotifyHandler --> AgentCommandDispatcher
    ProgressHandler --> AgentCommandDispatcher
    CompleteHandler --> AgentCommandDispatcher
    MutateHandler --> AgentCommandDispatcher
```

## 조합 (DI)

```mermaid
classDiagram
    class AgentModule {
        <<@Module>>
        +agentSession(AgentSessionImpl): AgentSession «@Binds»
        +commandDispatcher(CommandRouter): AgentCommandDispatcher «@Binds»
        +agentApi(AgentSseClient): AgentApiPort «@Binds»
    }
    class AgentInitializer {
        <<@Singleton>>
        -HighlightHandler
        -ScrollHandler
        -ProgressHandler
        -OverlayElement
        -ConfirmDialogElement
        -PreviewPanelElement
        -NavigateHandler
        -NotifyHandler
        -CompleteHandler
        -MutateHandler
        -AgentInputElement
        +initialize()
    }

    AgentInitializer --> HighlightHandler
    AgentInitializer --> ScrollHandler
    AgentInitializer --> ProgressHandler
    AgentInitializer --> OverlayElement
    AgentInitializer --> ConfirmDialogElement
    AgentInitializer --> PreviewPanelElement
    AgentInitializer --> NavigateHandler
    AgentInitializer --> NotifyHandler
    AgentInitializer --> CompleteHandler
    AgentInitializer --> MutateHandler
    AgentInitializer --> AgentInputElement
```

## 디자인 패턴

| 패턴 | 적용 클래스 | 설명 |
|------|------------|------|
| **Router/Dispatcher** | `CommandRouter` (AgentCommandDispatcher 구현) | SSE로 수신된 JSON을 파싱하여 `type` 필드에 따라 10개 BehaviorSubject 중 하나에 라우팅. 각 핸들러가 해당 Subject를 구독하여 반응. |
| **Observer** | `AgentSessionImpl`, `CommandRouter`의 10개 BehaviorSubject | 세션 상태와 커맨드 스트림을 Observable로 발행. UI 컴포넌트가 구독하여 반응형으로 동작. |
| **State** | `AgentInputElement` + `AgentSessionState` | 6개 세션 상태(IDLE→PLANNING→EXECUTING→AWAITING_CONFIRM→COMPLETED/ABORTED)에 따라 입력 필드 활성화, 버튼 전환, 라벨 변경이 자동 전환. |
| **Port/Adapter** | `AgentApiPort`(포트) ↔ `AgentSseClient`(어댑터), `AgentCommandDispatcher`(포트) ↔ `CommandRouter`(어댑터) | 헥사고날 아키텍처. 유스케이스 레이어가 포트에만 의존하고, SSE/REST 구현 세부사항은 어댑터에 캡슐화. |
| **Callback** | `ConfirmDialogElement.ResponseCallback` | 확인 다이얼로그의 사용자 응답을 콜백으로 전달. `AgentInputElement`가 콜백을 등록하여 `api.respond()` 호출. |

## 모바일 지원

```mermaid
classDiagram
    class ViewportObserver {
        -BehaviorSubject~Boolean~ isMobile
        +isMobile(): Observable~Boolean~
    }

    class AgentInputElement {
        +setMobileMode(boolean mobile)
        -adjustForKeyboard()
        Note: 모바일: position fixed bottom 0
        Note: visualViewport API로 키보드 높이 감지
    }

    class PreviewPanelElement {
        +setMobileMode(boolean mobile)
        Note: 모바일: flex-direction column
    }

    class ConfirmDialogElement {
        +setMobileMode(boolean mobile)
        Note: 모바일: bottom sheet 스타일
    }

    ViewportObserver --> AgentInputElement
    ViewportObserver --> PreviewPanelElement
    ViewportObserver --> ConfirmDialogElement
```

| 클래스 | 모바일 동작 |
|--------|-----------|
| `ViewportObserver` | matchMedia 감지 → 각 컴포넌트에 모바일 모드 전달 |
| `AgentInputElement` | 하단 고정 + visualViewport.resize로 키보드 높이 보정 |
| `PreviewPanelElement` | before/after 세로 스택 (flex-direction: column) |
| `ConfirmDialogElement` | bottom sheet (하단에서 슬라이드 업) |
| `OverlayElement` | 터치 탭으로 닫기 (click 이벤트와 동일) |
