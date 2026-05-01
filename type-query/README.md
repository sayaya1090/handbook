# Type-Query 모듈

타입 스키마 읽기 전용 백엔드 서비스 (CQRS Read). Gateway가 메뉴를 수집하여 Shell에 "types" 메뉴를 노출한다.

## 계층 구조

```
├── usecase/         TypeService, LayoutService, TypeRepository, LayoutRepository
└── interfaces/
    ├── api/         TypeController (GET), LayoutController (GET), MenuController
    ├── database/    R2dbcTypeEntity, R2dbcAttributeEntity, R2dbcLayoutEntity, R2dbcTypeSearchRepository
    └── config/      SearchTypeConfig (Bean 등록, ObjectMapper)
```

## API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspaces/{workspace}/types` | 타입 조회 (기간 필터 선택) |
| GET | `/workspaces/{workspace}/types/{type}/versions` | 특정 타입의 모든 버전 조회 |
| GET | `/workspaces/{workspace}/types/{type}/diff?v1=&v2=` | 두 버전 간 diff |
| GET | `/workspaces/{workspace}/layouts` | 레이아웃 기간 목록 |
| GET | `/menus` | 타입 메뉴 정보 (Gateway 수집용) |

## Type-Command와의 역할 분리

| 역할 | Type-Command | Type-Query |
|------|-------------|-------------|
| 읽기 | - | GET |
| 쓰기 | PUT/PATCH/DELETE | - |
| 메뉴 | - | 제공 |

읽기/쓰기 분리(CQRS)로 읽기 전용 서비스를 독립 스케일링할 수 있다.

## 에이전트 연동

### 내부 assistant
- 호출 경로: `AGENT_COMMAND` navigate (`target.menu="types"`)
- 시나리오: "'User' 타입 정의 좀 보여줘" → `GET /types` 조회 후 `navigate`

### 외부 AI (Tool Use)
- 노출 엔드포인트: `GET /workspaces/{workspace}/types`
- OpenAPI `summary` / `description` 기입 위치: `TypeController.getTypes`

### Agent Command 타겟
- navigate: `types`
- highlight/mutate selector 패턴: `.type-list-item`

## 의존성

- schema (Type, TypeLayout, Attribute)
- activity (Menu, Tool)
- authentication (JWT 검증)
- R2DBC PostgreSQL
- SpringDoc OpenAPI (WebFlux)
- Log4j2

## 실행

```bash
./gradlew :type-query:bootRun
./gradlew :type-query:test
```
