# Search-Type 모듈

타입 스키마 읽기 전용 백엔드 서비스. Gateway가 메뉴를 수집하여 Shell에 "types" 메뉴를 노출한다.

## 계층 구조

```
├── usecase/         TypeService, LayoutService, TypeRepository, LayoutRepository
└── interfaces/
    ├── api/         TypeController (GET), LayoutController (GET), MenuController
    ├── database/    (R2DBC 어댑터 - 추후 구현)
    └── config/      SearchTypeConfig (Bean 등록, ObjectMapper)
```

## API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspace/{id}/types` | 타입 조회 (기간 필터 선택) |
| GET | `/workspace/{id}/layouts` | 레이아웃 기간 목록 |
| GET | `/menus` | 타입 메뉴 정보 (Gateway 수집용) |

## persist-type과의 역할 분리

| 역할 | persist-type | search-type |
|------|-------------|-------------|
| 읽기 | - | GET |
| 쓰기 | PUT/DELETE | - |
| 메뉴 | - | 제공 |

읽기/쓰기 분리(CQRS)로 읽기 전용 서비스를 독립 스케일링할 수 있다.

## 의존성

schema (Type, TypeLayout, Attribute), activity (Menu, Tool), authentication, R2DBC PostgreSQL, SpringDoc OpenAPI (WebFlux), Log4j2
