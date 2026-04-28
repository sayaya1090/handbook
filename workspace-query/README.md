# workspace-query

워크스페이스 도메인의 **조회(read-side)** 서비스. 현재는 `/menus` 공급자 역할만 수행하며,
후속 반복에서 워크스페이스·그룹·권한 목록 조회 API 가 추가된다.

## 역할 경계

| 모듈 | 역할 | 주 메서드 |
|------|------|----------|
| `workspace-command` | 쓰기 (CUD) + 웹훅 등록/삭제 | POST/PUT/DELETE |
| `workspace-query` | 읽기 + 메뉴 공급 | GET `/menus` |
| `workspace` | 도메인 모델 + 유스케이스 | - |

역할 분리는 다른 도메인(type, document)이 이미 따르는 패턴(`type-query`/`type-command`,
`document-query`/`document-command`)과 일관화를 위한 것이다.

## 현재 엔드포인트

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| GET | `/menus` | 워크스페이스 메뉴 엔트리 (Drawer 하단 고정) | **비인증 시 빈 목록**, 인증 시 workspaces 메뉴 |
| GET | `/workspaces` | 사용자에게 보이는 워크스페이스 목록 | 필수 (JWT) |
| GET | `/workspaces/{id}` | 워크스페이스 단건 조회 | 필수 (JWT) |
| GET | `/v3/api-docs` | OpenAPI 3.0 스펙 (springdoc) — 외부 AI Tool Use 디스커버리 | 불필요 |

`application/vnd.sayaya.handbook.v1+json` 미디어 타입으로 응답한다.
gateway `MenuService` 가 WebClient 로 `/menus` 를 집계하고, `/workspaces*` 는
gateway 의 `workspace-query` 라우트 (Method=GET) 가 이 서비스로 포워딩한다.

**읽기 전용 세션:** DB 연결 URL 에 `options=-c default_transaction_read_only=on`
을 포함하여 PostgreSQL 세션이 모든 트랜잭션을 READ ONLY 로 강제한다.
실수로 write 구현이 추가되더라도 DB 레벨에서 거부된다 (안전장치).

## 실행

```bash
./gradlew :workspace-query:bootRun    # 기본 8080
./gradlew :workspace-query:test       # 단위 테스트
```

프로덕션 port 는 gateway ConfigMap 의 `services` 목록에서
`service-workspace-query:8080` 으로 지정된다.

## 에이전트 연동

### 내부 assistant
- **호출 경로**: `AGENT_COMMAND` navigate — `target.menu="workspaces"` 로 이 메뉴를 선택
- **시나리오**: 사용자가 assistant 에 "워크스페이스 권한 설정 화면 열어줘" 요청
  → assistant 가 `navigate { menu: "workspaces", tool: "permissions" }` 커맨드 발행
  → shell-ui `UrlBasedMenuResolver` 가 `^workspaces` URL 매칭으로 자동 선택

### 외부 AI (Tool Use)
- **노출 엔드포인트**:
  - `GET /menus` — 메뉴 구조 디스커버리 (비인증은 빈 목록)
  - `GET /workspaces` — 워크스페이스 ID 목록 획득 (후속 tool 호출의 `workspace` 파라미터 결정 근거)
  - `GET /workspaces/{id}` — 단건 정보 조회
- **OpenAPI 기입 위치**: `MenuController.menus()`, `WorkspaceController.list()/get()`
  각각에 `@Operation(summary, description)` (`/v3/api-docs` 공개)
- **용도**:
  - 외부 AI 가 `list_workspace_menu` → `list_workspaces` 순으로 호출하여
    컨텍스트 파악 후 도메인 tool (`list_types`, `search_documents`) 에 workspace id 전달
- **감사 경로**: `/workspaces*` 호출은 gateway 에서 인증·PAT 검증 후
  `caller_type=EXTERNAL_AGENT/USER` AuditEntry 발행 → `docs/contracts/audit.md`

### (후속) MCP
- **Tool 매니페스트 후보**: `list_workspace_menu` — 워크스페이스 드로어 엔트리 조회
- mcp-server 구현 시 이 엔드포인트를 MCP tool 로 래핑 (구현 위치 미정)

### Agent Command 타겟
- **navigate**: `target.menu="workspaces"`, `target.tool ∈ {"workspace info", "groups", "permissions"}`
- **URL 정규식**: `^workspaces` — highlight/mutate 는 후속 확장 (정보/그룹/권한 화면 구현 후)

## 관련 문서

- [USECASE.md](USECASE.md) — UC 트레이서빌리티 + 에이전트 연동 시나리오
- [CLASS-DIAGRAM.md](CLASS-DIAGRAM.md) — 클래스 구조
- `docs/contracts/menus.md` — Menu 계약 (공급자 인벤토리)
- `docs/contracts/api.md` — 외부 AI 노출 엔드포인트 카탈로그
- `docs/contracts/agent-commands.md` — navigate/mutate 커맨드 프로토콜
- `docs/requirements.md` §3.1 워크스페이스, §3.23.2 Tool Use
