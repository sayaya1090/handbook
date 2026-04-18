# Persist-Workspace 유스케이스

## 워크스페이스 생성 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (workspace-ui)
    participant GW as Gateway
    participant Ctrl as WorkspaceController
    participant Svc as WorkspaceService
    participant WRepo as WorkspaceRepository
    participant GRepo as GroupRepository
    participant DB as PostgreSQL
    participant Pub as WorkspaceEventPublisher
    participant Kafka as Kafka

    Client->>GW: POST /workspace
    Note over Client,GW: Content-Type: application/vnd.sayaya.handbook.v1+json
    GW->>Ctrl: @RequestBody CreateWorkspaceRequest, @AuthenticationPrincipal UserAuthentication
    Note over Ctrl: UserAuthentication.getPrincipal()=this 여야<br/>@AuthenticationPrincipal UserAuthentication 이 주입됨
    Ctrl->>Svc: create(principal, name, description)
    Svc->>Svc: Workspace(UUID.randomUUID(), name, description)
    Svc->>WRepo: save(workspace)
    WRepo->>DB: INSERT INTO workspaces
    DB-->>WRepo: 저장된 워크스페이스 엔티티
    WRepo-->>Svc: Workspace
    Svc->>GRepo: createAndAssign(workspace, principal, "Admin", null)
    GRepo->>DB: INSERT INTO groups (workspace, name="Admin")
    GRepo->>DB: INSERT INTO group_members (workspace, group="Admin", member=principal.id)
    DB-->>GRepo: Group
    Svc->>Pub: publishCreated(workspace)
    Pub->>Kafka: WorkspaceEvent 발행
    Svc-->>Ctrl: Mono<Workspace>
    Ctrl-->>Client: 200 OK + 생성된 워크스페이스
```

## 워크스페이스 삭제 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (workspace-ui)
    participant GW as Gateway
    participant Ctrl as WorkspaceController
    participant Svc as WorkspaceService
    participant WRepo as WorkspaceRepository
    participant DB as PostgreSQL
    participant Pub as WorkspaceEventPublisher
    participant Kafka as Kafka

    Client->>GW: DELETE /workspace/{id}
    GW->>Ctrl: @PathVariable id
    Ctrl->>Svc: delete(id)
    Svc->>WRepo: delete(id)
    WRepo->>DB: DELETE FROM workspaces WHERE id=:id
    DB-->>WRepo: 완료
    WRepo-->>Svc: Mono<Void>
    Svc->>Pub: publishDeleted(id)
    Pub->>Kafka: WorkspaceEvent 발행
    Svc-->>Ctrl: Mono<Void>
    Ctrl-->>Client: 204 No Content
```

## 메뉴 제공 시퀀스

```mermaid
sequenceDiagram
    participant GW as Gateway (MenuService)
    participant Ctrl as MenuController

    GW->>Ctrl: GET /menus (WebClient)
    Note over Ctrl: 정적 Menu 객체 반환
    Ctrl-->>GW: 200 OK + workspaces 메뉴
    Note over Ctrl: title="workspaces", order="S",<br/>icon="fa-briefcase", script="js/workspace/workspace.nocache.js",<br/>tools: workspace info, groups, permissions
```

---

## UC-PW1: 워크스페이스 생성

| 항목 | 내용 |
|------|------|
| **액터** | 인증된 사용자 (workspace-ui 경유) |
| **선행조건** | 사용자가 인증된 상태 (JWT) |
| **정상 흐름** | 1. 클라이언트가 `POST /workspace`로 name, description을 전송한다.<br>2. `WorkspaceController`가 `@AuthenticationPrincipal`로 인증된 사용자를 추출한다.<br>3. `WorkspaceService.create()`가 새 UUID를 생성하고 `WorkspaceRepository.save()`로 워크스페이스를 저장한다.<br>4. `GroupRepository.createAndAssign()`으로 "Admin" 그룹을 자동 생성하고, 생성자를 해당 그룹에 배정한다.<br>5. `WorkspaceEventPublisher.publishCreated()`로 생성 이벤트를 Kafka에 발행한다.<br>6. 생성된 워크스페이스가 응답으로 반환된다. |
| **결과** | 200 OK + 생성된 워크스페이스 (Admin 그룹 자동 생성 및 생성자 배정 완료) |

## UC-PW2: 워크스페이스 목록 조회

| 항목 | 내용 |
|------|------|
| **액터** | 인증된 사용자 |
| **선행조건** | 사용자가 인증된 상태 (JWT) |
| **정상 흐름** | 1. 클라이언트가 사용자가 참여 중인 워크스페이스 목록을 요청한다.<br>2. 인증 정보를 기반으로 사용자가 멤버로 속한 워크스페이스를 조회한다.<br>3. 워크스페이스 목록이 응답으로 반환된다. |
| **비고** | 현재 구현에서는 워크스페이스 목록 조회 전용 엔드포인트가 persist-workspace 모듈에 없으며, `login` 모듈의 `/user` 엔드포인트에서 사용자 정보와 함께 제공될 수 있다 |
| **결과** | 사용자가 참여 중인 워크스페이스 목록 |

## UC-PW3: 워크스페이스 참여

| 항목 | 내용 |
|------|------|
| **액터** | 인증된 사용자 |
| **선행조건** | 대상 워크스페이스가 존재하고 참여 가능한 상태 |
| **정상 흐름** | 1. 클라이언트가 기존 워크스페이스에 참여를 요청한다.<br>2. 사용자가 해당 워크스페이스의 그룹에 멤버로 추가된다.<br>3. 참여 완료 후 워크스페이스 정보가 반환된다. |
| **비고** | 현재 구현에서는 `GroupRepository.createAndAssign()`이 워크스페이스 생성 시 Admin 그룹 배정에 사용되며, 기존 워크스페이스 참여(join) 전용 엔드포인트는 미구현 상태이다 |
| **결과** | 사용자가 워크스페이스에 참여됨 |

## UC-PW4: 워크스페이스 삭제

| 항목 | 내용 |
|------|------|
| **액터** | 워크스페이스 관리자 (workspace-ui 경유) |
| **선행조건** | 삭제 대상 워크스페이스가 존재 |
| **정상 흐름** | 1. 클라이언트가 `DELETE /workspace/{id}`를 요청한다.<br>2. `WorkspaceController`가 `WorkspaceService.delete(id)`를 호출한다.<br>3. `WorkspaceRepository.delete()`로 워크스페이스를 삭제한다 (관련 데이터 cascade 삭제).<br>4. `WorkspaceEventPublisher.publishDeleted(id)`로 삭제 이벤트를 Kafka에 발행한다.<br>5. 204 No Content가 반환된다. |
| **결과** | 204 No Content |

---

## 트레이서빌리티 매트릭스

| UC | 요구사항 | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---------|---|---|---|
| UC-PW1 (생성) | 3.1 (워크스페이스 생성, Admin 그룹 자동 생성) | 워크스페이스 생성 | WorkspaceController, WorkspaceService, WorkspaceRepository, R2dbcWorkspaceRepositoryAdapter, GroupRepository, R2dbcGroupRepositoryAdapter, WorkspaceEventPublisher, KafkaWorkspaceEventPublisher | - |
| UC-PW2 (목록 조회) | 3.1 (참여 중인 워크스페이스 조회) | — (미구현) | — | - |
| UC-PW3 (참여) | 3.1 (기존 워크스페이스에 조인) | — (미구현) | GroupRepository | - |
| UC-PW4 (삭제) | 3.1 (워크스페이스 삭제, cascade) | 워크스페이스 삭제 | WorkspaceController, WorkspaceService, WorkspaceRepository, R2dbcWorkspaceRepositoryAdapter, WorkspaceEventPublisher, KafkaWorkspaceEventPublisher | - |
