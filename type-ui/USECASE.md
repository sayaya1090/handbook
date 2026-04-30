# Type-UI 유스케이스

## 툴바 분리 레이아웃

기존 상단 컨트롤러가 '상단 상태바'와 '좌측 툴레일'로 분리되어 캔버스 작업 영역을 최대화합니다.

```mermaid
graph TD
    subgraph UI_Layout
        Header[상단 상태바: 워크스페이스/타입 정보/상태]
        ToolRail[좌측 툴레일: 툴 선택/액션 버튼]
        Canvas[캔버스 영역: 타입 카드/관계선]
    end
    
    Header --- Canvas
    ToolRail --- Canvas
```

## 동적 도구 연동 시퀀스 (Dynamic Tool Integration)

캔버스의 상태(모드, 선택 여부 등)에 따라 적절한 도구들을 쉘의 툴레일에 동적으로 노출하고 이벤트를 처리합니다.

```mermaid
sequenceDiagram
    participant C as Type-UI
    participant TP as ToolProvider (activity)
    participant B as WindowBridge (agent-bridge)
    participant S as Shell (shell-ui)

    Note over C: "캔버스 초기화 또는 상태 변경"
    C->>TP: "publish([Add, Undo, Redo, ...])"
    TP->>B: "ToolPublisher.publish(tools)"
    B->>S: "window.dispatchEvent(published)"
    S->>S: "Tool Rail UI 갱신"

    Note over S: "사용자가 'Undo' 도구 클릭"
    S->>B: "ToolSubscriber.select('undo')"
    B->>TP: "이벤트 수신"
    TP->>C: "onSelect 핸들러 트리거"
    C->>C: "ActionManager.undo() 실행"
```

## 타입 조회 (초기 로딩 및 전환) 시퀀스

```mermaid
sequenceDiagram
    participant Shell as Shell (WorkspaceSelect)
    participant Bridge as WindowWorkspaceEventBridge
    participant App as Application
    participant LA as LayoutApi
    participant LL as LayoutList
    participant LP as LayoutProvider
    participant TA as TypeApi
    participant TL as TypeList
    participant PM as PositionMap
    participant PRS as PeriodRecalculationService
    participant Canvas as CanvasElement

    Shell->>Bridge: "publishWorkspace(id)"
    Note over Bridge: "CustomEvent('handbook-workspace-context')"
    
    App->>Bridge: "receiver().workspaceId().subscribe()"
    Bridge-->>App: "workspaceId 발행"
    
    App->>TA: "setWorkspace(id)"
    App->>LA: "setWorkspace(id)"
    
    Note over App: "LoadAction 실행"
    App->>LA: "layouts()"
    LA-->>LL: "LayoutPeriod[] 발행"
    LP->>LP: "selectBestMatch(periods)"
    App->>TA: "list(selectedPeriod)"
    TA-->>TL: "Set<TypeValue> 발행"
    App->>LA: "positions(selectedPeriod)"
    LA-->>PM: "Map<String,Position> 발행"
    
    TL-->>PRS: "타입 변경 감지"
    PRS->>PRS: "기간 경계 재계산"
    TL-->>Canvas: "syncElements() → 카드 렌더링"
```

## 드래그 & 드롭 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Canvas as CanvasElement
    participant Drag as DragShapeElement
    participant Snap as GridSnap
    participant AM as ActionManager
    participant PM as PositionMap

    User->>Canvas: "mousedown (LAYOUT 모드)"
    Canvas->>Drag: "show(선택된 박스들)"
    loop "mousemove"
        User->>Canvas: "마우스 이동"
        Canvas->>Snap: "snapDelta(dx, dy)"
        Snap-->>Canvas: "정렬된 delta"
        Canvas->>Drag: "move(delta)"
    end
    User->>Canvas: "mouseup"
    Canvas->>Drag: "drop()"
    Canvas->>AM: "execute(ComplexAction)"
    Note over AM: "MoveTBoxAction + PushOutOverlapAction"
    AM->>PM: "위치 업데이트"
    PM-->>Canvas: "박스 이동 + 겹침 해소"
```

## 속성 편집 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant TBox as TypeElement
    participant Menu as TBoxContextMenuElement
    participant Dialog as AttributeEditorDialog
    participant VE as ValidatorEditor
    participant AM as ActionManager
    participant TL as TypeList

    alt "컨텍스트 메뉴로 추가"
        User->>TBox: "우클릭"
        TBox->>Menu: "show(x, y, typeKey)"
        User->>Menu: "'Add Attribute' 클릭"
        Menu->>Dialog: "show(null, onApply)"
    else "기존 속성 편집 (TYPE 모드)"
        User->>TBox: "속성 행 클릭"
        TBox->>Dialog: "show(attribute, onApply)"
    end

    Dialog-->>Dialog: "이름/타입/설명 입력"
    User->>Dialog: "타입 버튼 선택 (예: number)"
    Dialog->>VE: "NumberValidatorEditor 활성화"
    VE-->>VE: "min/max 입력"
    User->>Dialog: "Apply 클릭"
    Dialog->>VE: "collect() → AttributeTypeValue"
    Dialog->>AM: "execute(EditTBoxAction)"
    AM->>TL: "타입 업데이트"
    TL-->>TBox: "속성 목록 다시 그리기"
```

## 타입 생성 → 저장 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Canvas as CanvasElement
    participant AM as ActionManager
    participant TL as TypeList
    participant PM as PositionMap
    participant CT as ChangeTracker
    participant API as TypeApi/LayoutApi

    User->>Canvas: "Add Type 클릭"
    Canvas->>AM: "execute(ComplexAction)"
    AM->>TL: "CreateTBoxAction.execute()"
    TL-->>Canvas: "타입 카드 렌더링"
    AM->>PM: "위치 등록"
    AM->>PM: "PushOutOverlapAction (겹침 해소)"
    AM->>CT: "CHANGED 마킹"

    User->>Canvas: "속성 편집 (더블클릭)"
    Canvas->>AM: "execute(EditTBoxAction)"
    AM->>TL: "타입 업데이트"
    TL-->>Canvas: "카드 다시 그리기"

    User->>Canvas: "Save 버튼 클릭"
    Canvas->>AM: "execute(SaveAction)"
    AM->>API: "PUT /workspaces/{id}/types"
    AM->>API: "PUT /workspaces/{id}/layouts"
    AM->>CT: "초기화"
    AM->>AM: "스택 초기화"
```

## 에이전트 타입 조작 시퀀스

에이전트는 브릿지를 통해 캔버스 상태를 변경하는 액션을 직접 주입한다.

```mermaid
sequenceDiagram
    participant Agent as "에이전트 (SSE)"
    participant MR as MutationReceiver
    participant AMH as AgentMutationHandler
    participant AM as ActionManager
    participant TL as TypeList
    participant Canvas as CanvasElement

    Note over Agent,MR: "AgentMutation (CustomEvent)"
    Agent->>MR: "MutateCommand.changes[]"
    MR->>AMH: "mutations 구독"
    AMH->>AMH: "명령 파싱 (CREATE/DELETE/ADD/REMOVE/SET)"
    AMH->>AM: "execute(Action)"
    AM->>TL: "타입 추가/수정/삭제"
    TL-->>Canvas: "캔버스 즉시 반영"

    actor User as 사용자
    User->>AM: "Ctrl+Z (Undo)"
    AM->>TL: "Action.rollback()"
    TL-->>Canvas: "에이전트 작업 되돌림"
```

## UC-T11: 에이전트에 의한 타입 조작

| 항목 | 내용 |
|------|------|
| **액터** | AI 에이전트 |
| **선행조건** | type-ui 모듈 로딩 완료, MutationReceiver 브릿지 연결 |
| **정상 흐름** | 1. 에이전트가 `TypeStateProvider.snapshot()`으로 현재 캔버스 상태를 JSON으로 조회한다.<br>2. LLM이 MutateCommand의 changes 배열을 생성한다.<br>3. `MutationReceiver`를 통해 `AgentMutationHandler`에 전달된다.<br>4. 핸들러가 changes 문자열을 파싱하여 `CreateTBoxAction`, `EditTBoxAction`, `DeleteTBoxAction` 등으로 변환한다.<br>5. `ActionManager`에서 실행되어 캔버스에 즉시 반영된다. |
| **지원 명령** | CREATE type, DELETE type, ADD field, REMOVE field, SET type |
| **브릿지** | `agent-bridge` 모듈의 `AgentMutation`가 CustomEvent로 연결. |

## UC-T21: 에이전트 작업의 Undo/Redo

| 항목 | 내용 |
|------|------|
| **액터** | 사용자, AI 에이전트 |
| **선행조건** | 에이전트가 타입 생성 명령(CREATE)을 실행한 직후 |
| **정상 흐름** | 1. 에이전트가 `AgentMutation`를 통해 CREATE 명령으로 새 타입을 생성한다.<br>2. `AgentMutationHandler`가 `CreateTBoxAction`을 `ActionManager`에서 실행하여 캔버스에 박스가 추가된다.<br>3. 사용자가 즉시 Ctrl+Z를 누른다.<br>4. `ActionManager.undo()`가 실행되어 생성이 되돌려 지고, 박스 수가 원래대로 복원된다. |
| **결과** | 에이전트가 생성한 타입도 사용자의 Undo 스택에 포함되어 즉시 되돌릴 수 있다. |

## 트레이서빌리티 매트릭스

| UC | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---|---|---|
| UC-T2 (타입생성) | 타입 생성 → 저장 | CreateTBoxAction, ContextMenuHelper, TypeList, PositionMap, ChangeTracker | CanvasTest: 타입 카드 생성 및 캔버스 렌더링 확인 |
| UC-T9 (Undo) | 에이전트 타입 조작 | ActionManager, UndoButton, RedoButton | CanvasTest: Undo/Redo 기능 검증 |
| UC-T11 (에이전트) | 에이전트 타입 조작 | AgentMutationHandler, TypeStateProvider, MutationReceiver, ActionManager, AgentMutation | CollaborationTest: 에이전트 조작 검증 |
| UC-T24 (이력조회) | — | VersionHistoryPanel, type-query versions API | ✅ 구현 완료 (VersionHistoryPanel) |
