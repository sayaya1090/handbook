# Shell-UI 클래스 다이어그램

## 도메인

```mermaid
classDiagram
    class DrawerState {
        <<enum>>
        COLLAPSE
        EXPAND
        HIDE
    }
    class MenuRailState {
        <<enum>>
        COLLAPSE
        EXPAND
        HIDE
    }
    class ToolRailState {
        <<enum>>
        COLLAPSE
        EXPAND
        HIDE
    }
    class User {
        <<@JsType native>>
        +String id
        +String name
        +Workspace[] workspaces
    }
    class Workspace {
        <<@JsType native>>
        +String id
        +String name
    }
    User *-- Workspace
```

## 유스케이스 (상태 관리)

```mermaid
classDiagram
    class UserProvider {
        <<@Singleton>>
        -ReplaySubject~User~ _this «@Delegate»
        +UserProvider(UserRepository repo)
    }
    class UserRepository {
        <<interface>>
        +find(): Observable~User~
    }
    class MenuList {
        <<@Singleton>>
        -BehaviorSubject~List~Menu~~ _this «@Delegate»
        -MenuRepository menuRepository
        +MenuList(UserProvider, MenuRepository)
        -update(user: User)
        -updateIfChanged(list: List~Menu~)
    }
    class MenuRepository {
        <<interface>>
        +findAll(): Observable~List~Menu~~
    }
    class MenuSelected {
        <<@Singleton>>
        -BehaviorSubject~Menu~ _this «@Delegate»
        +MenuSelected(ToolSelected tool)
    }
    class MenuHover {
        <<@Singleton>>
        -BehaviorSubject~Menu~ _this «@Delegate»
    }
    class ToolList {
        <<@Singleton>>
        -BehaviorSubject~List~Tool~~ _this «@Delegate»
        +ToolList(MenuSelected, MenuHover)
        -update(menu: Menu)
    }
    class ToolSelected {
        <<@Singleton>>
        -BehaviorSubject~Tool~ _this «@Delegate»
        +ToolSelected(ToolExecutionManager executor)
    }
    class ToolExecutionManager {
        <<@Singleton>>
        -Subscription executionSubscription
        +register(tool: Tool)
        -execute(function: ToolFunction)
    }
    class DrawerMode {
        <<@Singleton>>
        -BehaviorSubject~DrawerState~ _this «@Delegate»
        +DrawerMode(MenuSelected, ToolSelected)
    }
    class MenuRailMode {
        <<@Singleton>>
        -BehaviorSubject~MenuRailState~ _this «@Delegate»
        +MenuRailMode(DrawerMode, ToolList)
        -update(drawerState, hasNoChildren)
    }
    class ToolRailMode {
        <<@Singleton>>
        -BehaviorSubject~ToolRailState~ _this «@Delegate»
        +ToolRailMode(DrawerMode, MenuRailMode, ToolList)
        -update(drawerState, menuState, hasMultipleChildren)
    }
    class HistoryManager {
        <<@Singleton>>
        -BehaviorSubject~String~ uri
        +initialize()
        -update(url: String)
    }
    class UrlBasedMenuResolver {
        <<@Singleton>>
        -Map~JsRegExp,Menu~ map
        -MenuSelected select
        -DrawerMode drawer
        -String lastKnownUri
        +initialize()
        -resolve(uri: String)
    }
    class ToolBasedMenuResolver {
        <<@Singleton>>
        -Map~Tool,Menu~ map
        +initialize()
        -resolve(tool: Tool)
    }
    class ModuleScriptManager {
        <<@Singleton>>
        -MenuSelected menu
        +initialize()
        -update(menu: Menu)
    }
    class WorkspaceList {
        <<@Singleton>>
        -BehaviorSubject~List~Workspace~~ _this «@Delegate»
        +WorkspaceList(UserProvider)
        -update(user: User)
    }

    UserProvider --> UserRepository : 구독
    MenuList --> UserProvider : 사용자 변경 감지
    MenuList --> MenuRepository : 메뉴 로딩
    MenuSelected ..> ToolSelected : 도구 1개면 자동 선택
    ToolSelected --> ToolExecutionManager : exec() 위임
    ToolList --> MenuSelected : 선택 메뉴의 도구
    ToolList --> MenuHover : 호버 메뉴의 도구
    DrawerMode --> MenuSelected : 메뉴 선택 시 COLLAPSE
    DrawerMode --> ToolSelected : 도구 선택 시 COLLAPSE
    MenuRailMode --> DrawerMode : 상태에 반응
    ToolRailMode --> DrawerMode : 상태에 반응
    ToolRailMode --> MenuRailMode : 상태에 반응
    UrlBasedMenuResolver --> MenuSelected : URL 매칭 → 메뉴 선택
    ToolBasedMenuResolver --> MenuSelected : 도구 → 부모 메뉴 역추적
    ModuleScriptManager --> MenuSelected : 스크립트 동적 주입
    WorkspaceList --> UserProvider : 워크스페이스 추출
```

## Drawer UI 컴포넌트

```mermaid
classDiagram
    class ContentElement {
        <<@Singleton>>
        +IsElement~HTMLDivElement~
        +FrameContainer
        +ContentElement(DrawerElement drawer)
    }
    class DrawerElement {
        <<@Singleton>>
        +IsElement~HTMLElement~
        +DrawerElement(DrawerMode, MenuRailElement, ToolRailElement, ShellStylesheet)
        -state(state: DrawerState)
    }
    class NavigationRailElement~E~ {
        <<interface>>
        +expand()
        +collapse()
        +hide()
    }
    class NavigationRailItemElement {
        <<abstract>>
        -IconButtonElementBuilder collapse
        -HTMLContainerBuilder expand
        +select(value: boolean)
        +icon(icon: Element): NavigationRailItemElement
        +headline(text: String): NavigationRailItemElement
        +supportingText(text: String): NavigationRailItemElement
        +on(type, callback)
    }
    class MenuRailElement {
        <<@Singleton>>
        -MenuRailItemFactory factory
        -List~MenuRailItemElement~ children
        +MenuRailElement(MenuList, MenuRailMode, factory, ViewportObserver, MenuToggleButton)
        -update(menus: List~Menu~)
        -mode(state: MenuRailState)
    }
    class ToolRailElement {
        <<@Singleton>>
        -ToolRailItemFactory factory
        -List~ToolRailItemElement~ children
        -CloseToolRailButton close
        +ToolRailElement(ToolList, ToolRailMode, MenuSelectedElementProvider, factory, close)
        -update(tools: List~Tool~)
        -offset(parent: MenuRailItemElement)
    }
    class MenuRailItemElement {
        <<@AssistedInject>>
        -Menu menu
        -TooltipCard tooltip
        +MenuRailItemElement(menu, MenuSelected, MenuHover, MenuSelectedElementProvider, MenuRailMode, LabelProvider)
        -observeHighlight() : MutationObserver
    }
    class ToolRailItemElement {
        <<@AssistedInject>>
        -Tool tool
        +ToolRailItemElement(tool, ToolSelected, LabelProvider)
    }
    class MenuRailItemFactory {
        <<@AssistedFactory>>
        +item(menu: Menu): MenuRailItemElement
    }
    class ToolRailItemFactory {
        <<@AssistedFactory>>
        +item(tool: Tool): ToolRailItemElement
    }
    class MenuToggleButton {
        <<@Singleton>>
        -DrawerMode mode
        SVG 햄버거 ↔ X 애니메이션
        -toggleDrawerState()
    }
    class CloseToolRailButton {
        <<@Singleton>>
        -MenuRailMode menu
        -ToolRailMode tools
    }
    class WorkspaceSelectElement {
        <<@Singleton>>
        -WorkspaceList workspaces
        OutlinedSelectElementBuilder
    }
    class MenuSelectedElementProvider {
        <<@Singleton>>
        -BehaviorSubject~MenuRailItemElement~ _this
    }

    ContentElement --> DrawerElement
    DrawerElement --> MenuRailElement
    DrawerElement --> ToolRailElement
    MenuRailElement --> MenuToggleButton
    NavigationRailElement <|.. MenuRailElement
    NavigationRailElement <|.. ToolRailElement
    NavigationRailItemElement <|-- MenuRailItemElement
    NavigationRailItemElement <|-- ToolRailItemElement
    NavigationRailItemElement <|-- CloseToolRailButton
    MenuRailElement --> MenuRailItemFactory : creates
    ToolRailElement --> ToolRailItemFactory : creates
    ToolRailElement --> MenuSelectedElementProvider : 위치 추적
    MenuRailItemFactory ..> MenuRailItemElement
    ToolRailItemFactory ..> ToolRailItemElement
```

## Frame + API

```mermaid
classDiagram
    class FrameContainer {
        <<interface>>
        +add(element): FrameContainer
    }
    class FrameElement {
        <<@AssistedInject>>
        +fadeOut()
        +fadeIn()
    }
    class FrameFactory {
        <<@AssistedFactory>>
        +frame(): FrameElement
    }
    class FrameUpdater {
        <<@Singleton>>
        -FrameContainer parent
        -FrameFactory factory
        -Observable~Render~ render
        +initialize()
    }
    class ProgressElement {
        <<@Singleton>>
        -LinearProgressElementBuilder bar
        -HTMLDivElement label
        -update(value: Progress)
    }

    FrameContainer <|.. ContentElement
    FrameUpdater --> FrameContainer
    FrameUpdater --> FrameFactory
    FrameFactory ..> FrameElement

    class MenuApi {
        <<@Singleton>>
        -FetchApi fetchApi
        -Observer~Progress~ progress
        +findAll(): Observable~List~Menu~~
    }
    class UserApi {
        <<@Singleton>>
        -FetchApi fetchApi
        -Observer~Progress~ progress
        -int REFRESH_INTERVAL = 600000$
        +find(): Observable~User~
        -periodicRefresh(user)
        -refresh(): Observable~Void~
    }
    class BrowserLanguageDetector {
        <<@Singleton>>
        +detect(): String «JSNI»
    }
    class FetchLanguagePackRepository {
        <<@Singleton>>
        -FetchApi fetchApi
        +load(lang): Observable~Labels~
    }

    MenuApi ..|> MenuRepository
    UserApi ..|> UserRepository
    BrowserLanguageDetector ..|> LanguageDetector
    FetchLanguagePackRepository ..|> LanguagePackRepository
```

## 조합 (DI)

```mermaid
classDiagram
    class Application {
        +onModuleLoad()
    }
    class Component {
        <<@Singleton, @dagger.Component>>
        modules: Module, ApiModule, I18nModule, HostSharedModule
        +shellInitializer(): ShellInitializer
    }
    class ShellInitializer {
        <<@Singleton>>
        -HistoryManager
        -UrlBasedMenuResolver
        -ToolBasedMenuResolver
        -FrameUpdater
        -ModuleScriptManager
        -ProgressElement
        -ContentElement
        -SessionPollingService
        +initialize()
    }
    class SessionPollingService {
        <<@Singleton>>
        -FetchApi fetchApi
        -ToastContainer toastContainer
        -LabelProvider labelProvider
        -int CHECK_INTERVAL_MS = 60000$
        -int WARNING_BEFORE_EXPIRY_MS = 300000$
        -double REFRESH_THRESHOLD = 0.8$
        +initialize()
        -checkSession()
        -refreshToken()
        -redirectToLogin()
        -getTokenExpiry(): double «JSNI»
        -getTokenDuration(): double «JSNI»
    }
    class ThemeToggle {
        <<@Singleton>>
        -LabelProvider labelProvider
        -boolean darkMode
        -HTMLElement headlineEl
        -Labels currentLabels
        -toggle()
        -updateHeadline()
        -applyTheme()
        -createThemeSvg(): SVGElement
    }
    NavigationRailItemElement <|-- ThemeToggle
    ShellInitializer --> SessionPollingService
    class HostSharedModule {
        <<@Module>>
        +uri(): BehaviorSubject~String~$
        +render(): BehaviorSubject~Render~$
        +progress(): BehaviorSubject~Progress~$
    }
    class WindowProgressBridge {
        <<agent-bridge>>
        +register(NextFn)$
        +next(Object)$
    }
    class WindowUriBridge {
        <<agent-bridge>>
        +register(NextFn)$
        +next(String)$
    }
    class WindowLabelBridge {
        <<agent-bridge>>
        +publish(Labels)$
        +subscribe(NextFn)$
    }

    Application --> Component : DaggerComponent.create()
    Component --> ShellInitializer
    ShellInitializer --> WindowProgressBridge : register(progress)
    ShellInitializer --> WindowUriBridge : register(uri)
    ShellInitializer --> WindowLabelBridge : publish(labels)
```

## 디자인 패턴

| 패턴 | 적용 클래스 | 설명 |
|------|------------|------|
| **Observer** | `DrawerMode`, `MenuSelected`, `ToolSelected`, `MenuRailMode`, `ToolRailMode` | BehaviorSubject 기반 반응형 상태 관리. 메뉴 선택 → 도구 목록 갱신 → Drawer 축소 → Rail 상태 전환이 연쇄적으로 전파. |
| **State** | `DrawerMode`(EXPAND/COLLAPSE/HIDE), `MenuRailMode`, `ToolRailMode` | 3단 상태(EXPAND/COLLAPSE/HIDE) 전환을 관리하며, 입력(메뉴 선택, 도구 개수, 사용자 상태)에 따라 자동 전이. |
| **Factory** | `MenuRailItemFactory`, `ToolRailItemFactory` (@AssistedFactory) | Dagger Assisted Injection으로 Menu/Tool 도메인 객체를 받아 NavigationRailItemElement 서브클래스를 생성. |
| **Template Method** | `NavigationRailElement` (인터페이스) | expand/collapse/hide 동작을 정의하여 `MenuRailElement`, `ToolRailElement`가 공통 행위를 상속. |
| **Strategy** | `UrlBasedMenuResolver`, `ToolBasedMenuResolver` | 메뉴 자동 선택 전략 2가지: URL 정규식 매칭 vs 도구→부모 메뉴 역추적. 각각 독립적으로 구독하여 동작. |
| **Adapter** | `MenuApi` → `MenuRepository`, `UserApi` → `UserRepository` | API 호출을 Repository 인터페이스로 래핑. FetchApi를 통해 HTTP 세부사항을 숨김. |
| **Mediator** | `ToolExecutionManager` | 도구 선택 이벤트를 받아 ToolFunction 실행을 중재. DOM 준비 상태 확인 후 100ms 재시도 로직 포함. |

## 모바일 지원

```mermaid
classDiagram
    class ViewportObserver {
        -BehaviorSubject~Boolean~ isMobile
        +isMobile(): Observable~Boolean~
        -onResize()
    }

    class DrawerState {
        <<enumeration>>
        EXPAND
        COLLAPSE
        HIDE
        OVERLAY
    }

    class MenuRailState {
        <<enumeration>>
        EXPAND
        COLLAPSE
        HIDE
        BOTTOM_NAV
    }

    class ToolRailState {
        <<enumeration>>
        EXPAND
        COLLAPSE
        HIDE
        HORIZONTAL_CHIPS
    }

    ViewportObserver --> DrawerMode : OVERLAY 전환
    DrawerMode --> DrawerState
    MenuRailMode --> MenuRailState
    ToolRailMode --> ToolRailState
```

| 클래스 | 모바일 동작 |
|--------|-----------|
| `ViewportObserver` | `window.matchMedia('(max-width: 768px)')` 감지 → isMobile BehaviorSubject 발행 |
| `DrawerMode` | 모바일: OVERLAY 상태 추가 (배경 딤 + position: fixed + 메뉴 선택 시 자동 HIDE) |
| `MenuRailMode` | 모바일: BOTTOM_NAV 상태 추가 (하단 네비게이션 바, 최대 5개 메뉴) |
| `ToolRailMode` | 모바일: HORIZONTAL_CHIPS 상태 추가 (수평 스크롤 칩 바) |
| `DrawerElement` | 모바일: 스와이프 열기/닫기 (touchstart → touchmove → touchend) |
