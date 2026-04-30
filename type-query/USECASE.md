# Search-Type 유스케이스

## 타입 목록 조회 시퀀스

```mermaid
sequenceDiagram
    actor Client as "클라이언트 (type-ui)"
    participant GW as Gateway
    participant Ctrl as TypeController
    participant Svc as TypeService
    participant Repo as TypeRepository
    participant DB as PostgreSQL

    Client->>GW: "GET /workspaces/{workspaceId}/types?effect_date_time=&expire_date_time="
    GW->>Ctrl: "@PathVariable workspace, @RequestParam effectDateTime?, expireDateTime?"
    alt "effectDateTime이 존재"
        Ctrl->>Svc: "findByRange(workspace, effectDateTime, expireDateTime)"
        Svc->>Repo: "findByRange(workspace, effectDateTime, expireDateTime)"
        Note over Svc,Repo: "expireDateTime이 null이면 effectDateTime으로 대체"
        Repo->>DB: "SELECT * FROM types WHERE workspace=:w AND 기간 겹침"
    else "effectDateTime이 null"
        Ctrl->>Svc: "findByRange(workspace, null, null)"
        Svc->>Repo: "findAll(workspace)"
        Repo->>DB: "SELECT * FROM types WHERE workspace=:w"
    end
    DB-->>Repo: "List<Type>"
    Repo-->>Svc: "Flux<Type>"
    Svc-->>Ctrl: "Flux<Type>"
    Ctrl-->>Client: "200 OK + 타입 목록"
```

## 레이아웃 기간 목록 조회 시퀀스

```mermaid
sequenceDiagram
    actor Client as "클라이언트 (type-ui)"
    participant GW as Gateway
    participant Ctrl as LayoutController
    participant Svc as LayoutService
    participant Repo as LayoutRepository
    participant DB as PostgreSQL

    Client->>GW: "GET /workspaces/{workspaceId}/layouts"
    GW->>Ctrl: "@PathVariable workspace"
    Ctrl->>Svc: "findByWorkspace(workspace)"
    Svc->>Repo: "findByWorkspace(workspace)"
    Repo->>DB: "SELECT * FROM type_layouts WHERE workspace=:w"
    DB-->>Repo: "List<TypeLayout>"
    Repo-->>Svc: "Flux<TypeLayout>"
    Svc-->>Ctrl: "Flux<TypeLayout>"
    Ctrl-->>Client: "200 OK + 레이아웃 기간 목록"
```

## 메뉴 제공 시퀀스

```mermaid
sequenceDiagram
    participant GW as "Gateway (MenuService)"
    participant Ctrl as MenuController

    GW->>Ctrl: "GET /menus (WebClient)"
    Note over Ctrl: "정적 Menu 객체 반환"
    Ctrl-->>GW: "200 OK + types 메뉴"
    Note over Ctrl: "title='types', order='B',<br/>icon='fa-cubes', script='js/type/type.nocache.js',<br/>url='/workspaces/{workspaceId}/types',<br/>urlRegex='^/workspaces/\\{workspaceId\\}/types$'"
```

---

...
| UC-ST3 (메뉴 제공) | 3.11 (Shell - Menu Rail) | 메뉴 제공 | MenuController | - |
| UC-ST4 (버전 히스토리 조회) | 6.12 | — | TypeController.versions(), TypeSearchService.findVersions() | ❌ 테스트 미작성 (구현 완료) |
