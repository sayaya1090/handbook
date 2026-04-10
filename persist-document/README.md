# Persist-Document 모듈

문서 CRUD 백엔드 서비스. 문서의 생성, 수정, 삭제를 처리하고 Kafka 이벤트를 발행한다.

## API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| PUT | `/workspace/{id}/documents` | 문서 저장 (upsert) |
| PATCH | `/workspace/{id}/documents` | 문서 부분 업데이트 (JSONB 머지) |
| DELETE | `/workspace/{id}/documents` | 문서 삭제 |
| POST | `/workspace/{id}/documents/import` | 문서 일괄 임포트 (JSON). DocumentService.save()를 재사용하여 이벤트도 발행된다 |
| GET | `/workspace/{id}/documents/export` | 문서 일괄 익스포트 (JSON). 선택적 `type` 쿼리 파라미터로 타입별 필터링 가능 |
| POST | `/workspace/{id}/files` | 파일 업로드 (multipart/form-data). 허용 확장자 검증 후 저장소에 저장, URL 반환 |

모든 엔드포인트는 `application/vnd.sayaya.handbook.v1+json` Content-Type을 사용한다. Import/Export 엔드포인트는 `application/json`을 사용한다.

## 구조

```
├── usecase/         DocumentService, DocumentRepository, DocumentEventPublisher, FileStorageService
└── interfaces/
    ├── api/         DocumentController, ImportExportController (임포트/익스포트), FileUploadController (파일 업로드)
    ├── database/    R2dbcDocumentEntity, R2dbcDocumentRepository, R2dbcDocumentRepositoryAdapter
    ├── storage/     LocalFileStorageAdapter (로컬 파일시스템 저장소)
    ├── event/       KafkaDocumentEventPublisher (DOCUMENT_CREATED/DELETED 이벤트 발행)
    └── config/      DocumentConfig (Spring Bean 등록, ObjectMapper), FileConfig (파일 업로드 설정)
```

## 설계 결정

| 결정 | 이유 |
|------|------|
| data 필드를 JSON 문자열로 저장 | 타입별 속성이 동적이므로 스키마리스 저장 |
| @Version 낙관적 잠금 | 동시 편집 충돌 방지 |
| TransactionalOperator 주입 | usecase에 Spring 어노테이션 없이 트랜잭션 지원 |
| Kafka 이벤트 발행 | DOCUMENT_CREATED/DELETED → event-broadcaster → 실시간 UI 갱신 |
| DuplicateKeyException 핸들링 | serial 중복 시 409 Conflict 반환 |

## 인프라 기능

| 기능 | 구현 | 설명 |
|------|------|------|
| 파일 크기 제한 | `FileUploadController.maxFileSize` | 기본 50MB (`file.max-size` 프로퍼티), 초과 시 413 반환 |
| Prometheus | `application.yml` | `/actuator/prometheus` 메트릭 노출 |
| 구조화 로깅 | `application.yml` | 로그 패턴에 correlationId 포함 |

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
