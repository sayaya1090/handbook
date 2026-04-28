# Persist-Type 모듈

타입 CRUD + 레이아웃 관리 백엔드 서비스.

## API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspace/{id}/types?effect_date_time=&expire_date_time=` | 기간별 타입 목록 |
| PUT | `/workspace/{id}/types` | 타입 저장 (upsert) |
| PATCH | `/workspace/{id}/types` | 타입 부분 업데이트 (속성 upsert) |
| DELETE | `/workspace/{id}/types` | 타입 삭제 |
| GET | `/workspace/{id}/layouts` | 레이아웃 기간 목록 |
| PUT | `/workspace/{id}/layouts` | 캔버스 위치 저장 |

## 구조

```
├── usecase/         TypeService, LayoutService, TypeRepository, LayoutRepository, TypeEventPublisher
└── interfaces/
    ├── api/         TypeController, LayoutController
    ├── database/    R2dbcTypeEntity, R2dbcAttributeEntity, R2dbcLayoutEntity + Repository
    ├── event/       KafkaTypeEventPublisher (TYPE_CREATED/DELETED 이벤트 발행)
    └── config/      TypeConfig (Spring Bean 등록, ObjectMapper)
```

## 설계 결정

| 결정 | 이유 |
|------|------|
| Attribute 별도 테이블 (type_attributes) | 속성 기반 검색 지원 + JSONB로 AttributeType 저장 |
| @Version 낙관적 잠금 | 동시 편집 충돌 방지 |
| TransactionalOperator 주입 | usecase에 Spring 어노테이션 없이 트랜잭션 지원 |
| Kafka 이벤트 발행 | TYPE_CREATED/DELETED → event-broadcaster → 실시간 UI 갱신 |

## 의존성

- schema (Type, Attribute, AttributeType, TypeLayout)
- event (TypeEvent)
- authentication (JWT 검증)
- R2DBC PostgreSQL, Kafka
- SpringDoc OpenAPI (WebFlux)
- Log4j2

## 실행

```bash
./gradlew :type-command:bootRun
./gradlew :type-command:test
```
