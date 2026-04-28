# Persist-Workspace 모듈

워크스페이스 CRUD 백엔드 서비스. 워크스페이스 생성 시 Admin 그룹을 자동 생성하고 생성자를 배정한다.

## 계층 구조

```
├── usecase/         WorkspaceService, WorkspaceRepository, GroupRepository, WorkspaceEventPublisher
└── interfaces/
    ├── api/         WorkspaceController (POST/PUT/DELETE /workspace), MenuController
    ├── database/    R2dbcWorkspaceEntity (@Version), R2dbcGroupEntity, R2dbcGroupMemberEntity
    ├── event/       KafkaWorkspaceEventPublisher
    └── config/      WorkspaceConfig (Bean 등록, @EnableR2dbcAuditing)
```

## API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/workspace` | 워크스페이스 생성 (자동 Admin 그룹 + 생성자 배정) |
| PUT | `/workspace/{id}` | 워크스페이스 수정 (이름, 설명) |
| DELETE | `/workspace/{id}` | 워크스페이스 삭제 |
| GET | `/menus` | 워크스페이스 메뉴 정보 (Gateway 수집용) |

## 설계 결정

| 결정 | 이유 |
|------|------|
| 생성 시 Admin 그룹 자동 생성 | 생성자에게 즉시 관리 권한 부여 |
| 생성자를 Admin 그룹에 자동 배정 | 워크스페이스 생성 직후 바로 사용 가능 |
| @Version 낙관적 잠금 | 동시 수정 충돌 방지 |
| Kafka 이벤트 발행 | WORKSPACE_CREATED/DELETED 이벤트 |
| usecase에 Spring 어노테이션 없음 | Config에서 Bean 등록 |

## 의존성

workspace (도메인), authentication, Spring WebFlux, R2DBC PostgreSQL, Kafka, SpringDoc OpenAPI (WebFlux), Log4j2
