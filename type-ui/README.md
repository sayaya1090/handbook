# Type-UI 모듈

캔버스 기반 타입 스키마 편집기 (GWT). 타입을 드래그 가능한 카드로 시각화하고, 속성 추가/편집, 참조 관계 시각화, Undo/Redo를 지원한다.
Shell이 `ModuleScriptManager`로 `js/type/type.nocache.js`를 동적 로딩하여 실행한다.

## Mount 패턴

`Application.onModuleLoad()` 은 `WindowRenderBridge.next(render)` 로 shell
`FrameUpdater` 에 Render 위임. body 직접 append 금지 —
[`docs/contracts/frame.md`](../../docs/contracts/frame.md).

---

## 캔버스

타입 카드를 배치하는 메인 편집 영역. 20px 격자 배경 위에 타입 박스들이 `position: absolute`로 배치된다.

### 편집 모드

`CanvasMode`로 세 가지 모드를 전환한다. 툴바의 모드 토글 버튼으로 변경한다.

| 모드 | 설명 | 활성 기능 |
|------|------|-----------|
| **VIEW** | 조회 전용 | 모든 편집 비활성 |
| **LAYOUT** | 레이아웃 편집 (기본값) | 드래그 이동, 리사이즈, 격자 스냅 |
| **TYPE** | 타입 편집 | 이름/버전 더블클릭 편집, 속성 클릭 편집/삭제 |

키보드 단축키(Ctrl+Z, Delete, 화살표 키)와 컨텍스트 메뉴, 멀티셀렉트는 VIEW를 제외한 모든 모드에서 동작한다.

### 드래그 & 드롭 (LAYOUT 모드)

타입 박스를 마우스로 드래그하여 이동한다.

1. **mousedown** -- 드래그 시작. `DragShapeElement`가 선택된 모든 박스의 고스트(점선 테두리 + 반투명 배경)를 생성한다.
2. **mousemove** -- 고스트가 마우스 델타만큼 이동한다. 격자 스냅이 켜져 있으면 20px 단위로 정렬된다.
3. **mouseup** -- 고스트를 숨기고, `MoveBoxAction` + `PushOutOverlapAction`을 복합 실행한다. 실제 박스가 최종 위치로 이동하고, 겹치는 박스가 자동으로 밀려난다.
4. **텍스트 셀렉션 방지** -- mousedown에서 `preventDefault()`, 캔버스에 `user-select: none`.

### 리사이즈 (LAYOUT 모드)

타입 박스 우하단 모서리에 리사이즈 핸들이 표시된다. 드래그로 박스 크기를 조절하며, 최소 크기(120x60)를 보장한다. 격자 스냅 적용. Undo 지원.

### 인라인 편집 (TYPE 모드)

- **타입 이름**: 헤더의 이름 영역을 더블클릭하면 input으로 전환. Enter로 확정, Esc로 취소.
- **버전**: 헤더의 버전 배지를 더블클릭하면 input으로 전환. 동일 UX.
- **속성 편집**: 속성 행을 클릭하면 AttributeEditorDialog가 열린다.
- **속성 삭제**: 속성 행에 마우스를 올리면 x 버튼이 표시된다. 클릭 시 즉시 삭제.

모든 인라인 편집은 `EditBoxAction`으로 캡슐화되어 Undo/Redo를 지원한다.

### 키보드 단축키

| 키 | 동작 |
|----|------|
| `Ctrl+Z` | Undo |
| `Ctrl+Shift+Z` | Redo |
| `Delete` / `Backspace` | 선택된 타입 삭제 |
| `Arrow Up/Down/Left/Right` | 선택된 박스를 5px 이동 (스냅 활성 시 20px) |
| `Shift+Arrow` | 20px 단위 이동 |

### 컨텍스트 메뉴

- **캔버스 빈 영역 우클릭**: Add Type, Undo, Redo, Reload. Undo/Redo는 가능 여부에 따라 비활성화 처리된다.
- **타입 박스 우클릭**: Add Attribute (속성 편집 다이얼로그 열림), Delete.

### 충돌 해소 (PushOutOverlapAction)

타입 추가/이동 후 겹치는 박스를 자동으로 밀어낸다.

- **BFS 큐 기반 연쇄 처리**: A를 밀면 B와 겹침 -> B도 밀고, B가 C와 겹치면 C도 처리.
- **최소 이동 방향 선택**: 상하좌우 4방향 중 이동량이 가장 적은 방향으로 밀어낸다.
- `padding` 파라미터로 박스 간 최소 여백(10px)을 보장한다.

### 격자 스냅

툴바의 **Snap 체크박스**로 on/off 전환한다. 활성화 시:

- 드래그 드롭: 고스트와 최종 위치가 20px 격자에 정렬된다.
- 리사이즈: 크기가 20px 단위로 정렬된다.
- 화살표 키 이동: 이동량이 20px로 고정된다.
- 캔버스 배경 격자(20px)와 시각적으로 일치한다.

---

## 타입 박스 (TypeElement)

각 타입을 MD3 Card 스타일로 표시한다.

- **헤더**: 타입 이름(좌측) + 버전 배지(우측). TYPE 모드에서 더블클릭으로 인라인 편집.
- **속성 목록**: `ValueListElement` -> `ValueElement` 행. 각 행에 속성 이름(좌)과 타입(우, monospace), 삭제 버튼을 표시한다.
- **선택 상태**: `[selected]` attribute로 파란 테두리 + 그림자. Ctrl+Click으로 멀티셀렉트.
- **표시 모드**: `SIMPLE`(이름만) / `DETAIL`(속성 포함) 전환. `setDisplayMode()`로 제어한다.
- **리사이즈 핸들**: 우하단 모서리에 드래그 가능한 핸들. LAYOUT 모드에서만 활성.
- **위치 반응**: `PositionMap`을 구독하여 position 변경 시 자동으로 `left/top/width/min-height` 갱신.
- **Dagger @AssistedFactory**: `BoxElementFactory.create(TypeValue, Position)`으로 생성.

---

## 참조 화살표 (BoxReferenceElement)

Document 타입 속성이 다른 타입을 참조할 때 SVG 화살표를 그린다.

- **자동 갱신**: `TypeList`와 `PositionMap`을 구독하여 참조 관계나 위치 변경 시 즉시 다시 그린다.
- **곡선 경로**: `ArrowFactory`가 두 박스의 최단 테두리 좌표를 계산하고, 수평/수직 우세 방향에 따라 Cubic Bezier 곡선을 생성한다.
- **화살표 머리**: 곡선 접선 방향(`approachAngle`)에 맞춰 회전된 삼각형. 선분 끝은 화살표 밑변에 맞춰 단축된다.
- **스타일**: 실선, 1.5px, 메인 색상(`--md-sys-color-primary`).

---

## 속성 편집 (AttributeEditorDialog)

타입 박스 우클릭 -> "Add Attribute" 또는 TYPE 모드에서 속성 행 클릭으로 다이얼로그를 연다.

- **이름 필드**: 속성 이름 입력.
- **타입 셀렉터**: 9종 버튼 (text, number, date, enum, bool, array, map, file, document). 선택 시 `[selected]` 스타일 적용.
- **Validator 에디터**: `ValidatorEditorFactory`가 선택한 타입에 따라 하위 에디터를 동적 생성한다.
  - **Text**: Regex 패턴 (여러 줄, 줄바꿈 구분)
  - **Number**: Min / Max 범위
  - **Date**: After / Before 날짜 범위
  - **Enum**: 허용 값 목록 (한 줄에 하나)
  - **Array**: MD3 Select(`SelectElementBuilder.select().outlined()`)로 원소 타입을 선택하면 해당 타입의 서브 에디터가 재귀적으로 표시된다. 예: `Array<Number(0~100)>`, `Array<Map<Text, Date>>`
  - **Map**: 키 타입과 값 타입을 각각 MD3 Select 드롭다운으로 선택하고, 각각의 서브 에디터가 재귀적으로 표시된다. 예: `Map<Text(^[A-Z]+$), Number(0~100)>`
  - **File**: 허용 확장자 목록
  - **Document**: 참조할 타입 이름
  - **재귀 깊이 제한**: `ValidatorEditorFactory`가 최대 3단계까지 중첩을 허용한다. 깊이 초과 시 array/map 옵션이 드롭다운에서 자동 제외된다.
  - **시각적 계층**: 서브 에디터는 깊이별로 좌측 보더 색상과 배경이 차별화된다 (outline-variant → primary → tertiary).
- **설명 필드**: 속성 설명.
- **Apply**: `EditBoxAction`으로 타입에 속성을 추가/수정한다. Undo 지원.
- **i18n**: 모든 레이블이 `LabelProvider`를 통해 다국어 처리된다.

---

## 컨트롤러 (ControllerElement)

캔버스 상단 툴바. 모바일 환경에서는 화면 영역 확보를 위해 플로팅 컨트롤로 전환된다. 각 버튼은 독립적인 `@Singleton` 컴포넌트로 Dagger에서 주입된다.

| 그룹 | 버튼 / 요소 | 동작 | 모바일 UI |
|------|------|------|------|
| 모드 전환 | `ModeToggleButton` | LAYOUT/TYPE 모드 전환 | `settings-dial` 내부 |
| 속성 정보 | `TypePropertyBar`, `NewVersionButton` | ID, 버전, 기간 표시 및 **새 버전 생성** | 상단바 / 플로팅 툴바 |
| 기간 이동 | `BeforeButton`, `AfterButton`, **Period Label** | 레이아웃 기간 전환 및 현재 기간 표시 | `type-floating-pill` (상단 캡슐) |
| 타입 CRUD | `AddTypeButton`, `RemoveTypeButton` | 타입 추가/삭제 | 좌측 툴레일 / 플로팅 툴바 |
| 히스토리 | `UndoButton`, `RedoButton` | Undo/Redo | `action-dial` 내부 |
| 저장 | `SaveButton`, `ReloadButton` | 서버 저장 / 다시 로드 | `action-dial` 내부 |
| 벌크 삭제 | `BulkDeleteButton` | 다중 선택 타입 일괄 삭제 | 툴레일 또는 상황별 노출 |
| 스냅 | `SnapButton` | 격자 스냅 on/off 토글 | `settings-dial` 내부 |

### 모바일 플로팅 컨트롤 (Speed Dial) 및 동적 재배치

`StatusHeaderElement`는 화면 너비에 따라 DOM을 재배치(Dynamic Reparenting)하는 코디네이터 역할을 수행한다. 데스크톱에서는 상단바에 일렬로 배치되던 버튼들이 모바일에서는 플로팅 다이얼 요소로 그룹화되어 이동한다.

- **상단 캡슐 (`.type-floating-pill`)**: 기간 탐색 및 정보 표시.
- **액션 다이얼 (`ActionDialElement`)**: 저장, 되돌리기 등 실행형 액션 그룹.
- **설정 다이얼 (`SettingsDialElement`)**: 편집 모드, 스냅 등 환경 설정 그룹.

---

## 더티 트래킹 & Undo/Redo

`ChangeTracker`로 타입별 `NOT_CHANGED` / `CHANGED` / `DELETED` 상태를 추적하고, `ActionManager`로 Undo/Redo를 지원한다. Save 시 원자적 저장.

> 공통 패턴 상세는 [설계 패턴](../docs/design-patterns.md) 참조.

### Action 목록

| Action | 역할 |
|--------|------|
| `CreateBoxAction` | TypeList에 타입 추가 + PositionMap에 위치 등록 + ChangeTracker CHANGED |
| `DeleteBoxAction` | TypeList에서 제거 + ChangeTracker DELETED 마킹 |
| `EditBoxAction` | 타입 메타데이터/속성 변경 (before → after) + ChangeTracker CHANGED |
| `MoveBoxAction` | 선택된 박스들을 dx, dy만큼 이동 |
| `ResizeBoxAction` | 박스 크기 변경 |
| `PushOutOverlapAction` | BFS 연쇄 충돌 해소 |
| `ChangeLayoutAction` | 레이아웃 기간 전환 (undo 지원) |
| `ComplexAction` | 복합 액션 (이동 + 충돌 해소 등) |
| `LoadAction` | 서버에서 레이아웃, 타입, 위치를 로드. 스택 초기화. |
| `SaveAction` | CHANGED → PUT, DELETED → DELETE 원자적 저장 + 위치 저장. 스택 초기화. |

---

## 버전 히스토리 (VersionHistoryPanel)

특정 타입의 모든 버전을 타임라인으로 표시하고, 두 버전 간 diff 비교를 지원한다.

- **버전 목록 조회**: `TypeRepository.versions(typeId)` → type-query `GET /workspaces/{id}/types/{typeId}/versions`
- **diff 비교**: 두 버전을 클릭하면 diff API를 호출하여 속성 추가/삭제/변경 사항을 시각적으로 표시
- **Escape/닫기**: Escape 키 또는 닫기 버튼으로 패널을 닫을 수 있다

---

## 에이전트 연동

에이전트가 현재 캔버스에서 편집 중인 타입 데이터를 읽고, 직접 Action을 실행할 수 있다. 에이전트 편집도 사용자 편집과 동일한 Action/ChangeTracker 경로를 타며, Undo/Redo 가능하다.

### 상태 조회 (TypeStateProvider)

`StateProvider` 인터페이스 구현. 현재 캔버스의 타입/속성 정보를 JSON으로 반환한다. 에이전트가 이 정보를 기반으로 지능적인 mutation을 생성할 수 있다.

### Mutation 수신 (AgentMutationHandler)

`MutationReceiver`를 구독하여 에이전트의 `MutateCommand` changes 문자열을 Action으로 변환하고 `ActionManager`에서 실행한다. 에이전트 작업도 Ctrl+Z로 되돌릴 수 있다.

| 명령어 | 예시 | 동작 |
|--------|------|------|
| `CREATE type:<id>` | `CREATE type:customer` | 새 타입 생성 + 충돌 해소 |
| `DELETE type:<key>` | `DELETE type:customer:1.0` | 타입 삭제 |
| `ADD field:<key>:<name>:type=<type>` | `ADD field:customer:1.0:phone:type=text` | 속성 추가 |
| `REMOVE field:<key>:<name>` | `REMOVE field:customer:1.0:phone` | 속성 삭제 |
| `SET type:<key>:<prop>=<value>` | `SET type:customer:1.0:description=고객` | 타입 속성 변경 |

### 연동 흐름

```
사용자: "customer에 phone 속성 추가해줘"
  -> 에이전트: TypeStateProvider.snapshot()으로 현재 상태 조회
  -> LLM: changes 배열 생성
  -> MutateCommand -> MutationReceiver -> AgentMutationHandler
  -> ActionManager.execute(EditBoxAction) -> 캔버스 즉시 반영
  -> 사용자: Ctrl+Z로 에이전트 작업 되돌리기 가능
```

---

## 상태 관리

> BehaviorSubject 패턴 상세는 [설계 패턴](../docs/design-patterns.md#반응형-상태-관리-behaviorsubject) 참조.

| 클래스 | 역할 |
|--------|------|
| `TypeList` | 현재 로딩된 전체 타입 목록 |
| `LayoutList` / `LayoutProvider` | 레이아웃 기간 목록 / 현재 선택 기간 |
| `PositionMap` | 타입별 캔버스 좌표 |
| `TypeDataCoordinator` | 레이아웃 전환 시 자동 데이터 로딩 관리 |
| `ChangeTracker` | NOT_CHANGED / CHANGED / DELETED 추적 |
| `CanvasMode` | VIEW / LAYOUT / TYPE 편집 모드 |
| `GridSnap` | 격자 스냅 on/off |
| `SelectedBoxElement` | 현재 선택된 타입 key 집합 |
| `PeriodRecalculationService` | 타입 변경 시 유효기간 자동 재계산 |

---

## API 연동

`TypeRepository`와 `LayoutRepository` 포트를 통해 백엔드(`type-command`)와 통신한다.

| 포트 메서드 | HTTP | 설명 |
|------------|------|------|
| `TypeRepository.list(period)` | `GET /workspaces/{id}/types` | 기간별 타입 조회 |
| `TypeRepository.save(types)` | `PUT /workspaces/{id}/types` | 타입 저장 (신규) |
| `TypeRepository.patch(patches)` | `PATCH /workspaces/{id}/types` | 타입 부분 업데이트 (변경 속성만) |
| `TypeRepository.delete(types)` | `DELETE /workspaces/{id}/types` | 삭제된 타입 제거 |
| `LayoutRepository.layouts()` | `GET /workspaces/{id}/layouts` | 레이아웃 기간 목록 |
| `LayoutRepository.positions(period)` | `GET /workspaces/{id}/layouts/{period}` | 기간별 위치 조회 |
| `LayoutRepository.savePositions(...)` | `PUT /workspaces/{id}/layouts` | 위치 저장 |

---

## 데이터 로딩 전략 (Incremental Loading)

Type-UI는 대규모 워크스페이스에서도 부드러운 탐색을 제공하기 위해 **반응형 점진적 로딩** 방식을 채택한다.

1.  **지연 로딩 (Lazy Loading)**: 초기 로딩 시 전체 데이터를 가져오지 않고, 현재 선택된 레이아웃 기간에 필요한 데이터만 서버에서 조회한다.
2.  **자동 동기화 (TypeDataCoordinator)**: 사용자가 기간 이동(Before/After) 버튼을 누르면 `TypeDataCoordinator`가 이를 감지하여 해당 기간의 데이터를 백엔드에서 비동기로 추가 로드한다.
3.  **상태 통합 (Merge Strategy)**: 새로 로드된 데이터는 기존 메모리에 있는 `TypeList` 및 `PositionMap`과 병합(Merge)된다. 이를 통해 이미 방문한 기간으로 다시 돌아갈 때는 네트워크 요청 없이 즉시 렌더링이 가능하다.
4.  **캐시 무결성**: 서버 저장(Save) 또는 새로고침(Reload) 시에는 로컬 캐시를 초기화하여 최신 상태를 유지한다.
5.  **워크스페이스 격리**: 워크스페이스 전환 시 `LoadAction`이 실행되며, 이때 기존의 모든 타입 및 위치 정보(`TypeList`, `PositionMap`)를 명시적으로 비운 후(Clear) 새 워크스페이스의 데이터를 로드하여 데이터 혼선을 방지한다.

---

## 모바일 터치 지원

캔버스는 모바일/태블릿 환경에서 터치 입력을 지원한다.

- **터치 드래그**: `TouchEventAdapter`가 touchstart/touchmove/touchend를 마우스 이벤트로 변환하여 드래그 & 드롭, 리사이즈를 터치로 동작시킨다.
- **핀치 줌**: `PinchZoomHandler`가 두 손가락 핀치 제스처로 캔버스 줌을 조절한다 (0.5x ~ 3.0x).
- **롱프레스**: 500ms 롱프레스 시 컨텍스트 메뉴를 트리거한다. 이동 거리 10px 이상이면 롱프레스가 취소된다.
- **리사이즈 핸들**: 터치 영역이 44px 이상으로 확대되어 모바일에서도 정확한 조작이 가능하다.
- **속성 편집 다이얼로그**: 모바일에서 전체 화면 bottom sheet로 전환된다.
- **컨트롤러 툴바**: CSS flex-wrap으로 좁은 화면에서 자동 줄바꿈된다.

---

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.

---

## 실행

```bash
./gradlew :type-ui:gwtDev    # DevMode
./gradlew :type-ui:test      # 테스트
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
