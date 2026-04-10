# Document-UI 모듈

스프레드시트 기반 문서 편집기 (GWT). Handsontable을 사용하여 타입별 문서를 테이블 형태로 편집한다.
GWT 모듈명은 `rename-to="data"`로 설정되어 있다.
Shell이 `ModuleScriptManager`로 `js/data.nocache.js`를 동적 로딩하여 실행한다.

---

## 스프레드시트 (SpreadsheetElement)

Handsontable JS 라이브러리를 JsInterop으로 래핑한 테이블 컴포넌트.

- **동적 컬럼**: 선택된 타입의 속성(Attribute)을 기반으로 컬럼을 자동 생성한다.
- **컬럼 타입 매핑**: AttributeType에 따라 Handsontable column type을 결정한다.
  - Text → text, Number → numeric, Date → date, Enum → dropdown, Bool → checkbox
- **고정 컬럼**: serial(문서 식별자), effectDateTime, expireDateTime은 항상 표시된다.
- **수정 감지**: `afterChange` 이벤트로 셀 변경을 캡처하여 `EditDocumentAction`으로 변환한다.

---

## 컨트롤러 (ControllerElement)

상단 툴바. 타입 탭 선택, 문서 CRUD, Undo/Redo, 페이지네이션을 제공한다.

| 그룹 | 컴포넌트 | 동작 |
|------|---------|------|
| 타입 선택 | `TypeTabsElement` | 타입 탭 선택 → 컬럼 재구성 + 문서 다시 로딩 |
| 문서 CRUD | `AddButton`, `DeleteButton` | 새 문서 추가 / 선택 문서 삭제 |
| 히스토리 | `UndoButton`, `RedoButton` | Undo/Redo |
| 저장 | `SaveButton` | 변경사항 서버 저장 |
| 페이지네이션 | `PaginationElement` | 페이지 이동, 페이지당 항목 수 변경 |

---

## Undo/Redo (ActionManager)

모든 편집 작업은 `Action` 인터페이스(`execute()`, `rollback()`)로 캡슐화된다.

| Action | 역할 |
|--------|------|
| `AddDocumentAction` | DocumentList에 빈 문서 추가 |
| `EditDocumentAction` | 셀 값 변경 (before → after) |
| `DeleteDocumentAction` | DocumentList에서 제거 |
| `SaveAction` | 변경/삭제 문서 서버 저장, 스택 초기화 |

---

## 에이전트 연동

### 상태 조회 (DocumentStateProvider)

`StateProvider` 구현. 현재 스프레드시트의 문서 데이터를 JSON으로 반환한다.

### Mutation 수신 (AgentDocumentHandler)

`MutationReceiver`를 구독하여 에이전트 명령을 Action으로 변환한다.

| 명령어 | 동작 |
|--------|------|
| `DOC_SELECT <type>` | 타입 탭 전환 |
| `DOC_ADD` | 새 문서 추가 |
| `DOC_EDIT <serial> <field> <value>` | 셀 편집 |
| `DOC_DELETE <serial>` | 문서 삭제 |
| `DOC_SAVE` | 저장 |

---

## 상태 관리

| 클래스 | 타입 | 역할 |
|--------|------|------|
| `DocumentList` | `BehaviorSubject<List<DocumentValue>>` | 현재 타입의 문서 목록 |
| `TypeProvider` | `BehaviorSubject<TypeInfo>` | 현재 선택된 타입 |
| `TypeList` | `BehaviorSubject<List<TypeInfo>>` | 전체 타입 목록 |
| `PageState` | `BehaviorSubject<Search>` | 현재 검색/페이지 상태 |

---

## API 연동

| 포트 메서드 | HTTP | 설명 |
|------------|------|------|
| `DocumentRepository.search(param)` | `GET /workspace/{id}/documents` | 문서 검색 |
| `DocumentRepository.save(docs)` | `PUT /workspace/{id}/documents` | 문서 저장 |
| `DocumentRepository.delete(docs)` | `DELETE /workspace/{id}/documents` | 문서 삭제 |
| `TypeRepository.list()` | `GET /workspace/{id}/types` | 타입 목록 (컬럼 정의용) |

---

## 프로젝트 구조

```
document-ui/
├── build.gradle.kts
├── src/main/
│   ├── java/dev/sayaya/handbook/
│   │   ├── Document.gwt.xml
│   │   └── client/
│   │       ├── domain/       (Action, DocumentValue, TypeInfo, ColumnDef)
│   │       ├── usecase/      (상태 4 + 액션 4 + ActionManager + 포트 2 + 에이전트 2)
│   │       └── interfaces/   (api 4 + ui 12)
│   └── webapp/css/document-ui.css
└── src/test/
    ├── java/      TestComponent, MockModule, TestApplication
    ├── kotlin/    DocumentTest.kt (Playwright)
    └── webapp/    documenttest.html
```

## 실행

```bash
# DevMode
./gradlew :document-ui:gwtDev

# 컴파일
./gradlew :document-ui:compileJava

# 테스트
./gradlew :document-ui:test
```

## 모바일 지원

- **스프레드시트**: 좁은 뷰포트에서 serial 컬럼을 고정(fixedColumnsLeft)하고 나머지는 수평 스크롤.
- **타입 탭**: 수평 스크롤 가능한 탭 바. overflow-x: auto.
- **컨트롤러 툴바**: flex-wrap으로 좁은 화면에서 줄바꿈. 핵심 버튼(Save, Add)만 1행에 표시.
- **셀 편집**: 모바일 가상 키보드와 호환되도록 셀 포커스 시 스크롤 위치 자동 조정.
- **카드 뷰 전환**: 뷰포트 < 480px에서 스프레드시트 대신 문서별 카드 뷰로 전환 가능 (Tool Rail 토글).

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
