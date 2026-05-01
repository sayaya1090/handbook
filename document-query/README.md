# Document-Query 모듈

문서 읽기 전용 백엔드 서비스 (CQRS Read). Gateway가 메뉴를 수집하여 Shell에 "documents" 메뉴를 노출한다.

## 계층 구조

```
├── usecase/         DocumentService, DocumentRepository
└── interfaces/
    ├── api/         DocumentController (GET), ExportController (내보내기), MenuController, StatsController
    ├── database/    R2dbcDocumentEntity, R2dbcDocumentRepository, ElasticsearchDocumentRepository
    └── config/      SearchDocumentConfig (Bean 등록, ObjectMapper), ElasticsearchConfig
```

## API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspaces/{workspace}/documents` | 문서 검색 (페이지네이션, 필터) |
| GET | `/workspaces/{workspace}/documents/search?q=` | 전문 검색 (Elasticsearch 기반) |
| GET | `/workspaces/{workspace}/{type}/{serial}` | 특정 문서 조회 |
| GET | `/workspaces/{workspace}/{type}/{serial}/history` | 문서 이력 조회 (전체 버전) |
| GET | `/workspaces/{workspace}/{type}/{serial}/diff?date1=&date2=` | 두 시점 간 문서 diff |
| GET | `/workspaces/{workspace}/documents/export` | 문서 내보내기 (CSV/JSON, 스트리밍 방식) |
| GET | `/workspaces/{workspace}/stats/**` | 대시보드용 통계 API |
| GET | `/menus` | 문서 메뉴 정보 (Gateway 수집용) |

## Document-Command와의 역할 분리

| 역할 | Document-Command | Document-Query |
|------|-----------------|-----------------|
| 읽기 | - | GET (검색, 단건 조회, 통계) |
| 쓰기 | PUT/PATCH/DELETE | - |
| 메뉴 | - | 제공 |

읽기/쓰기 분리(CQRS)로 읽기 전용 서비스를 독립 스케일링하고, 검색 최적화(Elasticsearch)를 적용할 수 있다.

## 에이전트 연동

### 내부 assistant
- 호출 경로: `AGENT_COMMAND` navigate (`target.menu="documents"`)
- 시나리오: "어제 작성한 기획서 찾아줘" → `GET /workspaces/{ws}/documents` 로 검색 후 `navigate`

### 외부 AI (Tool Use)
- 노출 엔드포인트: `GET /workspaces/{workspace}/documents/search`
- OpenAPI `summary` / `description` 기입 위치: `DocumentController.searchDocuments`

### Agent Command 타겟
- navigate: `documents`, `search`
- highlight/mutate selector 패턴: `.search-result-item`, `.document-list-row`

## 검색 파라미터

| 파라미터 | 설명 |
|---------|------|
| page | 페이지 번호 |
| limit | 페이지당 항목 수 |
| sortBy | 정렬 기준 필드 |
| asc | 오름차순 여부 |
| type | 문서 타입 필터 |
| serial | serial 필터 |
| date | 특정 시점 필터 (effectDateTime ≤ date < expireDateTime) |
| last | true이면 최신 버전만 |

## 인프라 기능

| 기능 | 구현 | 설명 |
|------|------|------|
| 검색 쿼리 제한 | `DocumentController.MAX_QUERY_LENGTH` | 전문 검색 쿼리 최대 1000자, 초과 시 400 반환 |
| DB 인덱스 | `V2__add_indexes.sql` | documents/types 복합 인덱스 4건으로 검색 성능 최적화 |
| Export 스트리밍 | `ExportController` | ServerWebExchange 직접 write로 chunked 전송 (JSON/CSV) |
| Prometheus | `application.yml` | `/actuator/prometheus` 메트릭 노출 |
| 구조화 로깅 | `application.yml` | 로그 패턴에 correlationId 포함 |

## 의존성

- document (Document 도메인)
- activity (Menu, Tool)
- authentication (JWT 검증)
- R2DBC PostgreSQL, Elasticsearch 9.3.3
- SpringDoc OpenAPI (WebFlux)
- Log4j2

## 실행

```bash
./gradlew :document-query:bootRun
./gradlew :document-query:test
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
