# Document-UI 모듈

스프레드시트 기반 문서 편집기 (GWT). Handsontable을 사용하여 타입별 문서를 테이블 형태로 편집한다.
GWT 모듈명은 `rename-to="data"`로 설정되어 있다.
Shell이 `ModuleScriptManager`로 `js/data/data.nocache.js`를 동적 로딩하여 실행한다.

## Mount 패턴

`Application.onModuleLoad()` 은 `WindowRenderBridge.next(render)` 로 shell
`FrameUpdater` 에 Render 위임. body 직접 append 금지 —
[`docs/contracts/frame.md`](../../docs/contracts/frame.md).

---

## 스프레드시트 (SpreadsheetElement)

Handsontable 6.2.4 (MIT) 라이브러리를 JsInterop으로 래핑한 테이블 컴포넌트.

- **동적 컬럼**: 선택된 타입의 속성(Attribute)을 기반으로 컬럼을 자동 생성한다.
- **컬럼 타입 매핑**: AttributeType에 따라 Handsontable column type을 결정한다.
  - Text → text, Number → numeric, Date → date, Enum → dropdown, Bool → checkbox
- **고정 컬럼**: serial(문서 식별자), effectDateTime, expireDateTime은 항상 표시된다.
- **수정 감지**: `afterChange` 이벤트로 셀 변경을 캡처하여 `EditDocumentAction`으로 변환한다.
- **읽기 전용 셀**: `htDimmed` 클래스로 편집 불가 셀을 시각적으로 구분한다.
- **행 호버/zebra**: 행 탐색 시 호버 피드백, 짝수 행 배경색 구분을 제공한다.

---

## 컨트롤러 (ControllerElement)

상단 툴바. 타입 탭 선택, 문서 CRUD, Undo/Redo, 페이지네이션을 제공한다.

| 그룹 | 컴포넌트 | 동작 |
|------|---------|------|
| 타입 선택 | `TypeTabsElement` | 타입 탭 선택 → 컬럼 재구성 + 문서 다시 로딩 |
| 문서 CRUD | `AddButton`, `DeleteButton` | 새 문서 추가 / 선택 문서 삭제 |
| 벌크 작업 | `BulkDeleteButton`, `BulkStatusButton` | 다중 선택 문서 일괄 삭제 / 상태 변경 (DRAFT/REVIEW/PUBLISHED) |
| 히스토리 | `UndoButton`, `RedoButton` | Undo/Redo |
| 저장 | `SaveButton` | 변경사항 서버 저장 |
| 페이지네이션 | `PaginationElement` | 페이지 이동, 페이지당 항목 수 변경 |

---

## 더티 트래킹 & Undo/Redo

`DirtyTracker`로 로컬 변경을 추적하고, `ActionManager`로 Undo/Redo를 지원한다. Save 시 원자적 저장.

> 공통 패턴 상세는 [설계 패턴](../docs/design-patterns.md) 참조.
> document-ui 전용 디자인 명세는 [DESIGN.md](DESIGN.md) 참조.

| Action | 역할 |
|--------|------|
| `AddDocumentAction` | DocumentList에 빈 문서 추가, DirtyTracker.created에 등록 |
| `EditDocumentAction` | 셀 값 변경 (before → after), DirtyTracker.changed에 등록 |
| `DeleteDocumentAction` | DocumentList에서 제거, DirtyTracker.deleted에 등록 |
| `SaveAction` | created+changed → PUT, deleted → DELETE 원자적 저장, 스택 초기화 |

---

## 에이전트 연동

### 상태 조회 (DocumentStateProvider)

`StateProvider` 구현. 현재 스프레드시트의 문서 데이터를 JSON으로 반환한다.

### Mutation 수신 (AgentDocumentHandler)

`MutationReceiver`를 구독하여 에이전트 명령을 Action으로 변환한다.
에이전트 편집도 사용자 편집과 동일한 Action/DirtyTracker 경로를 타며, Undo/Redo 가능하다.

| 명령어 | 동작 |
|--------|------|
| `DOC_SELECT <type>` | 타입 탭 전환 (미저장 변경이 있으면 경고) |
| `DOC_ADD` | 새 문서 추가 (DirtyTracker.created 등록) |
| `DOC_EDIT <serial> <field> <value>` | 셀 편집 (DirtyTracker.changed 등록) |
| `DOC_DELETE <serial>` | 문서 삭제 마킹 (DirtyTracker.deleted 등록) |
| `DOC_SAVE` | 원자적 저장 (토스트: "에이전트가 저장을 요청했습니다") |

---

## 타입 인식 입력 위젯

`ColumnFactory`가 타입 속성의 `AttributeType`에 따라 Handsontable 컬럼을 동적 생성한다. `ColumnDef` 클래스가 각 컬럼의 이름, 타입, 너비, 입력 소스를 정의한다.

| 속성 타입 | 입력 위젯 |
|-----------|-----------|
| Text | 텍스트 입력 |
| Number | 숫자 전용 입력 (numeric) |
| Date | 날짜 선택 (date picker) |
| Enum | 드롭다운 (source: allowedValues) |
| Bool | 체크박스 |
| Document | 참조 타입 드롭다운 |
| File | 텍스트 입력 |
| Array | 텍스트 입력 |
| Map | 텍스트 입력 |

---

## 모바일 지원

- **컨트롤러 툴바**: CSS flex-wrap으로 좁은 화면에서 자동 줄바꿈된다.
- **타입 탭**: 수평 스크롤(overflow-x: auto)로 많은 타입이 있어도 탐색 가능하다.
- **스프레드시트**: 수평 스크롤 + 고정 컬럼(serial)으로 모바일에서도 문서를 탐색할 수 있다.
- **ViewportObserver**: 뷰포트 크기에 따라 레이아웃을 자동 전환한다.

---

## 상태 관리

| 클래스 | 타입 | 역할 |
|--------|------|------|
| `DocumentList` | `BehaviorSubject<List<DocumentValue>>` | 현재 타입의 문서 목록 |
| `DirtyTracker` | `created(Set), changed(Map), deleted(Set)` | 로컬 변경 추적 (Save 전까지) |
| `TypeProvider` | `BehaviorSubject<TypeInfo>` | 현재 선택된 타입 |
| `TypeList` | `BehaviorSubject<List<TypeInfo>>` | 전체 타입 목록 |
| `PageState` | `BehaviorSubject<Search>` | 현재 검색/페이지 상태 |

---

## API 연동

| 포트 메서드 | HTTP | 설명 |
|------------|------|------|
| `DocumentRepository.search(param)` | `GET /workspace/{id}/documents` | 문서 검색 |
| `DocumentRepository.save(docs)` | `PUT /workspace/{id}/documents` | 문서 저장 (신규) |
| `DocumentRepository.patch(patches)` | `PATCH /workspace/{id}/documents` | 문서 부분 업데이트 (변경 필드만) |
| `DocumentRepository.delete(docs)` | `DELETE /workspace/{id}/documents` | 문서 삭제 |
| `TypeRepository.list()` | `GET /workspace/{id}/types` | 타입 목록 (컬럼 정의용) |

---

## 실행

```bash
./gradlew :document-ui:gwtDev    # DevMode
./gradlew :document-ui:test      # 테스트
```

> 상세 유스케이스는 [USECASE.md](USECASE.md), 디자인 명세는 [DESIGN.md](DESIGN.md) 참조.
