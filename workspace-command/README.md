# Workspace-Command 모듈

워크스페이스 CRUD 백엔드 서비스. 워크스페이스 생성 시 Admin 그룹을 자동 생성하고 생성자를 배정한다.

## 계층 구조

```
├── usecase/         WorkspaceService, WorkspaceRepository, GroupRepository, RoleRepository, WebhookService, WorkspaceEventPublisher
└── interfaces/
    ├── api/         WorkspaceController (POST/PUT/DELETE /workspaces), GroupController, RoleController, WebhookController
    ├── database/    R2dbcWorkspaceEntity (@Version), R2dbcGroupEntity, R2dbcGroupMemberEntity, R2dbcWebhookEntity
    ├── event/       KafkaWorkspaceEventPublisher
    └── config/      WorkspaceConfig (Bean 등록, @EnableR2dbcAuditing)
```

## API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| POST | `/workspaces` | 워크스페이스 생성 (자동 Admin 그룹 + 생성자 배정) |
| PUT | `/workspaces/{id}` | 워크스페이스 수정 (이름, 설명) |
| DELETE | `/workspaces/{id}` | 워크스페이스 삭제 |
| POST | `/workspaces/{id}/join` | 워크스페이스 참여 요청 |
| POST | `/workspaces/{ws}/groups` | 그룹 생성 |
| DELETE | `/workspaces/{ws}/groups/{gid}` | 그룹 삭제 |
| POST | `/workspaces/{ws}/groups/{gid}/members/{uid}` | 그룹 멤버 추가 |
| DELETE | `/workspaces/{ws}/groups/{gid}/members/{uid}` | 그룹 멤버 삭제 |
| POST | `/workspaces/{ws}/groups/{gid}/roles` | 역할 부여 |
| DELETE | `/workspaces/{ws}/groups/{gid}/roles/{role}` | 역할 제거 |
| POST | `/workspaces/{workspace}/webhooks` | 웹훅 등록 |
| DELETE | `/workspaces/{workspace}/webhooks/{id}` | 웹훅 삭제 |

## 설계 결정

| 결정 | 이유 |
|------|------|
| 생성 시 Admin 그룹 자동 생성 | 생성자에게 즉시 관리 권한 부여 |
| 생성자를 Admin 그룹에 자동 배정 | 워크스페이스 생성 직후 바로 사용 가능 |
| @Version 낙관적 잠금 | 동시 수정 충돌 방지 |
| Kafka 이벤트 발행 | WORKSPACE_CREATED/DELETED 이벤트 |
| usecase에 Spring 어노테이션 없음 | Config에서 Bean 등록 |

## 에이전트 연동

### 내부 assistant
- 호출 경로: 직접 REST (POST/PUT/DELETE /workspaces)
- 시나리오: "새 워크스페이스 'AI Lab'을 만들어줘" → assistant 가 `POST /workspaces` 호출

### 외부 AI (Tool Use)
- 노출 엔드포인트: POST /workspaces
- OpenAPI `summary` / `description` 기입 위치: `WorkspaceController` 메서드
- 감사 경로: `caller_type=EXTERNAL_AGENT` → `AuditEntry`

### Agent Command 타겟
- navigate: workspace, settings, groups
- highlight/mutate selector 패턴: `.workspace-item`, `.group-row`, `.member-cell`

## 의존성

- workspace (도메인)
- authentication (JWT 검증)
- R2DBC PostgreSQL, Kafka
- SpringDoc OpenAPI (WebFlux)
- Log4j2

## 실행

```bash
./gradlew :workspace-command:bootRun
./gradlew :workspace-command:test
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
