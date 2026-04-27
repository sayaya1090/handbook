# Type-UI 클래스 다이어그램

## 도메인

```mermaid
classDiagram
    class Action {
        <<interface>>
        +execute()
        +rollback()
    }
    class TypeValue {
        <<@JsType native>>
        +String id
        +String version
        +double effectDateTime
        +double expireDateTime
        +String description
        +boolean primitive
        +String parent
        +AttributeValue[] attributes
        +key(): String <<@JsOverlay>>
        +create(id, version, effect, expire): TypeValue$
        +withAttributes(attrs): TypeValue <<@JsOverlay>>
        +withDescription(desc): TypeValue <<@JsOverlay>>
    }
    class AttributeValue {
        <<@JsType native>>
        +String name
        +int order
        +String description
        +AttributeTypeValue type
        +boolean nullable
        +boolean inherited
        +of(name, order, type): AttributeValue$
        +withName(name): AttributeValue <<@JsOverlay>>
        +withType(type): AttributeValue <<@JsOverlay>>
        +withNullable(n): AttributeValue <<@JsOverlay>>
    }
    class AttributeTypeValue {
        <<@JsType native>>
        +String type
        +String[] regexPatterns
        +Double min, max
        +String[] allowedValues
        +String referencedType
        +AttributeTypeValue elementType
        +AttributeTypeValue keyType
        +AttributeTypeValue valueType
        +text(): AttributeTypeValue$
        +number(min, max): AttributeTypeValue$
        +document(ref): AttributeTypeValue$
        +array(elementType): AttributeTypeValue$
        +map(keyType, valueType): AttributeTypeValue$
        +simplify(): String <<@JsOverlay>>
    }
    class Position {
        <<@JsType native>>
        +int x, y, width, height
        +of(x, y, w, h): Position$
        +move(dx, dy): Position <<@JsOverlay>>
        +resize(w, h): Position <<@JsOverlay>>
    }
    class LayoutPeriod {
        <<@JsType native>>
        +double effectDateTime
        +double expireDateTime
        +of(effect, expire): LayoutPeriod$
        +overlap(other): double <<@JsOverlay>>
    }

    TypeValue *-- AttributeValue
    AttributeValue *-- AttributeTypeValue
```

## 상태 관리 (Usecase)

```mermaid
classDiagram
    class TypeList {
        <<@Singleton>>
        -BehaviorSubject~Set~TypeValue~~ subject
        +getValue(): Set~TypeValue~
        +replace(types: Set~TypeValue~)
        +add(type: TypeValue)
        +remove(type: TypeValue)
        +update(before, after: TypeValue)
    }
    class PositionMap {
        <<@Singleton>>
        -BehaviorSubject~Map~String,Position~~ subject
        +getValue(): Map~String,Position~
        +get(typeKey): Position
        +put(typeKey, position)
        +move(typeKey, dx, dy)
        +replace(map)
    }
    class LayoutList {
        <<@Singleton>>
        -BehaviorSubject~List~LayoutPeriod~~ subject
        +getValue(): List~LayoutPeriod~
        +next(periods)
    }
    class LayoutProvider {
        <<@Singleton>>
        -BehaviorSubject~LayoutPeriod~ subject
        +getValue(): LayoutPeriod
        +next(period)
        +selectBestMatch(periods)
    }
    class ChangeTracker {
        <<@Singleton>>
        -Map~String,ChangeState~ states
        +getState(typeKey): ChangeState
        +markChanged(typeKey)
        +markDeleted(typeKey)
        +unmark(typeKey)
        +reset()
        +hasChanges(): boolean
        +getChangedKeys(): Set~String~
        +getDeletedKeys(): Set~String~
    }
    class CanvasMode {
        <<@Singleton>>
        -BehaviorSubject~Mode~ subject
        +setMode(mode)
        +isEditable(): boolean
        +isLayoutMode(): boolean
        +isTypeMode(): boolean
    }
    class GridSnap {
        <<@Singleton>>
        -boolean enabled
        +GRID_SIZE: int = 20$
        +snap(value): int
        +snapDelta(currentPos, delta): int
    }
    class ActionManager {
        <<@Singleton>>
        -LinkedList~Action~ undoStack
        -LinkedList~Action~ redoStack
        -BehaviorSubject~Boolean~ canUndo
        -BehaviorSubject~Boolean~ canRedo
        +execute(action: Action)
        +undo()
        +redo()
        +clear()
    }
    class SelectedBoxElement {
        <<@Singleton>>
        -BehaviorSubject~Set~String~~ subject
        +select(typeKey)
        +toggle(typeKey)
        +clear()
        +isSelected(typeKey): boolean
    }
    class PeriodRecalculationService {
        <<@Singleton>>
        +PeriodRecalculationService(TypeList, LayoutList, LayoutProvider)
    }

    PeriodRecalculationService --> TypeList : 구독
    PeriodRecalculationService --> LayoutList : 갱신
    PeriodRecalculationService --> LayoutProvider : best match 선택

    class ChangeState {
        <<enum>>
        NOT_CHANGED
        CHANGED
        DELETED
    }
    ChangeTracker --> ChangeState
    CanvasStateContext --> CanvasState
    CanvasState <|.. ViewState
    CanvasState <|.. LayoutState
    CanvasState <|.. TypeState
```

## Action 계층

```mermaid
classDiagram
    class Action {
        <<interface>>
        +execute()
        +rollback()
    }
    class CreateBoxAction {
        -TypeList typeList
        -PositionMap positionMap
        -ChangeTracker tracker
        -TypeValue type
        -Position position
    }
    class DeleteBoxAction {
        -TypeList typeList
        -ChangeTracker tracker
        -TypeValue type
        -ChangeState previousState
    }
    class EditBoxAction {
        -TypeList typeList
        -ChangeTracker tracker
        -TypeValue before
        -TypeValue after
    }
    class MoveBoxAction {
        -PositionMap positionMap
        -Set~String~ typeKeys
        -int dx, dy
    }
    class ResizeBoxAction {
        -PositionMap positionMap
        -String typeKey
        -Position before, after
    }
    class PushOutOverlapAction {
        -PositionMap positionMap
        -String sourceKey
        -int padding
        -Map~String,Position~ originalPositions
        +calculate(): Map~String,Position~
    }
    class ComplexAction {
        -List~Action~ actions
        +execute() 순서대로
        +rollback() 역순
    }
    class ChangeLayoutAction {
        -LayoutProvider provider
        -LayoutPeriod before, after
    }
    class LoadAction {
        -TypeRepository, LayoutRepository
        -TypeList, PositionMap, ChangeTracker
        -ActionManager, LayoutProvider, LayoutList
    }
    class SaveAction {
        -TypeRepository, LayoutRepository
        -TypeList, PositionMap, ChangeTracker
        -ActionManager, LayoutProvider
    }

    Action <|.. CreateBoxAction
    Action <|.. DeleteBoxAction
    Action <|.. EditBoxAction
    Action <|.. MoveBoxAction
    Action <|.. ResizeBoxAction
    Action <|.. PushOutOverlapAction
    Action <|.. ComplexAction
    Action <|.. ChangeLayoutAction
    Action <|.. LoadAction
    Action <|.. SaveAction
    ComplexAction *-- Action : actions[]
    ActionManager --> Action : execute/undo/redo
```

## 화살표 (Arrow)

```mermaid
classDiagram
    class Point {
        <<record>>
        +int x
        +int y
    }
    class Rectangle {
        <<record>>
        +int x, y, width, height
        +center(): Point
        +right(): int
        +bottom(): int
    }
    class Arrow {
        <<record>>
        +Point from
        +Point to
        +double approachAngle
        +String svgPath
    }
    class ArrowFactory {
        -int ARROW_HEAD_LENGTH = 10$
        +create(from: Rectangle, to: Rectangle): Arrow$
    }
    ArrowFactory ..> Arrow : creates
    Arrow --> Point
```

## 캔버스 + UI 컴포넌트

```mermaid
classDiagram
    class CanvasElement {
        <<@Singleton>>
        -Map~String,TypeElement~ elementMap
        -BoxElementFactory factory
        -DragShapeElement dragShape
        -BoxReferenceElement boxReference
        -CanvasContextMenuElement canvasMenu
        -BoxContextMenuElement boxMenu
        +syncElements(types: Set~TypeValue~)
        -initBoxHandlers(elem: TypeElement)
        -handleKeyDown(e: KeyboardEvent)
    }
    class EditorContext {
        <<@Singleton>>
        +ChangeTracker changeTracker
        +TypeList typeList
        +CanvasMode canvasMode
        +ActionManager actionManager
    }
    class StatusHeaderElement {
        <<@Singleton>>
        -EditorContext context
        -HTMLDivElement workspaceInfo
        -HTMLDivElement typeInfo
        -HTMLDivElement status
        +update(typeCount, status)
    }
    class ToolRailElement {
        <<@Singleton>>
        -HTMLDivElement rail
        -Map~String,Tool~ tools
        +addTool(tool: Tool)
        +selectTool(toolId)
    }
    class Tool {
        <<record>>
        +String id
        +String icon
        +Runnable action
        +boolean selected
    }
    class TypeElement {
        <<@AssistedInject>>
        -TypeValue type
        -Position position
        -HTMLDivElement nameLabel, versionLabel
        -ValueListElement valueList
        +applyPosition()
        +setType(type)
        +startInlineEdit()
        +startVersionEdit()
        +initResizeHandle()
    }
    class BoxElementFactory {
        <<@AssistedFactory>>
        +create(type, position): TypeElement
    }
    class DragShapeElement {
        <<@Singleton>>
        -Map~String,HTMLDivElement~ ghosts
        -int dragOriginX, dragOriginY
        -boolean active
        -Consumer~int[]~ dropHandler
        +show(mouseX, mouseY)
        +move(x, y)
        +drop(x, y)
        +onDrop(handler)
    }
    class BoxReferenceElement {
        <<@Singleton>>
        +redraw()
        -createArrowHead(arrow: Arrow)
    }
    class CanvasContextMenuElement {
        <<@Singleton>>
        +show(x, y)
        +hide()
        +addTypeAt(x, y)
    }
    class BoxContextMenuElement {
        <<@Singleton>>
        +show(x, y, typeKey)
        +hide()
        +addAttribute()
        +deleteTarget()
    }
    class ContextMenuHelper {
        <<utility>>
        +menuItem(text: String): HTMLDivElement$
        +uniqueTypeId(typeList: TypeList): String$
    }
    class ValueListElement {
        -List~ValueElement~ elements
        -Consumer~AttributeValue~ onEdit, onDelete
        +update(attributes: AttributeValue[])
    }
    class ValueElement {
        -AttributeValue attribute
        +ValueElement(attr, onEdit, onDelete)
    }

    CanvasElement *-- TypeElement : elementMap
    CanvasElement --> BoxElementFactory : creates
    CanvasElement --> DragShapeElement : 드래그 고스트
    CanvasElement --> BoxReferenceElement : SVG 화살표
    CanvasElement --> CanvasContextMenuElement
    CanvasElement --> BoxContextMenuElement
    CanvasElement --> StatusHeaderElement
    CanvasElement --> ToolRailElement
    StatusHeaderElement --> EditorContext
    ToolRailElement *-- Tool
    BoxElementFactory ..> TypeElement : creates
    TypeElement *-- ValueListElement
    ValueListElement *-- ValueElement
    BoxContextMenuElement --> AttributeEditorDialog
    CanvasContextMenuElement --> ContextMenuHelper : uniqueTypeId
    BoxContextMenuElement --> ContextMenuHelper
```

## 속성 편집 다이얼로그

```mermaid
classDiagram
    class AttributeEditorDialog {
        <<@Singleton>>
        -TextFieldElementBuilder nameField, descField
        -Map~String,HTMLElement~ typeButtons
        -Map~String,ValidatorEditor~ validatorEditors
        -Consumer~AttributeValue~ onApply
        -String selectedType
        +show(attribute, onApply)
        +hide()
        -selectType(type)
        -apply()
    }
    class ValidatorEditor {
        <<interface>>
        +load(value: AttributeTypeValue)
        +collect(): AttributeTypeValue
        +element(): HTMLElement
    }
    class ValidatorEditorFactory {
        -TypeList typeList
        -int depth
        -int MAX_DEPTH = 3$
        +ValidatorEditorFactory(typeList: TypeList)
        +create(type: String): ValidatorEditor
        +isMaxDepth(): boolean
        -nested(): ValidatorEditorFactory
    }
    class TextValidatorEditor {
        -TextFieldElementBuilder regexField
    }
    class NumberValidatorEditor {
        -TextFieldElementBuilder minField, maxField
    }
    class DateValidatorEditor {
        -TextFieldElementBuilder afterField, beforeField
    }
    class EnumValidatorEditor {
        -TextFieldElementBuilder valuesField
    }
    class FileValidatorEditor {
        -TextFieldElementBuilder extField
    }
    class DocumentValidatorEditor {
        -TextFieldElementBuilder refField
        -TypeList typeList
    }
    class ArrayValidatorEditor {
        -SelectElementBuilder typeSelect
        -HTMLDivElement subEditorContainer
        -ValidatorEditorFactory factory
        -ValidatorEditor currentSubEditor
        +load(value: AttributeTypeValue)
        +collect(): AttributeTypeValue
        -onTypeChanged()
    }
    class MapValidatorEditor {
        -SelectElementBuilder keySelect, valueSelect
        -HTMLDivElement keySubEditorContainer, valueSubEditorContainer
        -ValidatorEditorFactory factory
        -ValidatorEditor keySubEditor, valueSubEditor
        +load(value: AttributeTypeValue)
        +collect(): AttributeTypeValue
        -onKeyTypeChanged()
        -onValueTypeChanged()
    }

    ValidatorEditor <|.. TextValidatorEditor
    ValidatorEditor <|.. NumberValidatorEditor
    ValidatorEditor <|.. DateValidatorEditor
    ValidatorEditor <|.. EnumValidatorEditor
    ValidatorEditor <|.. FileValidatorEditor
    ValidatorEditor <|.. DocumentValidatorEditor
    ValidatorEditor <|.. ArrayValidatorEditor
    ValidatorEditor <|.. MapValidatorEditor
    ArrayValidatorEditor --> ValidatorEditorFactory : 서브 에디터 생성
    MapValidatorEditor --> ValidatorEditorFactory : 서브 에디터 생성
    ValidatorEditorFactory ..> ValidatorEditor : creates
    AttributeEditorDialog --> ValidatorEditorFactory : 최상위 팩토리 생성
    AttributeEditorDialog *-- ValidatorEditor : 타입별 에디터
```

## API 어댑터

```mermaid
classDiagram
    class TypeRepository {
        <<interface>>
        +list(period): Observable~Set~TypeValue~~
        +save(types): Observable~Set~TypeValue~~
        +delete(types): Observable~Void~
    }
    class LayoutRepository {
        <<interface>>
        +layouts(): Observable~List~LayoutPeriod~~
        +positions(period): Observable~Map~String,Position~~
        +savePositions(period, positions): Observable~Void~
    }
    class TypeApi {
        <<@Singleton>>
        -FetchApi fetchApi
        -Observer~Progress~ progress
        -String workspace
        +setWorkspace(workspace)
    }
    class LayoutApi {
        <<@Singleton>>
        -FetchApi fetchApi
        -String workspace
        +setWorkspace(workspace)
    }
    class TypeNative {
        <<@JsType native>>
        +toDomain(): TypeValue
        +fromDomain(type): TypeNative$
    }
    class AttributeNative {
        <<@JsType native>>
        +toDomain(): AttributeValue
        +fromDomain(attr): AttributeNative$
    }
    class LayoutNative {
        <<@JsType native>>
        +toPeriod(): LayoutPeriod
        +toPositionMap(): Map~String,Position~
    }

    TypeRepository <|.. TypeApi
    LayoutRepository <|.. LayoutApi
    TypeApi --> TypeNative : JSON 변환
    TypeApi --> AttributeNative : JSON 변환
    LayoutApi --> LayoutNative : JSON 변환
```

## 에이전트 연동

```mermaid
classDiagram
    class AgentMutationHandler {
        <<@Singleton>>
        -ActionManager actionManager
        -TypeList typeList
        -PositionMap positionMap
        -ChangeTracker changeTracker
        -LayoutProvider layoutProvider
        +AgentMutationHandler(..., MutationReceiver)
        -processChange(change: String)
        -createType(id: String)
        -deleteType(key: String)
        -addField(key, name, typeStr)
        -removeField(key, name)
        -setProperty(key, propValue)
    }
    class TypeStateProvider {
        <<@Singleton>>
        -TypeList typeList
        +snapshot(): String
    }
    class TypeSearchProvider {
        <<@Singleton>>
        -TypeList typeList
        +search(query): Observable~String~
        -matches(type, query): boolean
    }

    AgentMutationHandler --> ActionManager : execute(Action)
    AgentMutationHandler --> TypeList
    AgentMutationHandler --> PositionMap
    TypeStateProvider --> TypeList : JSON 변환
    TypeSearchProvider --> TypeList : 필터링
```
rovider --> TypeList : 필터링
```
ean
    }

    AgentMutationHandler --> ActionManager : execute(Action)
    AgentMutationHandler --> TypeList
    AgentMutationHandler --> PositionMap
    TypeStateProvider --> TypeList : JSON 변환
    TypeSearchProvider --> TypeList : 필터링
```
