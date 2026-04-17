# search-workspace USECASE

이 모듈이 참여하는 유스케이스와 트레이서빌리티.

## 트레이서빌리티 매트릭스

| UC | 제목 | 구현체 | 상태 |
|----|------|--------|------|
| UC-04 | 워크스페이스 홈 화면 진입 (메뉴 렌더링) | `MenuController.menus(Principal?)` | 구현 (비인증 빈 목록) |
| UC-05 | 워크스페이스 전환 | 간접 (메뉴 재렌더링 + `/workspaces` 조회) | 구현 |
| UC-06 | 워크스페이스 참여 | — (persist-workspace 담당) | 해당 없음 |
| UC-10 | 워크스페이스 생성 | — (persist-workspace) | 해당 없음 |
| UC-11 | 워크스페이스 삭제 | — (persist-workspace) | 해당 없음 |
| UC-85 | 외부 AI Tool Use (`list_workspace_menu`, `list_workspaces`) | `MenuController` + `WorkspaceController` + OpenAPI `/v3/api-docs` | 구현 |

## UC-04 시퀀스 — 메뉴 공급

```mermaid
sequenceDiagram
    participant Browser
    participant Gateway
    participant MenuService
    participant SW as search-workspace
    participant Others as 기타 MenuSupplier

    Browser->>Gateway: GET /menus
    Gateway->>MenuService: menus(headers)
    par 병렬 집계
        MenuService->>SW: GET /menus
        SW-->>MenuService: [workspaces]
    and
        MenuService->>Others: GET /menus
        Others-->>MenuService: [login, types, documents, ...]
    end
    MenuService->>MenuService: order 기준 정렬
    MenuService-->>Gateway: 정렬된 Menu Flux
    Gateway-->>Browser: 200 OK + 집계 메뉴
```

## 테스트 매핑

| UC | 테스트 | 위치 |
|----|--------|------|
| UC-04 (메뉴 공급) | `MenuControllerTest` | `src/test/kotlin/.../MenuControllerTest.kt` |
| UC-81 / UC-85 (에이전트 연동) | `MenuControllerTest` (OpenAPI 어노테이션 존재 검증은 `/v3/api-docs` 통합 테스트 후속) | 동일 파일 |

## 에이전트 연동 시나리오

### 시나리오 1 — 내부 assistant 의 navigate (UC-81 연계)

사용자가 assistant 에게 **"권한 설정 화면 열어줘"** 요청 → assistant 가 실행
계획을 세우고 `AGENT_COMMAND` navigate 를 발행.

```mermaid
sequenceDiagram
    participant U as 사용자
    participant AS as assistant
    participant K as Kafka (handbook-events)
    participant SSE as event-broadcaster
    participant AUI as agent-ui
    participant SU as shell-ui
    participant SW as search-workspace

    Note over U,SW: shell-ui 는 시작 시 이미 /menus 를 집계해 MenuList 구독 중
    SU->>SW: GET /menus (최초 1회)
    SW-->>SU: [workspaces menu]

    U->>AS: "권한 설정 열어줘"
    AS->>AS: 실행 계획 — navigate{menu:"workspaces", tool:"permissions"}
    AS->>K: AGENT_COMMAND 이벤트 발행
    K->>SSE: 토픽 소비
    SSE-->>AUI: SSE push
    AUI->>SU: CustomEvent (agent-bridge)
    SU->>SU: UrlBasedMenuResolver — ^workspaces URL 로 메뉴 선택
    SU-->>U: 워크스페이스 permissions 화면 렌더
```

### 시나리오 2 — 외부 AI 의 Tool Use (UC-85 연계)

외부 AI 가 `/openapi.json` 을 읽고 function calling 으로 `/menus` 를 조회하여
워크스페이스 기능을 사용자에게 요약 설명.

```mermaid
sequenceDiagram
    participant ExtAI as 외부 AI
    participant GW as gateway
    participant SW as search-workspace

    ExtAI->>GW: GET /openapi.json (with PAT)
    GW->>SW: OpenAPI 스펙 집계 (springdoc)
    SW-->>GW: /menus Operation (summary, description)
    GW-->>ExtAI: 병합된 OpenAPI 3.0 문서
    ExtAI->>ExtAI: function signature 파싱
    ExtAI->>GW: GET /menus (Authorization: Bearer <PAT>)
    GW->>GW: JWT 검증 + caller_type=EXTERNAL_AGENT 감사 엔트리 발행
    GW->>SW: forward
    SW-->>GW: [workspaces menu]
    GW-->>ExtAI: Menu JSON
    ExtAI-->>사용자: "워크스페이스 기능은 info/groups/permissions 3가지입니다..."
```

## 후속 확장 (미구현)

- 사용자가 속한 워크스페이스만 필터링 (현재 `findAll` — 권한 필터링은 auth-expert 와 조율 후 추가)
- `GET /workspaces/{id}/groups` — 그룹 목록 (현재 persist-workspace 에서만 조회)
- `GET /workspaces/{id}/permissions` — 권한 매트릭스 조회
- `operationId` 스네이크_케이스 통일 (현재 `list`/`get` → `list_workspaces`/`get_workspace`)

gateway 라우트 `search-workspace` (Path=`/workspaces,/workspaces/**`, Method=GET) 는 이미 등록되어 있다.
