# 공통 설계 패턴

프론트엔드 UI 모듈(document-ui, type-ui, shell-ui)에서 반복되는 핵심 설계 패턴을 정리한다.

---

## Command 패턴 (Action & ActionManager)

`ui-components` 모듈에 정의. 모든 편집 작업은 `Action` 인터페이스(`execute()`, `rollback()`)로 캡슐화된다. `ActionManager`가 Undo/Redo 스택을 관리한다.

- `Action`: `ui-components/domain/Action.java`
- `ActionManager`: `ui-components/client/components/ActionManager.java`

```mermaid
classDiagram
    class Action {
        <<interface>>
        +execute()
        +rollback()
    }
    class ActionManager {
        -LinkedList~Action~ undoStack
        -LinkedList~Action~ redoStack
        -int MAX_STACK_SIZE = 100
        +execute(action: Action)
        +undo()
        +redo()
        +canUndo(): BehaviorSubject~Boolean~
        +canRedo(): BehaviorSubject~Boolean~
        +clear()
    }
    class ComplexAction {
        -List~Action~ actions
        +execute(): 순서대로 실행
        +rollback(): 역순 되돌림
    }
    ActionManager --> Action
    ComplexAction ..|> Action
```

### 규칙

- 스택 최대 100개. 새 액션 실행 시 redo 스택 초기화.
- `BehaviorSubject<Boolean>`으로 canUndo/canRedo 상태를 버튼/메뉴에 전파.
- `ComplexAction`으로 여러 Action을 원자적으로 묶는다 (예: 이동 + 충돌 해소).
- 에이전트 편집도 동일한 Action 경로로 실행되므로 사용자가 Ctrl+Z로 되돌릴 수 있다.

### 적용 모듈

| 모듈 | Action 종류 |
|------|------------|
| **document-ui** | AddDocumentAction, EditDocumentAction, DeleteDocumentAction, SaveAction |
| **type-ui** | CreateBoxAction, DeleteBoxAction, EditBoxAction, MoveBoxAction, ResizeBoxAction, PushOutOverlapAction, ChangeLayoutAction, LoadAction, SaveAction |

---

## 더티 트래킹 & 원자적 저장

`ChangeTracker`(`ui-components/client/components/ChangeTracker.java`)가 키 기반 변경 상태를 추적한다. 사용자 편집은 로컬에만 반영되며, Save 버튼을 누르면 모든 변경점이 하나의 트랜잭션으로 원자적 저장된다.

### 핵심 규칙

1. 편집 → 로컬 더티 상태만 변경 (서버 미반영)
2. Save → created+changed는 PUT, deleted는 DELETE로 원자적 전송
3. Undo로 원본 상태 복원 → 더티 플래그 자동 해제
4. Save 성공 → Undo/Redo 스택 + 더티 상태 초기화
5. 부분 실패 → 실패 항목만 더티 유지, 토스트 알림

### 시각적 상태 (MD3 토큰 기반)

| 상태 | CSS 클래스 | 시각적 표현 | 의미 |
|------|-----------|------------|------|
| **생성** | `.created` | `tertiary-container` 배경, 좌측 3px `tertiary` 보더 | 로컬에서 새로 추가 |
| **수정** | `.changed` | `tertiary` 1px inset box-shadow | 원본 대비 값 변경 |
| **삭제 예정** | `.deleted` | 취소선, 텍스트 75% 투명화 | Save 시 서버에서 삭제 |
| **유효** | `.valid` | `primary` 텍스트 색상 | 서버 검증 통과 |
| **유효하지 않음** | `.invalid` | `error` 텍스트 + `error` 1px inset shadow | 필수값 누락/형식 오류 |
| **충돌** | `.conflict` | `secondary-container` 배경, `secondary` 2px 보더 | 다른 사용자와 동시 수정 |

### Save 버튼 UX

- 더티 없으면 **비활성화** (`disabled`)
- 더티 있으면 변경 건수 뱃지 표시 (예: `Save (3)`)
- 저장 중 **스피너** 표시, 중복 클릭 방지
- 탭/레이아웃 전환 시 미저장 변경 있으면 **확인 다이얼로그**

### 적용 모듈

| 모듈 | 트래커 | 상태 모델 |
|------|--------|----------|
| **document-ui** | `DirtyTracker` | `created(Set)`, `changed(Map)`, `deleted(Set)` |
| **type-ui** | `ChangeTracker` | `NOT_CHANGED` / `CHANGED` / `DELETED` (enum) |

---

## 반응형 상태 관리 (BehaviorSubject)

모든 상태는 RxJava `BehaviorSubject` 기반으로, 변경 시 구독자에게 자동 전파된다.

### 패턴

```
@Singleton
public class SomeState {
    private final BehaviorSubject<T> subject = BehaviorSubject.createDefault(initialValue);
    public void next(T value) { subject.onNext(value); }
    public T getValue() { return subject.getValue(); }
    public Observable<T> asObservable() { return subject; }
}
```

- UI 컴포넌트가 `asObservable()`을 구독하여 상태 변경 시 자동 렌더링
- `getValue()`로 현재 스냅샷 조회
- Dagger `@Singleton`으로 모듈 내 공유

---

## 프레즌스 (다른 사용자 편집 표시)

같은 워크스페이스에서 다른 사용자가 편집 중인 요소를 실시간으로 표시한다.

- **API**: `POST /workspace/{id}/presence`
- **디바운스**: 200ms (빠른 이동 시 이벤트 억제)
- **타임아웃**: 30초 (갱신 없으면 자동 해제)
- **시각**: 사용자별 고유 색상 2px 보더 + 이름 라벨 (3초 후 fade-out)

| 모듈 | 페이로드 |
|------|---------|
| **document-ui** | `{user, type, serial, field}` |
| **type-ui** | `{user, typeKey}` |

---

## 에이전트 연동 (WindowMutationBridge)

`agent-bridge` 모듈의 `WindowMutationBridge`가 CustomEvent 기반으로 GWT 모듈 간 통신을 중개한다.

### 패턴

1. 에이전트 → `MutateCommand.changes[]` → Kafka → SSE → `CustomEvent('handbook-mutate')`
2. 각 모듈의 `MutationReceiver` → `AgentHandler` → 명령 파싱 → `ActionManager.execute(Action)`
3. 동일한 Action/Undo 스택 사용 → 사용자가 에이전트 작업을 Ctrl+Z로 되돌릴 수 있음

### 상태 조회

각 모듈은 `StateProvider.snapshot()` → JSON으로 현재 상태를 에이전트에 제공한다.

---

## MD3 네이티브 컴포넌트 (sayaya-ui)

네이티브 HTML 요소 대신 `sayaya-ui` 라이브러리의 MD3 빌더 패턴 컴포넌트를 사용하여 디자인 시스템 일관성을 유지한다.

### 마이그레이션 매핑

| 네이티브 HTML | MD3 컴포넌트 (sayaya-ui) | 용례 |
|-------------|------------------------|------|
| `<select>` | `SelectElementBuilder.select().outlined()` | type-ui: Array/Map 서브 타입 드롭다운, shell-ui: 워크스페이스 선택 |
| `<input type="checkbox">` | `CheckboxElementBuilder.checkbox()` | type-ui: 그리드 스냅 토글 |
| `<input type="radio">` | `RadioElementBuilder.radio()` | 선택 옵션 그룹 |
| `<input type="text">` | `TextFieldElementBuilder.textField().outlined()` | 모든 텍스트 입력 필드 |
| `<button>` | `ButtonElementBuilder.button().filled()/.outlined()/.text()` | 모든 버튼 |

### 규칙

- 새로운 폼 요소 추가 시 반드시 `sayaya-ui` 빌더를 사용한다.
- 기존 네이티브 요소는 발견 시 MD3 빌더로 마이그레이션한다.
- 빌더 체인: `Builder.create().variant().label().css().element()` 형태로 구성한다.

---

## 재귀 서브 에디터 (ValidatorEditorFactory)

type-ui의 속성 편집에서 Array/Map 타입의 서브 타입 에디터를 재귀적으로 생성하는 패턴이다.

### 구조

- `ValidatorEditorFactory`가 타입 이름으로 적절한 `ValidatorEditor`를 동적 생성한다.
- Array/Map 에디터는 한 단계 깊은 `nested()` 팩토리를 받아 서브 타입 드롭다운 변경 시 재귀적으로 서브 에디터를 생성한다.
- 최대 깊이 3단계(`MAX_DEPTH=3`)로 무한 재귀를 방지한다. 깊이 초과 시 array/map 옵션이 드롭다운에서 제외된다.

### 시각적 계층 (CSS 중첩)

| 깊이 | 좌측 보더 색상 | 배경 |
|------|-------------|------|
| 1단계 | `--md-sys-color-outline-variant` | `surface-container` 50% |
| 2단계 | `--md-sys-color-primary` | `primary-container` 30% |
| 3단계 | `--md-sys-color-tertiary` | `tertiary-container` 30% |

### 적용 모듈

| 모듈 | 사용 위치 |
|------|----------|
| **type-ui** | `AttributeEditorDialog` → `ValidatorEditorFactory` → `ArrayValidatorEditor` / `MapValidatorEditor` |

---

## CQRS with External Search Engine (PostgreSQL + ES)

데이터의 원천 저장(Source of Truth)과 고성능 검색(Search)을 분리하는 CQRS 패턴을 구현한다.

### 아키텍처 구조

```mermaid
flowchart LR
    C["Command (Write)"] --> PG[("PostgreSQL<br/>(Source of Truth)")]
    PG -->|Kafka Event| S["search-document<br/>(Indexer)"]
    S --> ES[("Elasticsearch<br/>(Read Model)")]
    Q["Query (Read)"] --> ES
```

### 핵심 원칙

1. **데이터 저장 (Write)**: 모든 문서와 타입의 CUD 작업은 PostgreSQL을 통해 수행되며, 트랜잭션과 무결성을 보장한다.
2. **이벤트 발행**: 변경 완료 시 Kafka를 통해 도메인 이벤트(`DOCUMENT_CREATED`, `DOCUMENT_DELETED` 등)를 발행한다.
3. **데이터 동기화 (Sync)**: `search-document` 서비스가 이벤트를 수신하여 Elasticsearch 인덱스를 갱신한다. 이는 비동기로 처리되며 최종 일관성(Eventual Consistency)을 따른다.
4. **검색 및 조회 (Read)**: 사용자의 검색 요청 및 대량 목록 조회는 Elasticsearch를 통해 처리한다. PostgreSQL은 단건 상세 조회 및 이력 추적용으로만 사용한다.

### 장점

- **성능**: 복합 필터링 및 전문 검색(Full-text Search) 성능을 극대화한다.
- **확장성**: 읽기와 쓰기 부하를 독립적으로 분리하여 확장할 수 있다.
- **유연성**: 검색 인덱스 구조를 도메인 모델과 다르게 최적화하여 구성할 수 있다.
