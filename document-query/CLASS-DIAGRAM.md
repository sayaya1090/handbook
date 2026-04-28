# Search-Document 클래스 다이어그램

## Usecase 계층

```mermaid
classDiagram
    class DocumentService {
        -DocumentRepository repo
        +search(workspace: UUID, param: Search): Mono~Page~Document~~
        +find(workspace: UUID, type: String, serial: String, date: Instant?): Mono~Document~
        +fullTextSearch(workspace: UUID, q: String, page: Int, limit: Int): Mono~Page~Document~~
        +findHistory(workspace: UUID, type: String, serial: String): Flux~Document~
        +diff(workspace: UUID, type: String, serial: String, date1: Instant, date2: Instant): Mono~DiffResult~
        +findAllForExport(workspace: UUID, type: String?): Flux~Document~
    }

    class DocumentRepository {
        <<interface>>
        +search(workspace: UUID, param: Search): Mono~Page~Document~~
        +find(workspace: UUID, type: String, serial: String, date: Instant): Mono~Document~
        +fullTextSearch(workspace: UUID, q: String, page: Int, limit: Int): Mono~Page~Document~~
        +findHistory(workspace: UUID, type: String, serial: String): Flux~Document~
    }

    class Search {
        +page: Int
        +limit: Int
        +sortBy: String?
        +asc: Boolean?
        +filters: List~Pair~
    }

    DocumentService --> DocumentRepository
    DocumentService --> Search
```

## Interfaces 계층

```mermaid
classDiagram
    class DocumentController {
        -DocumentService svc
        +MAX_QUERY_LENGTH: Int$ = 1000
        +search(workspace: UUID, query: Search): Mono~Page~Document~~
        +find(workspace: UUID, type: String, serial: String, date: String?): Mono~Document~
        +fullTextSearch(workspace: UUID, q: String, page: Int, limit: Int): Mono~Page~Document~~
        +history(workspace: UUID, type: String, serial: String): Flux~Document~
        +diff(workspace: UUID, type: String, serial: String, date1: String, date2: String): Mono~DiffResult~
    }

    class MenuController {
        +MENU: Menu$
        +menus(): Flux~Menu~
    }

    class R2dbcDocumentEntity {
        +workspace: UUID
        +id: UUID
        +type: String
        +serial: String
        +effectDateTime: Instant
        +expireDateTime: Instant
        +createDateTime: Instant
        +creator: String
        +data: String
        +toDomain(): Document
    }

    class R2dbcDocumentRepository {
        -R2dbcEntityTemplate template
        -ObjectMapper objectMapper
        +search(workspace, param): Mono~Page~Document~~
        +find(workspace, type, serial, date): Mono~Document~
        -predicate(key, value): Criteria
    }

    class SearchDocumentConfig {
        <<Configuration>>
        +objectMapper(): ObjectMapper
        +documentRepository(): R2dbcDocumentRepository
        +documentService(): DocumentService
    }

    class ExportController {
        -DocumentSearchService svc
        -ObjectMapper objectMapper
        +export(workspace: UUID, format: String, query: Search, exchange: ServerWebExchange): Mono~Void~
        -exportJsonStreaming(workspace, query, exchange): Mono~Void~
        -exportCsvStreaming(workspace, query, exchange): Mono~Void~
    }

    class CsvSerializer {
        +serialize(documents: Flux~Document~): Flux~DataBuffer~
    }

    DocumentController --> DocumentService
    ExportController --> DocumentService
    ExportController --> CsvSerializer
    R2dbcDocumentRepository ..|> DocumentRepository
    R2dbcDocumentRepository --> R2dbcDocumentEntity
    SearchDocumentConfig ..> R2dbcDocumentRepository : creates
    SearchDocumentConfig ..> DocumentService : creates
```

## 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **CQRS (읽기 분리)** | document-query 전체 | document-command(쓰기)와 분리된 읽기 전용 서비스 |
| **Port & Adapter** | DocumentRepository | usecase의 포트를 R2dbcDocumentRepository가 구현 |
| **Criteria Builder** | R2dbcDocumentRepository.predicate() | 동적 검색 조건을 Criteria 객체로 조합 |
| **Menu Provider** | MenuController | Gateway가 수집하는 메뉴 정보 제공 |
| **Input Validation** | DocumentController (MAX_QUERY_LENGTH) | 검색 쿼리 길이 제한으로 보안 강화 |
| **Streaming Export** | ExportController | ServerWebExchange 직접 write로 메모리 최적화된 내보내기 |
