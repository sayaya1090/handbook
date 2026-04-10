# Persist-Type 유스케이스

## 타입 저장 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (type-ui)
    participant GW as Gateway
    participant Ctrl as TypeController
    participant Svc as TypeService
    participant Repo as TypeRepository
    participant AttrRepo as R2dbcAttributeEntityRepository
    participant DB as PostgreSQL
    participant Pub as TypeEventPublisher
    participant Kafka as Kafka

    Client->>GW: PUT /workspace/{id}/types
    Note over Client,GW: Content-Type: application/vnd.sayaya.handbook.v1+json
    GW->>Ctrl: @RequestBody List<Type>
    Ctrl->>Svc: save(workspace, types)
    Svc->>Repo: save(workspace, types)
    loop 각 타입마다 (saveOne)
        Repo->>Repo: Type → R2dbcTypeEntity 변환
        Repo->>DB: INSERT/UPDATE types 테이블
        DB-->>Repo: 저장된 타입 엔티티
        Repo->>AttrRepo: deleteByTypeIdAndTypeVersion (기존 속성 삭제)
        AttrRepo->>DB: DELETE FROM type_attributes
        Repo->>Repo: Attribute → R2dbcAttributeEntity 변환
        Note over Repo: attributeType → ObjectMapper JSON 직렬화
        Repo->>AttrRepo: saveAll(속성 엔티티 목록)
        AttrRepo->>DB: INSERT type_attributes
        DB-->>Repo: 저장된 속성
    end
    Note over Repo: TransactionalOperator로 전체 트랜잭션 관리
    Repo-->>Svc: Flux<Type>
    Svc->>Pub: publishCreated(workspace, type) (각 타입마다)
    Pub->>Kafka: TypeEvent(TYPE_CREATED) → "handbook-events"
    Note over Pub,Kafka: 파티션 키: workspace UUID
    Svc-->>Ctrl: Flux<Type>
    Ctrl-->>Client: 200 OK + 저장된 타입 목록
```

## 타입 삭제 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (type-ui)
    participant GW as Gateway
    participant Ctrl as TypeController
    participant Svc as TypeService
    participant Repo as TypeRepository
    participant AttrRepo as R2dbcAttributeEntityRepository
    participant DB as PostgreSQL
    participant Pub as TypeEventPublisher
    participant Kafka as Kafka

    Client->>GW: DELETE /workspace/{id}/types
    Note over Client,GW: Content-Type: application/vnd.sayaya.handbook.v1+json
    GW->>Ctrl: @RequestBody List<Type>
    Ctrl->>Svc: delete(workspace, types)
    Svc->>Repo: delete(workspace, types)
    loop 각 타입마다
        Repo->>AttrRepo: deleteByTypeIdAndTypeVersion
        AttrRepo->>DB: DELETE FROM type_attributes
        Repo->>DB: DELETE FROM types
    end
    Note over Repo: TransactionalOperator로 전체 트랜잭션 관리
    DB-->>Repo: 완료
    Repo-->>Svc: Mono<Void>
    Svc->>Pub: publishDeleted(workspace, type) (각 타입마다)
    Pub->>Kafka: TypeEvent(TYPE_DELETED) → "handbook-events"
    Svc-->>Ctrl: Mono<Void>
    Ctrl-->>Client: 204 No Content
```

## 레이아웃 저장 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (type-ui)
    participant GW as Gateway
    participant Ctrl as LayoutController
    participant Svc as LayoutService
    participant Repo as LayoutRepository
    participant DB as PostgreSQL

    Client->>GW: PUT /workspace/{id}/layouts
    Note over Client,GW: Content-Type: application/vnd.sayaya.handbook.v1+json
    GW->>Ctrl: @RequestBody TypeLayout
    Ctrl->>Svc: save(workspace, layout)
    Svc->>Repo: save(workspace, layout)
    Repo->>Repo: TypeLayout → R2dbcLayoutEntity 변환
    Note over Repo: positions Map → ObjectMapper JSON 직렬화
    Repo->>DB: INSERT/UPDATE type_layouts
    DB-->>Repo: 저장된 레이아웃 엔티티
    Repo->>Repo: R2dbcLayoutEntity → TypeLayout 변환
    Note over Repo: positions JSON → Map<String, Position> 역직렬화
    Repo-->>Svc: Mono<TypeLayout>
    Svc-->>Ctrl: Mono<TypeLayout>
    Ctrl-->>Client: 200 OK + 저장된 레이아웃
```

## 타입 조회 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (type-ui)
    participant GW as Gateway
    participant Ctrl as TypeController
    participant Svc as TypeService
    participant Repo as TypeRepository
    participant AttrRepo as R2dbcAttributeEntityRepository
    participant DB as PostgreSQL

    Client->>GW: GET /workspace/{id}/types?effect_date_time=&expire_date_time=
    GW->>Ctrl: @RequestParam effectDateTime, expireDateTime
    Ctrl->>Svc: findByPeriod(workspace, effectDateTime, expireDateTime)
    Svc->>Repo: findByWorkspaceAndPeriod(workspace, effectDateTime, expireDateTime)
    Repo->>DB: SELECT * FROM types WHERE workspace=:w AND 기간 겹침
    DB-->>Repo: List<R2dbcTypeEntity>
    Repo->>AttrRepo: findByWorkspaceAndTypeIdIn(workspace, typeIds)
    AttrRepo->>DB: SELECT * FROM type_attributes
    DB-->>AttrRepo: List<R2dbcAttributeEntity>
    Repo->>Repo: typeId:version 키로 속성 그룹핑
    Repo->>Repo: R2dbcTypeEntity + Attributes → Type 도메인 변환
    Note over Repo: attributeType JSON → AttributeType 역직렬화
    Repo-->>Svc: Flux<Type>
    Svc-->>Ctrl: Flux<Type>
    Ctrl-->>Client: 200 OK + 타입 목록 (속성 포함)
```

---

## UC-PT1: 타입 조회 (기간별)

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (type-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{id}/types?effect_date_time=&expire_date_time=`로 기간을 전달한다.<br>2. `TypeService.findByPeriod()`가 `TypeRepository.findByWorkspaceAndPeriod()`를 호출한다.<br>3. 타입 테이블에서 기간이 겹치는 타입을 조회하고, 해당 타입의 속성을 일괄 조회한다.<br>4. `attributeType` JSONB를 `AttributeType`으로 역직렬화하여 도메인 객체로 변환한다.<br>5. 속성이 포함된 타입 목록이 응답으로 반환된다. |
| **결과** | 200 OK + 기간에 해당하는 타입 목록 (속성 포함) |

## UC-PT2: 타입 저장

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (type-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유 |
| **정상 흐름** | 1. 클라이언트가 `PUT /workspace/{id}/types`로 타입 목록을 전송한다.<br>2. `TypeService.save()`가 `TypeRepository.save()`를 호출한다.<br>3. 각 타입마다 `R2dbcTypeEntity`로 변환하여 types 테이블에 upsert한다.<br>4. 기존 속성을 삭제하고(`deleteByTypeIdAndTypeVersion`) 새 속성을 일괄 삽입한다.<br>5. `attributeType`은 `ObjectMapper`로 JSON 직렬화하여 JSONB 컬럼에 저장된다.<br>6. 전체 과정이 `TransactionalOperator`로 트랜잭션 관리된다.<br>7. 저장 후 각 타입에 대해 `TYPE_CREATED` 이벤트가 Kafka로 발행된다.<br>8. 저장된 타입 목록이 응답으로 반환된다. |
| **결과** | 200 OK + 저장된 타입 목록 |

## UC-PT3: 타입 삭제

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (type-ui 경유) |
| **선행조건** | 삭제 대상 타입이 존재 |
| **정상 흐름** | 1. 클라이언트가 `DELETE /workspace/{id}/types`로 삭제 대상 타입 목록을 전송한다.<br>2. `TypeService.delete()`가 `TypeRepository.delete()`를 호출한다.<br>3. 각 타입마다 속성을 먼저 삭제하고(`deleteByTypeIdAndTypeVersion`), 타입을 삭제한다.<br>4. 전체 과정이 `TransactionalOperator`로 트랜잭션 관리된다.<br>5. 삭제 후 각 타입에 대해 `TYPE_DELETED` 이벤트가 Kafka로 발행된다.<br>6. 204 No Content가 반환된다. |
| **결과** | 204 No Content |

## UC-PT4: 레이아웃 기간 목록 조회

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (type-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{id}/layouts`로 요청한다.<br>2. `LayoutService.findByWorkspace()`가 `LayoutRepository.findByWorkspace()`를 호출한다.<br>3. type_layouts 테이블에서 워크스페이스의 모든 레이아웃을 조회한다.<br>4. `positions` JSONB를 `Map<String, Position>`으로 역직렬화한다.<br>5. `TypeLayout` 목록이 응답으로 반환된다. |
| **결과** | 200 OK + 레이아웃 기간 목록 |

## UC-PT5: 레이아웃 저장

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 (type-ui 경유) |
| **선행조건** | 워크스페이스 접근 권한 보유 |
| **정상 흐름** | 1. 클라이언트가 `PUT /workspace/{id}/layouts`로 레이아웃을 전송한다.<br>2. `LayoutService.save()`가 `LayoutRepository.save()`를 호출한다.<br>3. `positions` Map을 `ObjectMapper`로 JSON 직렬화하여 JSONB 컬럼에 저장한다.<br>4. 저장된 레이아웃이 응답으로 반환된다. |
| **결과** | 200 OK + 저장된 레이아웃 |

## UC-PT6: 타입 이벤트 발행

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (TypeService 내부) |
| **선행조건** | 타입 저장 또는 삭제 완료 |
| **정상 흐름** | 1. `KafkaTypeEventPublisher`가 `TypeEvent`를 생성한다.<br>2. `"handbook-events"` 토픽에 workspace UUID를 파티션 키로 발행한다.<br>3. `event-broadcaster`가 이벤트를 수신하여 SSE로 UI에 전파한다. |

---

## 트레이서빌리티 매트릭스

| UC | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---|---|---|
| UC-PT1 (조회) | 타입 조회 | TypeController, TypeService, TypeRepository, R2dbcTypeRepositoryAdapter, R2dbcAttributeEntityRepository | - |
| UC-PT2 (저장) | 타입 저장 | TypeController, TypeService, TypeRepository, R2dbcTypeRepositoryAdapter, R2dbcTypeEntity, R2dbcAttributeEntity, R2dbcAttributeEntityRepository, KafkaTypeEventPublisher | - |
| UC-PT3 (삭제) | 타입 삭제 | TypeController, TypeService, TypeRepository, R2dbcTypeRepositoryAdapter, R2dbcAttributeEntityRepository, KafkaTypeEventPublisher | - |
| UC-PT4 (레이아웃 조회) | — (단순) | LayoutController, LayoutService, LayoutRepository, R2dbcLayoutRepositoryAdapter, R2dbcLayoutEntity | - |
| UC-PT5 (레이아웃 저장) | 레이아웃 저장 | LayoutController, LayoutService, LayoutRepository, R2dbcLayoutRepositoryAdapter, R2dbcLayoutEntity | - |
| UC-PT6 (이벤트) | 타입 저장/삭제 (후반) | KafkaTypeEventPublisher, TypeEvent | - |
