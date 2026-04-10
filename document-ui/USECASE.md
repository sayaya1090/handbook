# Document-UI 유스케이스

## 초기 로딩 시퀀스

```mermaid
sequenceDiagram
    participant Shell as Shell (ModuleScriptManager)
    participant App as Application
    participant TA as TypeApi
    participant TL as TypeList
    participant TP as TypeProvider
    participant DA as DocumentApi
    participant DL as DocumentList
    participant CF as ColumnFactory
    participant Sheet as SpreadsheetElement

    Shell->>App: js/data.nocache.js 로딩
    App->>App: DaggerComponent.create()
    App->>App: WindowStateProviderBridge.register()
    App->>TA: list()
    TA-->>TL: List<TypeInfo> 발행
    TL-->>TP: 첫 번째 타입 자동 선택
    TP-->>CF: 속성 → 컬럼 정의 변환
    CF-->>Sheet: Handsontable 컬럼 설정
    TP-->>DA: search(workspace, {type, page:0, limit:50})
    DA-->>DL: List<DocumentValue> 발행
    DL-->>Sheet: 데이터 렌더링
```

## 문서 편집 → 저장 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Sheet as SpreadsheetElement
    participant AM as ActionManager
    participant DL as DocumentList
    participant DA as DocumentApi

    User->>Sheet: 셀 값 변경
    Sheet->>Sheet: afterChange 이벤트 캡처
    Sheet->>AM: execute(EditDocumentAction(before, after))
    AM->>DL: 문서 업데이트

    User->>Sheet: 새 행 추가 (Add 버튼)
    Sheet->>AM: execute(AddDocumentAction)
    AM->>DL: 빈 문서 추가
    DL-->>Sheet: 행 추가 렌더링

    User->>Sheet: Save 버튼 클릭
    Sheet->>AM: execute(SaveAction)
    AM->>DA: PUT /workspace/{id}/documents (변경된 문서)
    AM->>DA: DELETE /workspace/{id}/documents (삭제된 문서)
    DA-->>AM: 저장 완료
    AM->>AM: 스택 초기화
```

## 타입 탭 전환 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Tabs as TypeTabsElement
    participant TP as TypeProvider
    participant CF as ColumnFactory
    participant Sheet as SpreadsheetElement
    participant DA as DocumentApi
    participant DL as DocumentList
    participant PS as PageState

    User->>Tabs: "order" 탭 클릭
    Tabs->>TP: next(orderType)
    TP-->>CF: order 타입의 속성 → 컬럼 재생성
    CF-->>Sheet: updateColumns(newColumns)
    TP-->>PS: 페이지 초기화 (page:0)
    PS-->>DA: search(workspace, {type:"order", page:0, limit:50})
    DA-->>DL: List<DocumentValue> 발행
    DL-->>Sheet: 데이터 갱신
```

## 에이전트 문서 조작 시퀀스

```mermaid
sequenceDiagram
    actor Agent as 에이전트
    participant MR as MutationReceiver
    participant ADH as AgentDocumentHandler
    participant AM as ActionManager
    participant DL as DocumentList
    participant Sheet as SpreadsheetElement

    Note over Agent,MR: WindowMutationBridge (CustomEvent)

    alt 타입 선택
        Agent->>MR: ["DOC_SELECT customer"]
        MR->>ADH: processChange()
        ADH->>ADH: TypeProvider.next(customerType)
    else 문서 추가 + 편집
        Agent->>MR: ["DOC_ADD"]
        MR->>ADH: processChange()
        ADH->>AM: execute(AddDocumentAction)
        Agent->>MR: ["DOC_EDIT CUST-001 name 홍길동"]
        MR->>ADH: processChange()
        ADH->>AM: execute(EditDocumentAction)
        AM->>DL: 문서 업데이트
        DL-->>Sheet: 셀 값 반영
    else 저장
        Agent->>MR: ["DOC_SAVE"]
        MR->>ADH: processChange()
        ADH->>AM: execute(SaveAction)
    end

    actor User as 사용자
    User->>AM: Ctrl+Z (Undo)
    AM->>DL: Action.rollback()
    DL-->>Sheet: 에이전트 작업 되돌림
```

## 페이지네이션 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Pager as PaginationElement
    participant PS as PageState
    participant DA as DocumentApi
    participant DL as DocumentList
    participant Sheet as SpreadsheetElement

    User->>Pager: 다음 페이지 클릭
    Pager->>PS: next({...current, page: current.page + 1})
    PS-->>DA: search(workspace, {type, page:1, limit:50})
    DA-->>DL: List<DocumentValue> 발행
    DL-->>Sheet: 데이터 갱신
    DA-->>Pager: totalElements → 페이지 인디케이터 업데이트
```

---

## UC-D1: 문서 조회

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 워크스페이스 선택 완료, Shell이 document-ui 모듈을 로딩 |
| **정상 흐름** | 1. Shell이 `js/data.nocache.js`를 동적 로딩한다.<br>2. TypeApi로 타입 목록을 가져와 탭으로 표시한다.<br>3. 첫 번째 타입이 자동 선택되고 `ColumnFactory`가 속성 기반으로 컬럼을 생성한다.<br>4. DocumentApi로 해당 타입의 문서를 검색하여 스프레드시트에 렌더링한다. |
| **결과** | Handsontable 스프레드시트에 문서가 테이블 형태로 표시된다. |

## UC-D2: 문서 생성

| 항목 | 내용 |
|------|------|
| **액터** | 사용자, AI 에이전트 |
| **선행조건** | 타입이 선택됨 |
| **정상 흐름** | 1. Add 버튼을 클릭한다.<br>2. `AddDocumentAction`이 `DocumentList`에 빈 문서를 추가하고, `DirtyTracker.created`에 등록한다.<br>3. 스프레드시트 하단에 `.created` 상태의 빈 행이 추가된다 (tertiary-container 배경, 좌측 3px tertiary 보더).<br>4. 사용자가 셀을 클릭하여 serial, 속성값 등을 입력한다.<br>5. Save 전까지 로컬에만 존재하며, Undo로 제거 가능하다. |
| **대안 흐름** | 에이전트가 `DOC_ADD` 명령으로 실행 (동일한 DirtyTracker 경로). |

## UC-D3: 문서 편집

| 항목 | 내용 |
|------|------|
| **액터** | 사용자, AI 에이전트 |
| **선행조건** | 문서가 스프레드시트에 표시됨 |
| **정상 흐름** | 1. 셀을 클릭하여 값을 수정한다.<br>2. Handsontable의 `afterChange` 이벤트가 `EditDocumentAction(before, after)`을 생성한다.<br>3. `ActionManager`에서 실행되고, `DirtyTracker.changed`에 등록된다.<br>4. 변경된 셀에 `.changed` 상태가 표시된다 (tertiary 1px inset box-shadow).<br>5. Undo로 원본값이 복원되면 더티 플래그가 자동 해제된다. |
| **대안 흐름** | 에이전트가 `DOC_EDIT <serial> <field> <value>` 명령으로 실행 (동일한 DirtyTracker 경로). |
| **대안 흐름 (모바일)** | 셀 탭으로 편집 모드 진입. 가상 키보드가 올라올 때 스프레드시트가 자동 스크롤되어 편집 중인 셀이 가시 영역에 유지된다. 뷰포트 < 480px에서는 `CardViewElement`로 전환되어 카드 내에서 필드를 편집한다. |

## UC-D4: 문서 삭제

| 항목 | 내용 |
|------|------|
| **액터** | 사용자, AI 에이전트 |
| **선행조건** | 1개 이상의 행이 선택됨 |
| **정상 흐름** | 1. 행을 선택하고 Delete 버튼을 클릭한다.<br>2. `DeleteDocumentAction`이 실행되고, `DirtyTracker.deleted`에 등록된다.<br>3. 행이 `.deleted` 상태로 표시된다 (취소선, 75% 투명화). 즉시 제거되지 않고 Save 전까지 시각적으로 표시된다.<br>4. Undo로 삭제를 취소할 수 있다. |
| **대안 흐름** | 에이전트가 `DOC_DELETE <serial>` 명령으로 실행 (동일한 DirtyTracker 경로). |

## UC-D5: 저장 (패치 기반 원자적)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자, AI 에이전트 |
| **선행조건** | `ChangeTracker.hasChanges() == true` (Save 버튼 활성화 상태) |
| **정상 흐름** | 1. Save 버튼 클릭 (또는 에이전트 `DOC_SAVE`) → `SaveAction` 실행.<br>2. **created** 문서: `PUT /documents` (전체 데이터 전송, 새 문서 생성).<br>3. **changed** 문서: `PATCH /documents` (ChangeTracker가 추적한 변경 필드 + rev만 전송). 서버에서 JSONB 머지로 기존 데이터에 병합.<br>4. **deleted** 문서: `DELETE /documents`.<br>5. 전체 성공 시: Undo/Redo 스택 초기화, `ChangeTracker.reset()`, 모든 더티 상태 해제.<br>6. 부분 실패 시: 실패 항목만 더티 유지, 실패 셀에 `.invalid` 표시, 토스트. |
| **대안 흐름 (충돌)** | 409 Conflict: 같은 문서의 같은 필드를 동시 수정한 경우. `.conflict` 표시 + 사용자 선택. 서로 다른 필드 수정 시에는 JSONB 머지로 충돌 없이 병합됨. |

```mermaid
sequenceDiagram
    actor User as 사용자
    participant UI as document-ui
    participant CT as ChangeTracker
    participant GW as Gateway
    participant DB as Database

    User->>UI: Save 버튼 클릭
    UI->>CT: getChangedKeys(), getDeletedKeys()
    CT-->>UI: 변경 필드 맵, 삭제 키 목록

    alt 신규 문서 (created)
        UI->>GW: PUT /documents [{id:null, data:{전체}}]
        GW->>DB: INSERT
    end
    alt 수정 문서 (changed)
        UI->>GW: PATCH /documents [{id, rev, data:{변경필드만}}]
        GW->>DB: UPDATE data = data || patch, rev 체크
        alt rev 일치
            DB-->>GW: OK (rev+1)
        else rev 불일치 (동일 필드 충돌)
            DB-->>GW: OptimisticLockingFailure
            GW-->>UI: 409 Conflict
            Note over UI: .conflict 표시
        end
    end
    alt 삭제 문서 (deleted)
        UI->>GW: DELETE /documents [{id, rev}]
        GW->>DB: DELETE
    end

    GW-->>UI: 200 OK
    UI->>CT: reset()
```

## UC-D6: 타입 전환

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 타입 탭이 2개 이상 존재 |
| **정상 흐름** | 1. 다른 타입 탭을 클릭한다.<br>2. `TypeProvider`가 새 타입으로 전환된다.<br>3. `ColumnFactory`가 새 타입의 속성 기반으로 컬럼을 재생성한다.<br>4. 페이지가 0으로 초기화되고 새 타입의 문서가 로딩된다. |
| **주의** | `DirtyTracker.hasDirty()`이면 확인 다이얼로그를 표시한다: "저장하지 않은 변경사항이 있습니다. 계속하시겠습니까?" |
| **대안 흐름** | 에이전트가 `DOC_SELECT <type>` 명령으로 실행. |

## UC-D7: Undo/Redo

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 실행된 액션이 존재 |
| **정상 흐름** | 1. Ctrl+Z 또는 Undo 버튼 → `ActionManager.undo()`. 최근 액션의 `rollback()` 실행.<br>2. Ctrl+Shift+Z 또는 Redo 버튼 → `ActionManager.redo()`. 되돌린 액션의 `execute()` 재실행.<br>3. 스택은 최대 100개. 새 액션 실행 시 redo 스택 초기화.<br>4. Undo로 원본값이 복원되면 `DirtyTracker`에서 해당 셀의 더티 플래그가 자동 해제된다.<br>5. Save 성공 시 Undo/Redo 스택이 초기화된다 (서버 상태 변경 후 로컬 Undo 불가). |
| **특이사항** | 에이전트가 실행한 액션도 동일한 Undo 스택에 쌓이므로 사용자가 Ctrl+Z로 되돌릴 수 있다. |

## UC-D8: 페이지네이션

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 문서 수가 페이지 크기를 초과 |
| **정상 흐름** | 1. 페이지네이션 컨트롤에서 이전/다음 버튼을 클릭한다.<br>2. `PageState`가 새 page 번호로 업데이트된다.<br>3. DocumentApi로 해당 페이지의 문서를 다시 검색한다.<br>4. 스프레드시트가 갱신된다. |

## UC-D9: 에이전트에 의한 문서 조작

| 항목 | 내용 |
|------|------|
| **액터** | AI 에이전트 |
| **선행조건** | document-ui 모듈 로딩 완료, MutationReceiver 브릿지 연결 |
| **정상 흐름** | 1. 에이전트가 `DocumentStateProvider.snapshot()`으로 현재 문서 상태를 JSON으로 조회한다.<br>2. LLM이 DOC_* 명령을 생성한다.<br>3. `MutationReceiver`를 통해 `AgentDocumentHandler`에 전달된다.<br>4. 핸들러가 명령을 파싱하여 적절한 Action으로 변환하고 `ActionManager`에서 실행한다. |
| **지원 명령** | DOC_SELECT, DOC_ADD, DOC_EDIT, DOC_DELETE, DOC_SAVE |
| **브릿지** | `agent-bridge` 모듈의 `WindowMutationBridge`가 CustomEvent로 연결. |

## 모바일 레이아웃 전환 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자 (모바일)
    participant VP as ViewportObserver
    participant Sheet as SpreadsheetElement
    participant Tabs as TypeTabsElement
    participant Ctrl as ControllerElement
    participant Card as CardViewElement

    Note over VP: 뷰포트 < 768px 감지
    VP->>Sheet: fixedColumnsLeft = 1 (serial 고정)
    VP->>Tabs: overflow-x: auto (수평 스크롤)
    VP->>Ctrl: flex-wrap 활성화 (줄바꿈)

    alt 뷰포트 < 480px
        VP->>Card: 카드 뷰 활성화
        Card-->>Sheet: display: none
        Note over Card: 문서별 카드 리스트 표시
        User->>Card: 카드 탭 → 상세 보기
    else 뷰포트 480~768px
        VP->>Sheet: 수평 스크롤 모드
        User->>Sheet: 좌우 스와이프로 컬럼 탐색
    end
```

## UC-D10: 모바일 반응형 레이아웃

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (모바일/태블릿 디바이스) |
| **선행조건** | 뷰포트 너비 < 768px |
| **정상 흐름** | 1. `ViewportObserver`가 뷰포트 변경을 감지한다.<br>2. 스프레드시트에서 serial 컬럼이 고정(`fixedColumnsLeft=1`)되고 나머지 컬럼은 수평 스크롤된다.<br>3. 타입 탭이 수평 스크롤 가능한 탭 바로 표시된다.<br>4. 컨트롤러 툴바가 flex-wrap으로 줄바꿈되며, 핵심 버튼(Save, Add)만 1행에 표시된다.<br>5. 뷰포트 < 480px에서 `CardViewElement`로 전환하여 문서별 카드 뷰를 사용할 수 있다. |

## UC-D11: RBAC 권한 검증 (계획)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 워크스페이스 선택 완료, document-ui 모듈 로딩 완료 |
| **정상 흐름** | 1. 문서 편집 화면 진입 시 Shell로부터 현재 사용자의 권한 정보를 수신한다.<br>2. `workspace:document:edit` 권한이 있는 경우 일반 편집 모드로 진입한다.<br>3. `workspace:document:edit` 권한이 없는 경우 읽기 전용 모드로 전환한다.<br>4. 읽기 전용 모드에서는 Add, Delete, Save 버튼이 비활성화되고, 셀 편집이 차단된다.<br>5. 에이전트 명령(DOC_ADD, DOC_EDIT, DOC_DELETE, DOC_SAVE)도 권한이 없으면 무시된다. |
| **대안 흐름** | 권한 정보를 가져올 수 없는 경우 읽기 전용 모드로 기본 전환한다. |
| **요구사항** | 3.3 RBAC (역할 기반 접근 제어) — `{workspace}:type:{type}:document:edit` |

## UC-D12: 이력 조회 (계획)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 문서가 스프레드시트에 표시됨 |
| **정상 흐름** | 1. 사용자가 특정 문서의 이력 조회를 요청한다 (예: 컨텍스트 메뉴 또는 이력 버튼).<br>2. `DocumentApi`가 `GET /workspace/{id}/{type}/{serial}?date={datetime}`으로 특정 시점의 문서 버전을 조회한다.<br>3. date 파라미터가 지정된 시점에 유효한(effectDateTime ≤ date < expireDateTime) 문서 버전이 반환된다.<br>4. 조회된 과거 버전이 스프레드시트에 표시되거나 별도 다이얼로그로 비교 뷰가 제공된다. |
| **대안 흐름** | 해당 시점에 문서가 존재하지 않으면 404 Not Found가 반환되고, 사용자에게 안내 메시지를 표시한다. |
| **요구사항** | 3.7 이력 조회 — 문서의 변경 이력을 시간 기반으로 추적 |

## UC-D13: 실시간 협업

| 항목 | 내용 |
|------|------|
| **액터** | 다른 사용자 (이벤트 발행자) |
| **선행조건** | document-ui 모듈 로딩 완료, `DocumentEventHandler`가 초기화되어 `WorkspaceEventReceiver`를 구독 중 |
| **정상 흐름** | 1. 다른 사용자가 문서를 생성/삭제하면 서버가 Kafka를 통해 DOCUMENT_CREATED 또는 DOCUMENT_DELETED 이벤트를 발행한다.<br>2. shell-ui의 SSE 연결이 이벤트를 수신하고 `WindowWorkspaceEventBridge.publish()`로 CustomEvent를 디스패치한다.<br>3. `DocumentEventHandler`가 `WorkspaceEventReceiver.events()`를 통해 이벤트를 수신한다.<br>4. `DocumentRepository.search()`를 재호출하여 최신 문서 목록을 가져온다.<br>5. `DocumentList`가 갱신되어 스프레드시트에 반영된다.<br>6. 토스트 알림: "다른 사용자가 문서를 변경했습니다" |
| **요구사항** | 3.1 실시간 협업 |

## UC-D14: 동시 편집 충돌 방지 (계획)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 같은 문서를 여러 사용자가 동시에 편집 중 |
| **정상 흐름** | 1. 사용자가 문서를 저장할 때 서버 측에서 `@Version` 기반 낙관적 잠금으로 충돌을 감지한다.<br>2. 충돌이 발생하면 서버가 409 Conflict를 반환한다.<br>3. 클라이언트가 충돌 알림을 표시하고 최신 데이터를 다시 로드하도록 안내한다. |
| **요구사항** | 3.1 실시간 협업 — 낙관적 잠금 기반 충돌 감지 |

## UC-D15: 프레즌스 (다른 사용자 편집 표시)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 같은 워크스페이스에 2명 이상 동시 접속 |
| **정상 흐름** | 1. 사용자 A가 셀을 선택하면 200ms 디바운스 후 `POST /workspace/{id}/presence`로 위치를 전송한다.<br>2. SSE PRESENCE 이벤트가 다른 사용자에게 전달된다.<br>3. 해당 셀에 사용자별 고유 색상 보더(2px)와 이름 라벨이 표시된다 (3초 후 fade-out).<br>4. 포커스 해제 시 프레즌스가 해제된다.<br>5. 30초 갱신 없으면 자동 해제 (연결 끊김 대비). |
| **요구사항** | 3.1 실시간 협업 — 프레즌스 |

```mermaid
sequenceDiagram
    actor A as 사용자 A
    actor B as 사용자 B
    participant Sheet as SpreadsheetElement (B)
    participant GW as Gateway (SSE)

    A->>GW: POST /presence {user:"A", serial:"CUST-001", field:"name"}
    GW-->>Sheet: SSE PRESENCE 이벤트
    Sheet->>Sheet: 셀 [CUST-001, name]에 A 색상 보더 + "A님" 라벨
    Note over Sheet: 3초 후 라벨 fade-out, 보더는 유지

    A->>GW: POST /presence {user:"A", type:null}
    GW-->>Sheet: SSE PRESENCE 해제
    Sheet->>Sheet: 프레즌스 제거
```

---

## 트레이서빌리티 매트릭스

| UC | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---|---|---|
| UC-D1 (조회) | 초기 로딩 | Application, TypeApi, DocumentApi, TypeList, TypeProvider, DocumentList, ColumnFactory, SpreadsheetElement | DocumentTest: 스프레드시트 렌더링, 타입 탭 표시 |
| UC-D2 (생성) | 문서 편집 → 저장 | AddDocumentAction, ActionManager, DocumentList, AddButton | DocumentTest: Add 클릭 → 행 추가 |
| UC-D3 (편집) | 문서 편집 → 저장 | EditDocumentAction, ActionManager, SpreadsheetElement, DocumentList | DocumentTest: 셀 변경 검증 |
| UC-D4 (삭제) | 문서 편집 → 저장 | DeleteDocumentAction, ActionManager, DocumentList, DeleteButton | DocumentTest: 행 삭제 검증 |
| UC-D5 (저장) | 문서 편집 → 저장 (후반) | SaveAction, DocumentApi, ActionManager | DocumentTest: Save 버튼 존재 확인 |
| UC-D6 (타입전환) | 타입 탭 전환 | TypeTabsElement, TypeProvider, ColumnFactory, DocumentApi, PageState | DocumentTest: 탭 전환 → 컬럼 변경 검증 |
| UC-D7 (Undo) | — | ActionManager, UndoButton, RedoButton | DocumentTest: Ctrl+Z/Ctrl+Shift+Z 검증 |
| UC-D8 (페이지) | 페이지네이션 | PaginationElement, PageState, DocumentApi, DocumentList | DocumentTest: 페이지 이동 검증 |
| UC-D9 (에이전트) | 에이전트 문서 조작 | AgentDocumentHandler, DocumentStateProvider, MutationReceiver, WindowMutationBridge | ❌ 미구현 |
| UC-D10 (모바일) | 모바일 레이아웃 전환 | ViewportObserver, SpreadsheetElement(fixedColumnsLeft), CardViewElement, ControllerElement(flex-wrap), TypeTabsElement(overflow-x) | ❌ 미구현 |
| UC-D11 (RBAC) | — | RbacGuard (계획), SpreadsheetElement(readOnly) (계획) | ❌ 미구현 (계획) |
| UC-D12 (이력조회) | — | DocumentApi(date param), HistoryDialog (계획) | ❌ 미구현 (계획) |
| UC-D13 (실시간협업) | — | DocumentEventHandler, WorkspaceEventReceiver, DocumentRepository, DocumentList, ToastContainer | DocumentTest: DOCUMENT_CREATED 이벤트 디스패치 → 문서 목록 갱신 검증 |
| UC-D14 (충돌방지) | — | @Version 낙관적 잠금 (계획) | ❌ 미구현 (계획) |
| UC-D15 (프레즌스) | 프레즌스 시퀀스 | PresenceHandler, PresenceRenderer, WorkspaceEventReceiver | DocumentTest: PRESENCE 이벤트 수신/해제 검증 |

### 글로벌 UC 매핑

| 글로벌 UC | document-ui UC | 설명 |
|----------|---------------|------|
| UC-50 (문서 생성) | UC-D2 | Add 버튼 → DirtyTracker.created → Save |
| UC-51 (문서 변경) | UC-D3 | 셀 편집 → DirtyTracker.changed → Save |
| UC-52 (문서 삭제) | UC-D4 | Delete 버튼 → DirtyTracker.deleted → Save |
| UC-53 (문서 조회) | UC-D1 | 타입 선택 → 문서 로딩 |
| UC-54 (문서 검색) | UC-D8 | 페이지네이션 + 필터 |
| UC-55 (이력 조회) | UC-D12 | 계획 |
| UC-58 (프레즌스) | UC-D15 | 셀 선택 → SSE → 프레즌스 표시 |
| UC-59 (협업 충돌) | UC-D13, UC-D14 | SSE 갱신 + 낙관적 잠금 |
