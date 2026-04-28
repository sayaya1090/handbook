# Persist-Type 유스케이스

## 타입 저장 시퀀스

```mermaid
sequenceDiagram
    actor Client as "클라이언트 (type-ui)"
    participant GW as Gateway
    participant Ctrl as TypeController
    participant Svc as TypeService
    participant Repo as TypeRepository
    participant AttrRepo as R2dbcAttributeEntityRepository
    participant DB as PostgreSQL
    participant Pub as TypeEventPublisher
    participant Kafka as Kafka

    Client->>GW: "PUT /workspace/{id}/types"
    Note over Client,GW: "Content-Type: application/vnd.sayaya.handbook.v1+json"
    GW->>Ctrl: "@RequestBody List<Type>"
    Ctrl->>Svc: "save(workspace, types)"
    Svc->>Repo: "save(workspace, types)"
    loop "각 타입마다 (saveOne)"
        Repo->>Repo: "Type → R2dbcTypeEntity 변환"
        Repo->>DB: "INSERT/UPDATE types 테이블"
        DB-->>Repo: "저장된 타입 엔티티"
        Repo->>AttrRepo: "deleteByTypeIdAndTypeVersion (기존 속성 삭제)"
        AttrRepo->>DB: "DELETE FROM type_attributes"
        Repo->>Repo: "Attribute → R2dbcAttributeEntity 변환"
        Note over Repo: "attributeType → ObjectMapper JSON 직렬화"
        Repo->>AttrRepo: "saveAll(속성 엔티티 목록)"
        AttrRepo->>DB: "INSERT type_attributes"
        DB-->>Repo: "저장된 속성"
    end
    Note over Repo: "TransactionalOperator로 전체 트랜잭션 관리"
    Repo-->>Svc: "Flux<Type>"
    Svc->>Pub: "publishCreated(workspace, type) (각 타입마다)"
    Pub->>Kafka: "TypeEvent(TYPE_CREATED) → 'handbook-events'"
    Note over Pub,Kafka: "파티션 키: workspace UUID"
    Svc-->>Ctrl: "Flux<Type>"
    Ctrl-->>Client: "200 OK + 저장된 타입 목록"
```

## 타입 삭제 시퀀스

```mermaid
sequenceDiagram
    actor Client as "클라이언트 (type-ui)"
    participant GW as Gateway
    participant Ctrl as TypeController
    participant Svc as TypeService
    participant Repo as TypeRepository
    participant AttrRepo as R2dbcAttributeEntityRepository
    participant DB as PostgreSQL
    participant Pub as TypeEventPublisher
    participant Kafka as Kafka

    Client->>GW: "DELETE /workspace/{id}/types"
    Note over Client,GW: "Content-Type: application/vnd.sayaya.handbook.v1+json"
    GW->>Ctrl: "@RequestBody List<Type>"
    Ctrl->>Svc: "delete(workspace, types)"
    Svc->>Repo: "delete(workspace, types)"
    loop "각 타입마다"
        Repo->>AttrRepo: "deleteByTypeIdAndTypeVersion"
        AttrRepo->>DB: "DELETE FROM type_attributes"
        Repo->>DB: "DELETE FROM types"
    end
    Note over Repo: "TransactionalOperator로 전체 트랜잭션 관리"
    DB-->>Repo: "완료"
    Repo-->>Svc: "Mono<Void>"
    Svc->>Pub: "publishDeleted(workspace, type) (각 타입마다)"
    Pub->>Kafka: "TypeEvent(TYPE_DELETED) → 'handbook-events'"
    Svc-->>Ctrl: "Mono<Void>"
    Ctrl-->>Client: "204 No Content"
```

## 레이아웃 저장 시퀀스

```mermaid
sequenceDiagram
    actor Client as "클라이언트 (type-ui)"
    participant GW as Gateway
    participant Ctrl as LayoutController
    participant Svc as LayoutService
    participant Repo as LayoutRepository
    participant DB as PostgreSQL

    Client->>GW: "PUT /workspace/{id}/layouts"
    Note over Client,GW: "Content-Type: application/vnd.sayaya.handbook.v1+json"
    GW->>Ctrl: "@RequestBody TypeLayout"
    Ctrl->>Svc: "save(workspace, layout)"
    Svc->>Repo: "save(workspace, layout)"
    Repo->>Repo: "TypeLayout → R2dbcLayoutEntity 변환"
    Note over Repo: "positions Map → ObjectMapper JSON 직렬화"
    Repo->>DB: "INSERT/UPDATE type_layouts"
    DB-->>Repo: "저장된 레이아웃 엔티티"
    Repo->>Repo: "R2dbcLayoutEntity → TypeLayout 변환"
    Note over Repo: "positions JSON → Map<String, Position> 역직렬화"
    Repo-->>Svc: "Mono<TypeLayout>"
    Svc-->>Ctrl: "Mono<TypeLayout>"
    Ctrl-->>Client: "200 OK + 저장된 레이아웃"
```

## 타입 조회 시퀀스

```mermaid
sequenceDiagram
    actor Client as "클라이언트 (type-ui)"
    participant GW as Gateway
    participant Ctrl as TypeController
    participant Svc as TypeService
    participant Repo as TypeRepository
    participant AttrRepo as R2dbcAttributeEntityRepository
    participant DB as PostgreSQL

    Client->>GW: "GET /workspace/{id}/types?effect_date_time=&expire_date_time="
    GW->>Ctrl: "@RequestParam effectDateTime, expireDateTime"
    Ctrl->>Svc: "findByPeriod(workspace, effectDateTime, expireDateTime)"
    Svc->>Repo: "findByWorkspaceAndPeriod(workspace, effectDateTime, expireDateTime)"
    Repo->>DB: "SELECT * FROM types WHERE workspace=:w AND 기간 겹침"
    DB-->>Repo: "List<R2dbcTypeEntity>"
    Repo->>AttrRepo: "findByWorkspaceAndTypeIdIn(workspace, typeIds)"
    AttrRepo->>DB: "SELECT * FROM type_attributes"
    DB-->>AttrRepo: "List<R2dbcAttributeEntity>"
    Repo->>Repo: "typeId:version 키로 속성 그룹핑"
    Repo->>Repo: "R2dbcTypeEntity + Attributes → Type 도메인 변환"
    Note over Repo: "attributeType JSON → AttributeType 역직렬화"
    Repo-->>Svc: "Flux<Type>"
    Svc-->>Ctrl: "Flux<Type>"
    Ctrl-->>Client: "200 OK + 타입 목록 (속성 포함)"
```

---

...
| UC-PT5 (레이아웃 저장) | 레이아웃 저장 | LayoutController, LayoutService, LayoutRepository, R2dbcLayoutRepositoryAdapter, R2dbcLayoutEntity | - |
| UC-PT6 (이벤트) | 타입 저장/삭제 (후반) | KafkaTypeEventPublisher, TypeEvent | - |
