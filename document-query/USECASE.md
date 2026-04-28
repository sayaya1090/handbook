# Search-Document 유스케이스

## 문서 검색 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (document-ui)
    participant GW as Gateway
    participant Ctrl as DocumentController
    participant Svc as DocumentService
    participant Repo as DocumentRepository
    participant DB as PostgreSQL

    Client->>GW: GET /workspace/{workspaceId}/documents?page=0&limit=50&type=customer
    GW->>Ctrl: query: Search(page, limit, sortBy, asc, filters)
    Ctrl->>Svc: search(workspace, param)
    Svc->>Repo: search(workspace, param)
    Repo->>Repo: 동적 Criteria 생성 (type, serial, date, last)
    Repo->>DB: SELECT + COUNT (R2dbcEntityTemplate)
    DB-->>Repo: rows + total count
    Repo->>Repo: R2dbcDocumentEntity → Document 변환
    Repo-->>Svc: Mono<Page<Document>>
    Svc-->>Ctrl: Mono<Page<Document>>
    Ctrl-->>Client: 200 OK + { content: [...], totalElements, page, size }
```

## 문서 단건 조회 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant GW as Gateway
    participant Ctrl as DocumentController
    participant Svc as DocumentService
    participant Repo as DocumentRepository
    participant DB as PostgreSQL

    Client->>GW: GET /workspace/{workspaceId}/customer/CUST-001?date=2026-01-01T00:00:00Z
    GW->>Ctrl: type="customer", serial="CUST-001", date="2026-01-01T00:00:00Z"
    Ctrl->>Svc: find(workspace, type, serial, date)
    Svc->>Repo: find(workspace, type, serial, date)
    Repo->>DB: SELECT WHERE type=? AND serial=? AND effect<=date AND expire>date
    DB-->>Repo: row
    Repo->>Repo: R2dbcDocumentEntity → Document 변환
    Repo-->>Svc: Mono<Document>
    Svc-->>Ctrl: Mono<Document>
    Ctrl-->>Client: 200 OK + Document
```

## 메뉴 제공 시퀀스

```mermaid
sequenceDiagram
    participant GW as Gateway
    participant MC as MenuController
    participant Shell as Shell-UI

    GW->>MC: GET /menus
    MC-->>GW: Menu(title="documents", icon="fa-database", script="js/data/data.nocache.js", url="/workspace/{workspaceId}/documents", urlRegex=["^/workspace/\{workspaceId\}/documents$"])
    GW->>GW: 다른 서비스의 메뉴와 합산
    GW-->>Shell: 전체 메뉴 목록
    Shell->>Shell: 사이드바에 "documents" 메뉴 렌더링
    Note over Shell: order="A" → 메뉴 목록 최상단
```

---

## UC-SD1: 문서 검색

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (document-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유, 문서 타입 선택 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{workspaceId}/documents`에 검색 파라미터를 전송한다.<br>2. `DocumentService.search()`가 `DocumentRepository.search()`를 호출한다.<br>3. 동적 Criteria로 필터링 (type, serial, date, last)하고 페이지네이션한다.<br>4. `Page<Document>`가 반환된다 (content, totalElements, page, size). |
| **대안 흐름** | 필터 없이 호출 시 전체 문서 목록 반환. 잘못된 파라미터 시 400 Bad Request. |

## UC-SD2: 문서 단건 조회

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 또는 시스템 |
| **선행조건** | 타입과 serial을 알고 있음 |
| **정상 흐름** | 1. `GET /workspace/{workspaceId}/{type}/{serial}`로 요청한다.<br>2. date 파라미터가 있으면 해당 시점의 문서를, 없으면 현재 시점의 문서를 반환한다.<br>3. 문서의 `data` 필드(속성 맵)가 포함된 응답이 반환된다. |
| **대안 흐름** | 문서를 찾을 수 없으면 404 Not Found. |

## UC-SD3: 메뉴 제공

| 항목 | 내용 |
|------|------|
| **액터** | Gateway (시스템) |
| **선행조건** | document-query 서비스 실행 중 |
| **정상 흐름** | 1. Gateway가 `GET /menus`를 호출한다.<br>2. `MenuController`가 documents 메뉴 정보를 반환한다.<br>3. title="documents", icon="fa-database", order="A", script="js/data/data.nocache.js", URL="/workspace/{workspaceId}/documents", URL 패턴("^/workspace/\{workspaceId\}/documents$").<br>4. Gateway가 다른 서비스의 메뉴와 합산하여 Shell에 전달한다. |

## UC-SD4: 전문 검색

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (document-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유, 문서 타입 선택 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{workspaceId}/documents`에 텍스트 검색 파라미터를 전송한다.<br>2. `DocumentService.search()`가 문서의 `data` 필드(속성 맵) 내 값을 대상으로 텍스트 매칭을 수행한다.<br>3. JSON 데이터 필드 내 문자열 값에 대해 부분 일치(LIKE) 또는 전문 검색(full-text search)을 지원한다.<br>4. 기존 필터(type, serial, date, last)와 조합하여 사용할 수 있다.<br>5. 검색 결과가 `Page<Document>` 형태로 반환된다. |
| **대안 흐름** | 검색어가 비어있으면 기존 UC-SD1과 동일하게 필터 기반 목록을 반환한다. |
| **요구사항** | 3.13 사용성 — 문서 및 타입에 대한 전문 검색(full-text search) 지원 |

## UC-SD5: 문서 내보내기

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (document-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유, 문서 타입 선택 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{workspaceId}/documents/export`에 형식(format=csv 또는 json)과 필터 파라미터를 전송한다.<br>2. `ExportController`가 `DocumentSearchService.findAllForExport()`를 호출하여 필터 조건에 맞는 문서를 조회한다.<br>3. `CsvSerializer`(CSV) 또는 JSON 직렬화를 통해 응답 스트림으로 반환한다.<br>4. `Content-Disposition: attachment` 헤더로 파일 다운로드를 트리거한다. |
| **대안 흐름** | 지원하지 않는 형식 요청 시 400 Bad Request. 문서가 없으면 빈 파일 반환. |
| **요구사항** | 3.12 API 접근성 — 문서를 CSV/JSON 형식으로 일괄 익스포트 |

## UC-SD6: 문서 이력 조회

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (document-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유, 대상 문서가 존재 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{workspaceId}/{type}/{serial}/history`를 요청한다.<br>2. `DocumentSearchService.findHistory()`가 해당 문서의 모든 버전을 시간순으로 조회한다.<br>3. `Flux<Document>`로 전체 이력이 반환된다. |
| **대안 흐름** | 문서가 존재하지 않으면 빈 결과가 반환된다. |
| **요구사항** | 3.7 이력 조회 — 문서의 변경 이력을 시간 기반으로 추적 |

---

## UC-SD7: 검색 쿼리 제한

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (document-ui 경유) |
| **선행조건** | 전문 검색 요청 |
| **정상 흐름** | 1. `DocumentController`가 검색 쿼리 문자열의 길이를 `MAX_QUERY_LENGTH`(1000자)와 비교한다.<br>2. 초과 시 400 Bad Request를 반환한다 ("Search query exceeds maximum length of 1000 characters").<br>3. 이내이면 `DocumentSearchService.fullTextSearch()`를 호출한다. |
| **요구사항** | 7.1 보안 강화 — 검색 쿼리 제한 |
| **상태** | ✅ 구현 완료 |
| **구현 클래스** | `DocumentController` (MAX_QUERY_LENGTH = 1000) |

## UC-SD8: DB 인덱스

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (데이터베이스) |
| **선행조건** | PostgreSQL 스키마 마이그레이션 |
| **정상 흐름** | 1. `V2__add_indexes.sql` 마이그레이션으로 4개 인덱스 생성:<br>  - `idx_documents_workspace_type` — documents (workspace, type)<br>  - `idx_documents_workspace_dates` — documents (workspace, effect_date_time, expire_date_time)<br>  - `idx_types_workspace_dates` — types (workspace, effect_date_time, expire_date_time)<br>  - `idx_type_attributes_type` — type_attributes (type_id, type_version)<br>2. 검색 성능이 인덱스 스캔으로 개선된다. |
| **요구사항** | 7.2 성능 최적화 — DB 인덱스 |
| **상태** | ✅ 구현 완료 |
| **구현** | `docs/migrations/V2__add_indexes.sql` |

## UC-SD9: Export 스트리밍

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (document-ui 경유) |
| **선행조건** | 대량 문서 내보내기 요청 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{id}/documents/export`를 요청한다.<br>2. `ExportController`가 `ServerWebExchange.response`에 직접 write하여 청크 단위로 전송한다.<br>3. JSON 형식: `exportJsonStreaming()` — ObjectMapper 직렬화 후 DataBuffer로 write.<br>4. CSV 형식: `exportCsvStreaming()` — CsvSerializer 직렬화 후 DataBuffer로 write.<br>5. `Content-Disposition: attachment` 헤더로 파일 다운로드를 트리거한다. |
| **요구사항** | 7.2 성능 최적화 — Export 스트리밍 |
| **상태** | ✅ 구현 완료 |
| **구현 클래스** | `ExportController` (exportJsonStreaming, exportCsvStreaming) |

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant Ctrl as ExportController
    participant Svc as DocumentSearchService
    participant DB as PostgreSQL

    Client->>Ctrl: GET /workspace/{id}/documents/export
    Ctrl->>Svc: findAllForExport(workspace, filters)
    Svc->>DB: SELECT (스트리밍 쿼리)
    loop "청크 단위 전송"
        DB-->>Svc: 문서 N건
        Svc-->>Ctrl: Flux<Document> (N건)
        Ctrl-->>Client: chunked JSON/CSV (N건)
    end
    Note over Client: Transfer-Encoding: chunked
```

---

## 트레이서빌리티 매트릭스

| UC | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---|---|---|
| UC-SD1 (검색) | 문서 검색 | DocumentController, DocumentService, DocumentRepository, R2dbcDocumentRepository, Search | DocumentServiceTest, DocumentControllerTest |
| UC-SD2 (단건) | 문서 단건 조회 | DocumentController, DocumentService, DocumentRepository | DocumentControllerTest |
| UC-SD3 (메뉴) | 메뉴 제공 | MenuController, Menu, Tool | MenuControllerTest |
| UC-SD4 (전문검색) | — | DocumentController.fullTextSearch(), DocumentSearchService.fullTextSearch() | DocumentControllerTest |
| UC-SD5 (내보내기) | — | ExportController, DocumentSearchService.findAllForExport(), CsvSerializer | CsvSerializerTest, ExportControllerTest |
| UC-SD6 (이력 조회) | — | DocumentController.history(), DocumentSearchService.findHistory() | DocumentControllerTest |
| UC-SD7 (쿼리 제한) | — | DocumentController (MAX_QUERY_LENGTH = 1000) | DocumentControllerTest |
| UC-SD8 (DB 인덱스) | — | V2__add_indexes.sql (4개 인덱스) | - |
| UC-SD9 (Export 스트리밍) | Export 스트리밍 | ExportController (exportJsonStreaming, exportCsvStreaming) | ExportControllerTest |
