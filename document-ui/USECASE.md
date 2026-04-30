# Document-UI 유스케이스

## 초기 로딩 시퀀스

```mermaid
sequenceDiagram
    participant Shell as Shell (ModuleScriptManager)
    participant App as Application
    participant TA as TypeApi
    participant TL as TypeList
    participant TP as TypeProvider
    participant CF as ColumnFactory
    participant DA as DocumentApi
    participant DL as DocumentList
    participant Sheet as SpreadsheetElement

    Shell->>App: "js/data/data.nocache.js 로딩"
    App->>App: "DaggerComponent.create()"
    App->>App: "WindowStateProviderBridge.register()"
    App->>TA: "list()"
    TA-->>TL: "List<TypeInfo> 발행"
    TL-->>TP: "첫 번째 타입 자동 선택"
    TP-->>CF: "속성 → 컬럼 정의 변환"
    CF-->>Sheet: "Handsontable 컬럼 설정"
    TP-->>DA: "search(workspace, {type, page:0, limit:50})"
    DA-->>DL: "List<DocumentValue> 발행"
    DL-->>Sheet: "데이터 렌더링"
```

## 문서 편집 → 저장 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Sheet as SpreadsheetElement
    participant AM as ActionManager
    participant DL as DocumentList
    participant CT as ChangeTracker
    participant DA as DocumentApi

    User->>Sheet: "셀 값 변경"
    Sheet->>Sheet: "afterChange 이벤트 캡처"
    Sheet->>AM: "execute(EditDocumentAction(before, after))"
    AM->>DL: "문서 업데이트"
    AM->>CT: "CHANGED 마킹"

    User->>Sheet: "새 행 추가 (Add 버튼)"
    Sheet->>AM: "execute(AddDocumentAction)"
    AM->>DL: "빈 문서 추가"
    DL-->>Sheet: "행 추가 렌더링"

    User->>Sheet: "Save 버튼 클릭"
    Sheet->>AM: "execute(SaveAction)"
    AM->>DA: "PUT /workspaces/{id}/documents (변경된 문서)"
    AM->>DA: "DELETE /workspaces/{id}/documents (삭제된 문서)"
    DA-->>AM: "저장 완료"
    AM->>AM: "스택 초기화"
```

## UC-D16: 타입 인식 입력 위젯

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 타입이 선택되고 스프레드시트에 컬럼이 렌더링됨 |
| **정상 흐름** | 1. `ColumnFactory.create(type, allTypes)`가 `TypeInfo`의 속성 목록을 순회하며 `ColumnDef.fromAttribute(attr, typeNames)`로 속성 타입별 컬럼을 생성한다.<br>2. **enum** 속성: `dropdown` 타입 컬럼으로 변환. `allowedValues`가 드롭다운 source로 설정되어 사용자가 허용 값 중 하나를 선택한다.<br>3. **date** 속성: `date` 타입 컬럼으로 변환. `YYYY-MM-DD HH:mm` 포맷의 날짜/시간 선택기가 활성화된다.<br>4. **number** 속성: `numeric` 타입 컬럼으로 변환. 숫자 입력만 허용되며 소수점/음수 지원.<br>5. **bool** 속성: `checkbox` 타입 컬럼으로 변환. 체크박스로 true/false를 토글한다.<br>6. **document** 속성: `dropdown` 타입 컬럼으로 변환. 현재 레이아웃의 전체 타입 이름 목록(`typeNames`)이 드롭다운 source로 설정되어 참조할 타입을 선택한다.<br>7. **text/기타** 속성: `text` 타입 컬럼으로 변환. 자유 텍스트 입력. |
| **결과** | 속성 타입에 맞는 전용 입력 위젯이 셀 편집 시 활성화되어 데이터 품질을 보장한다. |

```mermaid
sequenceDiagram
    participant TL as TypeList
    participant TI as TypeInfo
    participant CF as ColumnFactory
    participant CD as ColumnDef
    participant Sheet as SpreadsheetElement (Handsontable)

    CF->>TL: "getValue() → allTypes"
    CF->>TL: "allTypes.stream().map(id) → typeNames[]"
    CF->>TI: "attributes 순회"
    loop "각 속성"
        CF->>CD: "fromAttribute(attr, typeNames)"
        alt "type = 'enum'"
            CD-->>CF: "dropdown + source=allowedValues"
        else "type = 'date'"
            CD-->>CF: "date + dateFormat='YYYY-MM-DD HH:mm'"
        else "type = 'number'"
            CD-->>CF: "numeric"
        else "type = 'bool'"
            CD-->>CF: "checkbox"
        else "type = 'document'"
            CD-->>CF: "dropdown + source=typeNames"
        else "기타"
            CD-->>CF: "text"
        end
    end
    CF-->>Sheet: "ColumnDef[] 전달"
    Sheet->>Sheet: "toColumns() → Handsontable Column[] 변환"
```

## UC-D17: 에이전트 + 사용자 동시 문서 편집

| 항목 | 내용 |
|------|------|
| **액터** | 사용자, AI 에이전트 |
| **선행조건** | 사용자가 셀을 편집 중, 에이전트가 DOC_ADD 명령을 실행 |
| **정상 흐름** | 1. 사용자가 스프레드시트에서 셀을 선택하여 편집 중이다.<br>2. 에이전트가 `AgentMutation` 브릿지를 통해 DOC_ADD 명령을 실행한다.<br>3. `AgentDocumentHandler`가 `AddDocumentAction`을 `ActionManager`에서 실행하여 새 행이 추가된다.<br>4. 사용자의 편집 중인 셀과 스프레드시트가 정상 유지된다. |
| **결과** | 에이전트의 동시 문서 추가가 사용자의 편집 상태를 방해하지 않는다. |

## UC-D21: 벌크 작업 (다중 선택 일괄 삭제/상태 변경)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | 문서 목록 로딩 완료 |
| **정상 흐름** | 1. 체크박스 또는 Shift+클릭으로 문서를 다중 선택한다.<br>2. 일괄 삭제 또는 일괄 상태 변경(DRAFT → PUBLISHED 등)을 실행한다.<br>3. 확인 다이얼로그 후 선택된 문서에 대해 일괄 처리가 수행된다. |
| **상태** | ✅ 구현 완료 (`BulkDeleteButton`, `BulkStatusButton`, `SelectedRows`) |

## 트레이서빌리티 매트릭스

| UC | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---|---|---|
| UC-D1 (초기로딩) | 초기 로딩 시퀀스 | Application, TypeApi, DocumentApi, ColumnFactory, SpreadsheetElement | DocumentTest: 초기 로딩 및 탭 생성 검증 |
| UC-D2 (문서생성) | 문서 편집 → 저장 | AddDocumentAction, ActionManager, DocumentList | DocumentTest: 문서 추가 버튼 클릭 및 행 생성 확인 |
| UC-D5 (문서저장) | 문서 편집 → 저장 | SaveAction, ChangeTracker, DocumentApi | DocumentTest: 저장 버튼 상태 및 성공 피드백 검증 |
| UC-D9 (에이전트) | 에이전트 문서 조작 | AgentDocumentHandler, DocumentStateProvider, AgentMutation | CollaborationTest: 에이전트 조작 검증 |
| UC-D13 (실시간협업) | — | DocumentEventHandler, WorkspaceEventReceiver, DocumentRepository | CollaborationTest: 실시간 이벤트 수신 검증 |
| UC-D16 (타입위젯) | 타입 인식 입력 위젯 | ColumnFactory, ColumnDef, SpreadsheetElement | DocumentInputTest: 각 타입별 입력 위젯 활성화 검증 |
| UC-D21 (벌크작업) | — | BulkDeleteButton, BulkStatusButton, SelectedRows | DocumentTest: 다중 선택 및 벌크 액션 검증 |
