---
name: debugging
description: 디버깅 패턴 및 문제 해결 가이드
---

# 디버깅 가이드

## 흔한 에러 패턴

| 에러 | 원인 | 해결 |
|------|------|------|
| `bad SQL grammar` + JSONB | R2DBC 엔티티의 JSONB 컬럼이 `String` 타입 | `io.r2dbc.postgresql.codec.Json` 타입 사용 |
| `no Identifier. Update not possible` | 엔티티에 `@Id` 누락 | `@Id` 어노테이션 추가 |
| `GWT ReferenceError` | `@JsOverlay` 인스턴스 메서드에서 재귀 호출 | static 헬퍼로 우회 |
| `DuplicateKeyException` on save | `@Version rev`가 null → INSERT 시도 | `fromDomain()`에서 rev 전달 |
| `Address already in use` (GWT 테스트) | openWebServer 포트 충돌 | 모듈별 고유 포트 할당으로 해결됨 (build.gradle.kts portMap) |
| Jackson `event_type` null | `@JsonTypeInfo` + 같은 클래스에 여러 discriminator | 구현 클래스에 `@JsonProperty("event_type")` 추가 |
| `switchIfEmpty` eager evaluation | `switchIfEmpty(createX())` — 조건 무관 즉시 실행 | `Mono.defer { createX() }` 사용 |
| MockK slot 다중 캡처 에러 | 같은 mock에서 여러 verify + slot | 각 Given에서 별도 mock 생성 |
| Dagger `MissingBinding` | 새 의존성 추가 후 `@Provides` 누락 | Module에 provider 메서드 추가 |
| `cannot find symbol` (테스트) | 인터페이스에 메서드 추가 후 Mock 구현 미추가 | Mock 클래스에 새 메서드 구현 추가 |
| GWT 컴파일 실패 (record) | Java record를 GWT 모듈에서 사용 | GWT 2.13.0은 Java record 미지원. 일반 class로 변환 |

## Playwright 테스트 검증 패턴

| 패턴 | 약한 검증 (피해야 함) | 강한 검증 (권장) |
|------|----------------------|-----------------|
| 요소 존재 | `shouldNotBe null` | `getAttribute("속성") shouldNotBe null` 또는 구체적 값 검증 |
| 개수 불변 | 재조회 후 `shouldNotBe null` | `before = count()` -> 액션 -> `after shouldBe before` |
| 상태 유지 | 부모 요소 존재 확인 | `getAttribute("selected")` 등 구체적 속성 검증 |
| Undo 검증 | Undo 후 존재 확인 | `before = count()` -> 생성 -> `+1` -> Undo -> `shouldBe before` |
| 이벤트 폭주 | 에러 없음 확인 | 캔버스 HTML `isNotEmpty()`, SVG/컨트롤러 존재 확인 |
| 호버 효과 | 클래스 추가 확인 | 추가 + `unhover` 후 클래스 제거 검증 (양방향) |

## 엣지 케이스 테스트 패턴

| 패턴 | 예시 | 검증 |
|------|------|------|
| 스팸 클릭 | `repeat(N) { click() }` → 정확한 개수 검증 | `after shouldBe before + N` |
| 빈 입력 | 값을 `""` 설정 → Apply → 크래시 없음 확인 | 다이얼로그 닫힘 또는 에러 없이 유지 |
| Malformed 이벤트 | `CustomEvent`에 잘못된 JSON 디스패치 → UI 안정성 확인 | 캔버스/스프레드시트 HTML `isNotBlank()` |
| 경계 페이지 | 첫 페이지에서 Prev 클릭 → 변화 없음 확인 | 셀 수/컬럼 수 불변 |
| 빈 Undo 스택 | Ctrl+Z (실행할 것 없음) → 변화 없음 확인 | 타입 개수 불변 |
| 0/0 진행률 | `currentGroup:0, totalGroups:0` 이벤트 → 에러 없음 | `body.innerHTML().isNotBlank()` |
| 빈 배열 이벤트 | `CustomEvent('handbook-mutate', {detail: []})` → 유지 | 스프레드시트 존재 |
| 연속 이벤트 폭주 | `for(i=1..5)` 이벤트 연속 디스패치 → UI 정상 | 캔버스/컨트롤러/SVG 유지 |

## GWT Playwright 테스트 안정화 패턴

| 문제 | 원인 | 해결 |
|------|------|------|
| `.ht_clone_top th` 카운트 불일치 | Handsontable은 clone 오버레이마다 thead 생성 | `.handsontable thead th` 또는 `.ht_master thead th` 사용 |
| Delete/Ctrl+Z 키가 먹히지 않음 | 캔버스에 포커스가 없으면 키보드 이벤트 무시 | `page.evaluate("document.querySelector('.type-canvas').focus()")` 후 키 발행 |
| Handsontable 헤더 미렌더링 | DOM 추가 전에 `init()` 호출 | `body().add(container)` 후 `spreadsheet().init()` 호출 |
| 우클릭 컨텍스트 메뉴 미표시 | Playwright `click(RIGHT)`이 canvas 핸들러에 먹힘 | JS `dispatchEvent(new MouseEvent('contextmenu', {bubbles:false}))` 사용 |
| GWT Promise 타이밍 이슈 | AsyncSubject.await() + FetchMock 캐스팅 조합 | 테스트에서는 동기 렌더링으로 우회, API 파싱은 백엔드 테스트에서 검증 |
| Undo 후 Save 비활성화 검증 실패 | Undo 1회로 전체 스택이 비워지지 않음 | 루프로 Undo 버튼 비활성화될 때까지 반복 후 검증 |
| 토스트 fadeout 검증 실패 | fadeout 300ms 후 DOM 제거 — 시점 가변 | `(toast == null \|\| fadeout != null) shouldBe true` 양쪽 허용 |
| `.rail .item` 카운트 불일치 | 여러 rail 요소 존재 시 합산됨 | `.rail:first-child .item` 또는 고유 셀렉터 사용 |

## 성능 트러블슈팅 패턴

### 느린 쿼리 진단

```bash
# PostgreSQL 슬로우 쿼리 로그 확인
docker exec -it handbook-postgres psql -U handbook -c "
  SELECT pid, now() - pg_stat_activity.query_start AS duration, query
  FROM pg_stat_activity
  WHERE state = 'active' AND now() - pg_stat_activity.query_start > interval '1 second'
  ORDER BY duration DESC;
"

# 쿼리 실행 계획 확인
docker exec -it handbook-postgres psql -U handbook -c "
  EXPLAIN ANALYZE SELECT * FROM documents WHERE workspace = '<uuid>' AND type = 'customer';
"
```

| 증상 | 원인 | 해결 |
|------|------|------|
| 문서 검색 느림 | `(workspace, type, serial)` 인덱스 없음 | `CREATE INDEX idx_documents_ws_type_serial ON documents (workspace, type, serial)` |
| 시점 기반 조회 느림 | `(workspace, effect_date_time, expire_date_time)` 인덱스 없음 | 복합 인덱스 추가 |
| 전문 검색 느림 | JSONB data 필드에 GIN 인덱스 없음 | `CREATE INDEX idx_documents_data_gin ON documents USING gin (data)` |

### 커넥션 풀 고갈 진단

```bash
# R2DBC 커넥션 풀 상태 확인 (Actuator)
curl -s http://localhost:8085/actuator/metrics/r2dbc.pool.acquired | jq
curl -s http://localhost:8085/actuator/metrics/r2dbc.pool.pending | jq
curl -s http://localhost:8085/actuator/metrics/r2dbc.pool.max.allocated | jq

# PostgreSQL 연결 수 확인
docker exec -it handbook-postgres psql -U handbook -c "
  SELECT count(*) FROM pg_stat_activity WHERE datname = 'handbook';
"
```

| 증상 | 원인 | 해결 |
|------|------|------|
| `Connection pool exhausted` | 동시 요청이 풀 최대 크기 초과 | `spring.r2dbc.pool.max-size` 증가 또는 쿼리 최적화 |
| 커넥션 누수 | `flatMap` 체인에서 구독 취소 시 커넥션 미반환 | `usingWhen` 또는 `TransactionalOperator` 사용 |
| 유휴 커넥션 타임아웃 | 풀 유효성 검사 미설정 | `spring.r2dbc.pool.validation-query=SELECT 1` 설정 |

### Kafka 지연 진단

```bash
# 컨슈머 랙 확인
docker exec -it handbook-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group event-broadcaster

# DLQ 메시지 확인
docker exec -it handbook-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic handbook-events.DLT \
  --from-beginning --max-messages 10
```

| 증상 | 원인 | 해결 |
|------|------|------|
| SSE 이벤트 지연 | 컨슈머 랙 증가 | 파티션 수 / 컨슈머 인스턴스 증가 |
| DLQ에 이벤트 쌓임 | 역직렬화 실패 또는 런타임 예외 | DLQ 메시지의 `x-exception-message` 헤더 확인 |
| 이벤트 유실 | 오토커밋 + 처리 실패 | `autoCommitOnError: false` + DLQ 활성화 |

## R2DBC + PostgreSQL JSONB 패턴

```kotlin
// 엔티티에서 JSONB 컬럼은 io.r2dbc.postgresql.codec.Json 사용
@Table("documents")
data class Entity(
    val data: Json,  // String이 아닌 Json 타입
)

// 도메인 → 엔티티 변환 시
data = Json.of(objectMapper.writeValueAsString(domain.data))

// 엔티티 → 도메인 변환 시
val dataMap: Map<String, String?> = objectMapper.readValue(entity.data.asString())
```

## Jackson 이벤트 직렬화 패턴

```kotlin
// Event 인터페이스: visible=true로 discriminator를 필드에도 전달
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event_type", visible = true)

// 구현 클래스: @JsonProperty로 snake_case 매핑
data class TypeEvent(
    @JsonProperty("event_type") override val eventType: Event.EventType,
)
```
