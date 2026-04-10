# Search-Document 모듈

문서 읽기 전용 백엔드 서비스. Gateway가 메뉴를 수집하여 Shell에 "documents" 메뉴를 노출한다.

## 계층 구조

```
├── usecase/         DocumentService, DocumentRepository
└── interfaces/
    ├── api/         DocumentController (GET), MenuController
    ├── database/    R2dbcDocumentEntity, R2dbcDocumentRepository
    └── config/      SearchDocumentConfig (Bean 등록, ObjectMapper)
```

## API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspace/{id}/documents` | 문서 검색 (페이지네이션, 필터) |
| GET | `/workspace/{id}/{type}/{serial}` | 특정 문서 조회 |
| GET | `/menus` | 문서 메뉴 정보 (Gateway 수집용) |

## persist-document와의 역할 분리

| 역할 | persist-document | search-document |
|------|-----------------|-----------------|
| 읽기 | - | GET (검색, 단건 조회) |
| 쓰기 | PUT/DELETE | - |
| 메뉴 | - | 제공 |

읽기/쓰기 분리(CQRS)로 읽기 전용 서비스를 독립 스케일링할 수 있다.

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

## 의존성

- document (Document 도메인)
- activity (Menu, Tool)
- authentication (JWT 검증)
- R2DBC PostgreSQL
- SpringDoc OpenAPI (WebFlux)
- Log4j2

## 실행

```bash
./gradlew :search-document:bootRun
./gradlew :search-document:test
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
