# Document-UI 클래스 다이어그램

## Domain 계층

```mermaid
classDiagram
    class Action {
        <<interface>>
        +execute()
        +rollback()
    }

    class DocumentValue {
        <<JsType native>>
        +id: String
        +type: String
        +serial: String
        +effectDateTime: double
        +expireDateTime: double
        +createDateTime: double
        +creator: String
        +data: JsPropertyMap~String~
    }

    class TypeInfo {
        <<JsType native>>
        +id: String
        +attributes: AttributeInfo[]
    }

    class AttributeInfo {
        <<JsType native>>
        +name: String
        +type: String
        +nullable: boolean
    }

    class ColumnDef {
        +name: String
        +type: String
        +width: int
        +readOnly: boolean
        +source: String[]
        +serial()$: ColumnDef
        +effectDateTime()$: ColumnDef
        +expireDateTime()$: ColumnDef
        +fromAttribute(attr, typeNames)$: ColumnDef
    }

    TypeInfo --> AttributeInfo
```

## Usecase 계층 — 상태 관리

```mermaid
classDiagram
    class DocumentList {
        -BehaviorSubject~List~DocumentValue~~ subject
        +next(docs: List~DocumentValue~)
        +getValue(): List~DocumentValue~
        +asObservable(): Observable
    }

    class TypeProvider {
        -BehaviorSubject~TypeInfo~ subject
        +next(type: TypeInfo)
        +getValue(): TypeInfo
        +asObservable(): Observable
    }

    class TypeList {
        -BehaviorSubject~List~TypeInfo~~ subject
        +next(types: List~TypeInfo~)
        +getValue(): List~TypeInfo~
        +asObservable(): Observable
    }

    class PageState {
        -BehaviorSubject~Search~ subject
        +next(search: Search)
        +getValue(): Search
        +asObservable(): Observable
    }
```

## Usecase 계층 — Action & ActionManager & DirtyTracker

```mermaid
classDiagram
    class ActionManager {
        -LinkedList~Action~ undoStack
        -LinkedList~Action~ redoStack
        -int MAX_STACK_SIZE
        +execute(action: Action)
        +undo()
        +redo()
        +canUndo(): BehaviorSubject~Boolean~
        +canRedo(): BehaviorSubject~Boolean~
        +clear()
    }

    class DirtyTracker {
        -Set~String~ created
        -Map~String, Map~String, Object[]~~ changed
        -Set~String~ deleted
        +markCreated(serial: String)
        +markChanged(serial: String, field: String, before: Object, after: Object)
        +markDeleted(serial: String)
        +unmarkCreated(serial: String)
        +unmarkChanged(serial: String, field: String)
        +unmarkDeleted(serial: String)
        +hasDirty(): boolean
        +summary(): DirtySummary
        +reset()
    }

    class DirtySummary {
        +created: int
        +changed: int
        +deleted: int
    }

    class AddDocumentAction {
        -DocumentList documentList
        -DirtyTracker dirtyTracker
        -DocumentValue newDoc
        +execute()
        +rollback()
    }

    class EditDocumentAction {
        -DocumentList documentList
        -DirtyTracker dirtyTracker
        -DocumentValue before
        -DocumentValue after
        +execute()
        +rollback()
    }

    class DeleteDocumentAction {
        -DocumentList documentList
        -DirtyTracker dirtyTracker
        -List~DocumentValue~ deleted
        +execute()
        +rollback()
    }

    class SaveAction {
        -DocumentRepository repo
        -DocumentList documentList
        -DirtyTracker dirtyTracker
        -ActionManager actionManager
        +execute()
        +rollback()
    }

    ActionManager --> Action
    ActionManager --> DirtyTracker
    DirtyTracker --> DirtySummary
    AddDocumentAction ..|> Action
    EditDocumentAction ..|> Action
    DeleteDocumentAction ..|> Action
    SaveAction ..|> Action
```

## Usecase 계층 — 포트

```mermaid
classDiagram
    class DocumentRepository {
        <<interface>>
        +search(param: Search): Observable~Page~
        +save(docs: List~DocumentValue~): Observable~Void~
        +delete(docs: List~DocumentValue~): Observable~Void~
    }

    class TypeRepository {
        <<interface>>
        +list(): Observable~List~TypeInfo~~
    }
```

## Interfaces 계층 — API

```mermaid
classDiagram
    class DocumentApi {
        -FetchApi fetchApi
        +search(param): Observable~Page~
        +save(docs): Observable~Void~
        +delete(docs): Observable~Void~
    }

    class TypeApi {
        -FetchApi fetchApi
        +list(): Observable~List~TypeInfo~~
    }

    class DocumentNative {
        <<JsType native>>
        +toValue(): DocumentValue
        +fromValue(doc: DocumentValue)$ DocumentNative
    }

    class ApiModule {
        <<Dagger Module>>
        +provideDocumentRepository(api: DocumentApi): DocumentRepository
        +provideTypeRepository(api: TypeApi): TypeRepository
    }

    DocumentApi ..|> DocumentRepository
    TypeApi ..|> TypeRepository
    DocumentApi --> DocumentNative
```

## Interfaces 계층 — UI 컴포넌트

```mermaid
classDiagram
    class SpreadsheetElement {
        -Handsontable instance
        -ColumnDef[] columns
        +render(data: List~DocumentValue~)
        +updateColumns(columns: ColumnDef[])
        +getSelected(): List~int~
    }

    class ColumnFactory {
        +create(type: TypeInfo): ColumnDef[]$
        +create(type: TypeInfo, allTypes: List~TypeInfo~): ColumnDef[]$
    }

    class DataProvider {
        +toArray(docs: List~DocumentValue~, columns: ColumnDef[]): Object[][]
        +fromArray(data: Object[][], columns: ColumnDef[]): List~DocumentValue~
    }

    class TypeTabsElement {
        -TypeList typeList
        -TypeProvider typeProvider
        +render()
    }

    class ControllerElement {
        +element(): HTMLElement
    }

    class AddButton {
        -ActionManager actionManager
        -DocumentList documentList
    }

    class DeleteButton {
        -ActionManager actionManager
        -DocumentList documentList
        -SpreadsheetElement spreadsheet
    }

    class SaveButton {
        -ActionManager actionManager
    }

    class UndoButton {
        -ActionManager actionManager
    }

    class RedoButton {
        -ActionManager actionManager
    }

    class PaginationElement {
        -PageState pageState
        +render(totalElements: int)
    }

    class SelectedRows {
        -BehaviorSubject~Set~Integer~~ subject
        +toggle(rowIndex: int)
        +clear()
        +getValue(): Set~Integer~
        +asObservable(): Observable~Set~Integer~~
        +subscribe(consumer: Consumer)
    }

    class BulkDeleteButton {
        -ActionManager actionManager
        -DocumentList documentList
        -SelectedRows selectedRows
        -LabelProvider labelProvider
    }

    class BulkStatusButton {
        -ActionManager actionManager
        -DocumentList documentList
        -SelectedRows selectedRows
        -LabelProvider labelProvider
        -HTMLSelectElement select (DRAFT/REVIEW/PUBLISHED)
    }

    ControllerElement --> TypeTabsElement
    ControllerElement --> AddButton
    ControllerElement --> DeleteButton
    ControllerElement --> SaveButton
    ControllerElement --> UndoButton
    ControllerElement --> RedoButton
    ControllerElement --> PaginationElement
    ControllerElement --> BulkDeleteButton
    ControllerElement --> BulkStatusButton
    BulkDeleteButton --> SelectedRows
    BulkStatusButton --> SelectedRows
    SpreadsheetElement --> ColumnFactory
    SpreadsheetElement --> DataProvider
```

## 에이전트 연동

```mermaid
classDiagram
    class AgentDocumentHandler {
        -TypeProvider typeProvider
        -ActionManager actionManager
        -DocumentList documentList
        -MutationReceiver mutationReceiver
        +init()
        -processChange(changes: String[])
    }

    class DocumentStateProvider {
        -DocumentList documentList
        -TypeProvider typeProvider
        +snapshot(): String
    }

    class MutationReceiver {
        <<interface>>
        +mutations(): Observable~String[]~
    }

    class StateProvider {
        <<interface>>
        +snapshot(): String
    }

    AgentDocumentHandler --> MutationReceiver
    AgentDocumentHandler --> ActionManager
    DocumentStateProvider ..|> StateProvider
    DocumentStateProvider --> DocumentList
```

## DI 조합

```mermaid
classDiagram
    class Application {
        +onModuleLoad()
    }

    class Component {
        <<Dagger Component>>
        +initializer(): DocumentInitializer
    }

    class DocumentModule {
        <<Dagger Module>>
        +provideDocumentList(): DocumentList
        +provideTypeList(): TypeList
        +provideTypeProvider(): TypeProvider
        +providePageState(): PageState
        +provideActionManager(): ActionManager
        +provideMutationReceiver(): MutationReceiver
    }

    class DocumentInitializer {
        -TypeRepository typeRepo
        -DocumentRepository docRepo
        -SpreadsheetElement spreadsheet
        -ControllerElement controller
        -AgentDocumentHandler agentHandler
        +init()
    }

    Application --> Component
    Component --> DocumentModule
    Component --> ApiModule
    Component --> DocumentInitializer
```

## 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **Command** | Action, ActionManager | 모든 편집을 Action으로 캡슐화하여 Undo/Redo 지원 |
| **Observer (BehaviorSubject)** | DocumentList, TypeProvider, PageState | 상태 변경 시 자동 전파 |
| **Port & Adapter** | DocumentRepository, TypeRepository | usecase 포트를 API 어댑터가 구현 |
| **Factory** | ColumnFactory | TypeInfo의 속성 + TypeList(전체 타입 목록)을 기반으로 Handsontable 컬럼 정의로 변환. document 속성은 타입 이름 드롭다운 제공 |
| **Adapter** | DataProvider | DocumentValue ↔ Handsontable 2D 배열 변환 |
| **Facade** | DocumentInitializer | 초기화 로직을 하나의 진입점으로 통합 |
| **Bridge (WindowMutationBridge)** | AgentDocumentHandler | GWT 모듈 간 CustomEvent 기반 통신 |

## 모바일 지원

```mermaid
classDiagram
    class ViewportObserver {
        -BehaviorSubject~Boolean~ isMobile
        -BehaviorSubject~Boolean~ isCompact
        +isMobile(): Observable~Boolean~
        +isCompact(): Observable~Boolean~
        Note: mobile < 768px, compact < 480px
    }

    class CardViewElement {
        -DocumentList documentList
        +render(docs: List~DocumentValue~)
        Note: compact 모드에서 스프레드시트 대체
    }

    class SpreadsheetElement {
        +setMobileMode(boolean mobile)
        Note: fixedColumnsLeft=1 serial 고정
    }

    class TypeTabsElement {
        Note: overflow-x auto 수평 스크롤
    }

    class ControllerElement {
        Note: flex-wrap 줄바꿈
    }

    ViewportObserver --> SpreadsheetElement
    ViewportObserver --> CardViewElement
    ViewportObserver --> ControllerElement
```

| 클래스 | 모바일 동작 |
|--------|-----------|
| `ViewportObserver` | matchMedia 감지. mobile(<768px), compact(<480px) 두 단계 |
| `SpreadsheetElement` | 모바일: fixedColumnsLeft=1 (serial 고정) + 수평 스크롤 |
| `CardViewElement` | compact 모드 전용. 문서별 카드 리스트 표시. 스프레드시트 숨김 |
| `TypeTabsElement` | overflow-x: auto 수평 스크롤 탭 바 |
| `ControllerElement` | flex-wrap으로 줄바꿈. 핵심 버튼(Save, Add)만 1행 표시 |
| `PaginationElement` | 모바일: 이전/다음 버튼만 표시, 페이지 번호 생략 |
