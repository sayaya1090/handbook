# Persist-Document 모듈

문서 CRUD 백엔드 서비스. 문서의 생성, 수정, 삭제를 처리하고 Kafka 이벤트를 발행한다.

## API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| PUT | `/workspace/{id}/documents` | 문서 저장 (upsert) |
| DELETE | `/workspace/{id}/documents` | 문서 삭제 |

모든 엔드포인트는 `application/vnd.sayaya.handbook.v1+json` Content-Type을 사용한다.

## 구조

```
├── usecase/         DocumentService, DocumentRepository, DocumentEventPublisher
└── interfaces/
    ├── api/         DocumentController
    ├── database/    R2dbcDocumentEntity, R2dbcDocumentRepository, R2dbcDocumentRepositoryAdapter
    ├── event/       KafkaDocumentEventPublisher (DOCUMENT_CREATED/DELETED 이벤트 발행)
    └── config/      DocumentConfig (Spring Bean 등록, ObjectMapper)
```

## 설계 결정

| 결정 | 이유 |
|------|------|
| data 필드를 JSON 문자열로 저장 | 타입별 속성이 동적이므로 스키마리스 저장 |
| @Version 낙관적 잠금 | 동시 편집 충돌 방지 |
| TransactionalOperator 주입 | usecase에 Spring 어노테이션 없이 트랜잭션 지원 |
| Kafka 이벤트 발행 | DOCUMENT_CREATED/DELETED → event-broadcaster → 실시간 UI 갱신 |
| DuplicateKeyException 핸들링 | serial 중복 시 409 Conflict 반환 |

## 의존성

- document (Document 도메인)
- event (DocumentEvent)
- authentication (JWT 검증)
- R2DBC PostgreSQL, Kafka
- SpringDoc OpenAPI (WebFlux)
- Log4j2

## 실행

```bash
./gradlew :persist-document:bootRun
./gradlew :persist-document:test
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
