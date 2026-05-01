# Workspace-Query 모듈

워크스페이스 도메인의 **조회(read-side)** 서비스 (CQRS Read). Gateway가 메뉴를 수집하여 Shell 드로어에 워크스페이스 목록을 노출한다.

## 역할 경계

| 모듈 | 역할 | 주 메서드 |
|------|------|----------|
| `workspace-command` | 쓰기 (CUD) + 웹훅 등록/삭제 | POST/PUT/DELETE |
| `workspace-query` | 읽기 + 메뉴 공급 | GET `/menus`, `/workspaces` |
| `workspace` | 도메인 모델 + 유스케이스 | - |

역할 분리는 다른 도메인(type, document)이 이미 따르는 패턴(`type-query`/`type-command`,
`document-query`/`document-command`)과 일관화를 위한 것이다.

## API 엔드포인트

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| GET | `/menus` | 워크스페이스 메뉴 엔트리 (Drawer 하단 고정) | **비인증 시 빈 목록**, 인증 시 workspaces 메뉴 |
| GET | `/workspaces` | 사용자가 소속된 워크스페이스 목록 | 필수 (JWT) |
| GET | `/workspaces/{id}` | 특정 워크스페이스 상세 정보 | 필수 (JWT) |
| GET | `/workspaces/{ws}/groups` | 그룹 목록 및 멤버 조회 | 필수 (JWT) |

`application/vnd.sayaya.handbook.v1+json` 미디어 타입으로 응답한다.
Gateway `MenuService` 가 WebClient 로 `/menus` 를 집계하며, `/workspaces*` 요청은 Gateway가 이 서비스로 포워딩한다.

**읽기 전용 세션:** DB 연결 URL 에 `options=-c default_transaction_read_only=on`
을 포함하여 PostgreSQL 세션이 모든 트랜잭션을 READ ONLY 로 강제한다.

## 구조

```
├── usecase/         WorkspaceSearchService, WorkspaceReadRepository, GroupReadRepository
└── interfaces/
    ├── api/         WorkspaceController (GET), GroupSearchController, MenuController
    ├── database/    R2dbcWorkspaceEntity, R2dbcGroupEntity (Read-only adapters)
    └── config/      SearchWorkspaceSecurityConfig
```

## 에이전트 연동

### 내부 assistant
- 호출 경로: `AGENT_COMMAND` navigate (`target.menu="workspaces"`)
- 시나리오: 사용자가 assistant 에 "워크스페이스 권한 설정 화면 열어줘" 요청
  → assistant 가 `navigate { menu: "workspaces", tool: "permissions" }` 커맨드 발행

### 외부 AI (Tool Use)
- 노출 엔드포인트:
  - `GET /workspaces` — 워크스페이스 ID 목록 획득 (모든 후속 도메인 작업의 근거)
  - `GET /workspaces/{id}` — 단건 정보 조회
- OpenAPI `summary` / `description` 기입 위치: `WorkspaceController` 메서드

### Agent Command 타겟
- navigate: `workspaces`, `menus`
- highlight/mutate selector 패턴: `.workspace-list-item`, `.menu-entry`

## 실행

```bash
./gradlew :workspace-query:bootRun
./gradlew :workspace-query:test
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
