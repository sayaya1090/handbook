# Kafka 이벤트 카탈로그

## 토픽

모든 도메인 이벤트는 단일 토픽 `handbook-events`로 발행된다. 파티션 키는 워크스페이스 UUID.

## 이벤트 흐름

```mermaid
flowchart LR
    PD["persist-document"] -->|"DOCUMENT_CREATED, DELETED, STATUS_CHANGED"| K["Kafka handbook-events"]
    PT["persist-type"] -->|"TYPE_CREATED, DELETED"| K
    AS["assistant"] -->|"AGENT_COMMAND"| K
    Browser -->|"POST /presence"| PC["PresenceController"]
    PC -->|"PRESENCE"| K
    K --> EB["event-broadcaster"]
    K --> WH["webhook-service"]
    WH -->|"HTTP POST"| Ext["외부 시스템"]
    EB -->|"SSE"| Browser
    Browser --> DocHandler["DocumentEventHandler"]
    Browser --> TypeHandler["TypeEventHandler"]
    Browser --> AgentHandler["AgentCommandHandler"]
```

## 이벤트 타입

| EventType | 발행 서비스 | 페이로드 | 구독자 (프론트엔드) |
|-----------|-----------|---------|-------------------|
| `DOCUMENT_CREATED` | persist-document | Document (id, type, serial, data) | DocumentEventHandler → 문서 목록 갱신 + 토스트 |
| `DOCUMENT_DELETED` | persist-document | Document (id, type, serial) | DocumentEventHandler → 문서 목록 갱신 + 토스트 |
| `TYPE_CREATED` | persist-type | Type (id, version, attributes) | TypeEventHandler → 타입 목록 갱신 + ChangeTracker/ActionManager 초기화 |
| `TYPE_DELETED` | persist-type | Type (id, version) | TypeEventHandler → 타입 목록 갱신 |
| `VALIDATION_REQUESTED` | persist-document | ValidationPayload (typeId, typeVersion, documentId) | assistant (ValidationEventListener) → QualityMonitorService.validate() |
| `AGENT_COMMAND` | assistant | AgentCommandPayload (seq, type, target, description, executionId). PROGRESS: {currentGroup, totalGroups, parallel, stepCount}. COMPLETE: artifact summary 포함 | AgentCommandHandler → 에이전트 UI 명령 실행 |
| `DOCUMENT_STATUS_CHANGED` | persist-document | Document (id, status, previousStatus) | DocumentEventHandler → 문서 상태 갱신 + 토스트 |
| `PRESENCE` | event-broadcaster | PresencePayload (user, userName, type, serial, field) | DocumentEventHandler / TypeEventHandler → 편집 위치 표시 |

## 이벤트 구조

```kotlin
// 공통 이벤트 인터페이스 (event 모듈)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    Type(DocumentEvent::class, name = "DOCUMENT_CREATED"),
    Type(DocumentEvent::class, name = "DOCUMENT_DELETED"),
    Type(DocumentEvent::class, name = "DOCUMENT_STATUS_CHANGED"),
    Type(TypeEvent::class, name = "TYPE_CREATED"),
    Type(TypeEvent::class, name = "TYPE_DELETED"),
    Type(ValidationEvent::class, name = "VALIDATION_REQUESTED"),
    Type(AgentCommandEvent::class, name = "AGENT_COMMAND"),
    Type(PresenceEvent::class, name = "PRESENCE")
)
interface Event<T : Serializable> {
    val type: EventType
    val workspace: UUID
    val payload: T
    val timestamp: Instant
}
```

## SSE 전달 형식

event-broadcaster가 Kafka 메시지를 수신하여 워크스페이스별 SSE 스트림으로 전달한다.

- **엔드포인트**: `GET /workspace/{workspace}/messages` (text/event-stream)
- **이벤트 포맷**: `event: EVENT_TYPE\ndata: payload_json\n\n`
- **Keep-alive**: 10초마다 ping
- **Replay buffer**: 10ms (새 구독자가 최근 이벤트를 받을 수 있음)

### 프론트엔드 수신 경로

```
SSE → shell-ui (EventSource) → WindowWorkspaceEventBridge.publish()
    → CustomEvent('handbook-workspace-event', detail: "EVENT_TYPE:payload_json")
    → 각 모듈의 WorkspaceEventReceiver.events() 구독
    → DocumentEventHandler / TypeEventHandler
```

## 발행 코드 위치

| 서비스 | 파일 | 클래스 |
|--------|------|--------|
| persist-document | `interfaces/event/KafkaDocumentEventPublisher.kt` | KafkaDocumentEventPublisher |
| persist-type | `interfaces/event/KafkaTypeEventPublisher.kt` | KafkaTypeEventPublisher |
| assistant | `interfaces/event/KafkaAgentCommandEventPublisher.kt` | KafkaAgentCommandEventPublisher |
| event-broadcaster | `interfaces/api/PresenceController.kt` | PresenceController (POST → Kafka) |

## Dead Letter Queue (DLQ) — 구현 완료 (7.3 회복성 강화)

### DLQ 토픽

| 토픽 | 소스 토픽 | 설명 |
|------|----------|------|
| `handbook-events-dlq` | `handbook-events` | 처리 실패한 이벤트 저장 |

### DLQ 발생 조건

| 조건 | 예시 |
|------|------|
| 역직렬화 실패 | 잘못된 JSON 형식, 알 수 없는 EventType |
| 런타임 예외 | SSE 브로드캐스트 중 예외, DB 연결 실패 (webhook-service) |
| 타임아웃 | 이벤트 처리 시간 초과 |

### 설정 (Spring Cloud Stream)

```yaml
spring:
  cloud:
    stream:
      bindings:
        event-in-0:
          destination: handbook-events
          consumer:
            max-attempts: 3
          group: event-broadcaster
      kafka:
        bindings:
          event-in-0:
            consumer:
              enableDlq: true
              dlqName: handbook-events-dlq
```

## Correlation ID (7.4 관측성)

### 이벤트 헤더에 추적 ID 전파

모든 Kafka 이벤트 발행 시 `X-Correlation-Id` 헤더를 포함한다. Gateway에서 생성된 추적 ID가 HTTP → Kafka → SSE 전 구간에 전파된다.

| 구간 | 전파 방식 |
|------|----------|
| 클라이언트 → Gateway | HTTP 요청 헤더 `X-Correlation-Id` (없으면 Gateway에서 UUID 생성) |
| Gateway → 백엔드 서비스 | HTTP 요청 헤더 전파 |
| 백엔드 서비스 → Kafka | `ProducerRecord` 헤더에 `x-correlation-id` 추가 |
| Kafka → event-broadcaster | `ConsumerRecord` 헤더에서 추출 → MDC에 설정 |
| event-broadcaster → SSE | SSE 이벤트 `id` 필드에 correlation ID 포함 |

### 이벤트 구조 변경 (계획)

```kotlin
interface Event<T : Serializable> {
    val type: EventType
    val workspace: UUID
    val payload: T
    val timestamp: Instant
    val correlationId: String?  // 신규: 요청 추적 ID
}
```

## 구독 코드 위치

| 모듈 | 파일 | 수신 이벤트 |
|------|------|-----------|
| event-broadcaster | `interfaces/event/EventMessageListener.kt` | 전체 (Kafka Consumer) |
| document-ui | `client/usecase/DocumentEventHandler.java` | DOCUMENT_CREATED, DOCUMENT_DELETED |
| type-ui | `client/usecase/TypeEventHandler.java` | TYPE_CREATED, TYPE_DELETED |
| agent-ui | `client/interfaces/AgentSseClient.java` | AGENT_COMMAND |
| assistant | `interfaces/event/ValidationEventListener.kt` | VALIDATION_REQUESTED → QualityMonitorService.validate() |
| webhook-service | (신규 모듈) | 전체 이벤트 → 웹훅 필터 매칭 후 HTTP POST 콜백 |
