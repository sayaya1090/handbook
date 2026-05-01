# Event-Broadcaster 유스케이스

## SSE 연결 및 이벤트 브로드캐스트 시퀀스

```mermaid
sequenceDiagram
    actor Client as "클라이언트 (shell-ui)"
    participant GW as Gateway
    participant Ctrl as MessageController
    participant BC as Broadcaster
    participant SM as WorkspaceSinkManager
    participant WS as WorkspaceSink
    participant Kafka as Kafka

    Client->>GW: "GET /workspaces/{id}/messages"
    GW->>Ctrl: "SSE 연결 요청"
    Ctrl->>BC: "listen(workspace)"
    BC->>SM: "listen(workspace)"
    SM->>SM: "ConcurrentHashMap.compute(workspace)"
    alt "첫 구독자"
        SM->>WS: "new WorkspaceSink(Sinks.many().replay().limit(10ms))"
    end
    SM->>WS: "incrementSubscribers()"
    WS-->>SM: "Flux<Event>"
    SM-->>BC: "Flux<Event>"
    BC-->>Ctrl: "Flux<Event>"
    Ctrl->>Ctrl: "Event → ServerSentEvent 변환"
    Note over Ctrl: "id=event.id, event=eventType, data=JSON(payload)"
    Ctrl-->>Client: "text/event-stream"

    loop "Kafka 이벤트 수신"
        Kafka->>BC: "EventMessageListener.accept(JSON)"
        BC->>BC: "JSON → Event 역직렬화"
        BC->>SM: "tryEmitNext(event)"
        SM->>WS: "tryEmitNext(event) [workspace 매칭]"
        WS-->>Ctrl: "Event 전달"
        Ctrl-->>Client: "SSE 이벤트 전송"
    end

    loop "Keep-alive (10초 간격)"
        Ctrl-->>Client: "SSE comment: 'ping'"
    end
```

## 연결 정리 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant Ctrl as MessageController
    participant SM as WorkspaceSinkManager
    participant WS as WorkspaceSink

    Client->>Ctrl: "연결 종료 (cancel)"
    Note over Ctrl: "doOnCancel 트리거"
    Ctrl->>SM: "doFinally → compute(workspace)"
    SM->>WS: "decrementSubscribers()"
    alt "구독자 수 > 0"
        SM->>SM: "WorkspaceSink 유지"
    else "구독자 수 ≤ 0 (마지막 구독자)"
        SM->>WS: "tryEmitComplete()"
        SM->>SM: "맵에서 WorkspaceSink 제거"
    end
```

---

## UC-EB1: SSE 연결 수립

| 항목 | 내용 |
|------|------|
| **액터** | 클라이언트 (shell-ui 경유) |
| **선행조건** | 워크스페이스에 참여 중 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspaces/{id}/messages`로 SSE 연결을 요청한다.<br>2. `MessageController`가 `Broadcaster.listen(workspace)`를 호출한다.<br>3. `WorkspaceSinkManager`가 `ConcurrentHashMap.compute()`로 원자적으로 처리한다.<br>4. 해당 워크스페이스에 기존 Sink가 없으면 새로 생성한다 (lazy 생성).<br>5. 구독자 카운트를 증가시키고 `Flux<Event>`를 반환한다.<br>6. `MessageController`가 이벤트를 `ServerSentEvent`로 변환하여 `text/event-stream`으로 응답한다. |
| **결과** | SSE 연결 수립, 워크스페이스별 이벤트 수신 시작 |

## UC-EB2: Kafka → SSE 변환

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (Kafka Consumer) |
| **선행조건** | SSE 구독자가 존재하는 워크스페이스에 이벤트 발생 |
| **정상 흐름** | 1. `EventMessageListener`가 Kafka에서 JSON 문자열 이벤트를 수신한다 (Spring Cloud Stream Consumer 바인딩).<br>2. `Broadcaster.broadcast()`가 JSON을 `Event<Serializable>`로 역직렬화한다.<br>3. 내부 Sink(`replay().limit(10ms)`)에 이벤트를 발행한다.<br>4. `WorkspaceSinkManager.tryEmitNext()`가 이벤트의 workspace UUID로 해당 Sink를 찾아 전달한다.<br>5. 구독자가 없는 워크스페이스의 이벤트는 무시된다.<br>6. 각 SSE 클라이언트에 `ServerSentEvent`(id, event, data)로 전달된다. |
| **결과** | Kafka 이벤트가 해당 워크스페이스의 모든 SSE 클라이언트에 브로드캐스트됨 |

## UC-EB3: Keep-alive

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (MessageController) |
| **선행조건** | SSE 연결이 수립된 상태 |
| **정상 흐름** | 1. `MessageController`가 `Flux.interval(Duration.ofSeconds(10))`으로 10초 간격 타이머를 생성한다.<br>2. 각 interval마다 `ServerSentEvent.builder().comment("ping").build()`를 생성한다.<br>3. `Flux.merge()`로 이벤트 스트림과 ping 스트림을 병합하여 클라이언트에 전송한다.<br>4. HTTP/1.1 연결이 타임아웃되지 않도록 유지한다. |
| **결과** | SSE 연결이 10초 간격 ping으로 유지됨 |

## UC-EB4: 연결 정리

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (클라이언트 연결 종료 시) |
| **선행조건** | SSE 연결이 수립된 상태 |
| **정상 흐름** | 1. 클라이언트가 연결을 종료하면 `doFinally` 콜백이 트리거된다.<br>2. `WorkspaceSinkManager.compute(workspace)`로 원자적으로 처리한다.<br>3. `WorkspaceSink.decrementSubscribers()`로 구독자 카운트를 감소시킨다.<br>4. 구독자 수가 0 이하이면 (마지막 구독자 해제) `tryEmitComplete()`로 Sink를 완료하고 맵에서 제거한다.<br>5. 구독자가 남아 있으면 Sink를 유지한다. |
| **결과** | 비활성 연결 정리, 마지막 구독자 해제 시 워크스페이스 Sink 자동 제거 |

## UC-EB5: 다중 구독자 브로드캐스트

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (Kafka Consumer) |
| **선행조건** | 동일 워크스페이스에 2명 이상의 SSE 구독자가 연결된 상태 |
| **정상 흐름** | 1. Kafka에서 이벤트가 수신되면 `Broadcaster.broadcast()`가 호출된다.<br>2. `WorkspaceSinkManager`가 해당 워크스페이스의 `WorkspaceSink`를 찾는다.<br>3. `WorkspaceSink`의 `Sinks.many().replay()` 기반 Sink에 이벤트를 발행한다.<br>4. 동일 워크스페이스를 구독 중인 모든 SSE 연결에 동시에 이벤트가 전달된다.<br>5. 각 구독자는 독립적으로 `ServerSentEvent`를 수신한다. |
| **결과** | 동일 워크스페이스의 모든 SSE 클라이언트가 동일한 이벤트를 수신함 |

---

## UC-EB6: SSE 재연결 지원

| 항목 | 내용 |
|------|------|
| **액터** | 클라이언트 (shell-ui) |
| **선행조건** | SSE 연결이 수립된 상태에서 네트워크 장애 또는 서비스 재시작 발생 |
| **정상 흐름** | 1. `MessageController`가 각 SSE 이벤트에 `retry(Duration.ofSeconds(5))` 힌트를 포함하여 전송한다.<br>2. SSE 연결이 끊어지면 브라우저의 EventSource가 5초 후 자동 재연결을 시도한다.<br>3. 재연결 성공 시 새로운 SSE 스트림이 수립된다. |
| **요구사항** | 7.3 회복성 강화 — SSE 재연결 |
| **상태** | ✅ 구현 완료 (서버 측 retry 힌트) |
| **구현 클래스** | `MessageController` (ServerSentEvent.builder().retry()) |
| **비고** | 클라이언트 측 exponential backoff 및 Toast 알림은 향후 프론트엔드에서 보강 예정 |

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant GW as Gateway
    participant EB as event-broadcaster

    Client->>GW: "SSE 연결 (정상)"
    GW->>EB: "SSE 프록시"
    EB-->>Client: "text/event-stream (이벤트 수신 중)"
    Note over Client,EB: "⚡ 네트워크 장애 / 서비스 재시작"
    Client->>Client: "onerror 감지"
    Client->>Client: "Toast WARNING 표시"

    loop "Exponential Backoff"
        Client->>GW: "재연결 시도 (1초 후)"
        GW-->>Client: "503 Service Unavailable"
        Client->>GW: "재연결 시도 (2초 후)"
        GW-->>Client: "503 Service Unavailable"
        Client->>GW: "재연결 시도 (4초 후)"
        GW->>EB: "SSE 프록시"
        EB-->>Client: "text/event-stream (재연결 성공)"
    end

    Client->>Client: "Toast INFO '연결 복구'"
```

## UC-EB7: Kafka DLQ 처리

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (Kafka Consumer) |
| **선행조건** | Kafka 이벤트 처리 중 예외 발생 |
| **정상 흐름** | 1. `EventMessageListener`가 이벤트 처리 중 예외를 발생시킨다 (역직렬화 실패, 런타임 에러 등).<br>2. Spring Cloud Stream이 최대 3회 재시도를 수행한다 (`consumer.max-attempts: 3`).<br>3. 3회 모두 실패하면 이벤트를 `handbook-events-dlq` 토픽에 저장한다 (`enableDlq: true`, `dlqName: handbook-events-dlq`).<br>4. DLQ 이벤트에 원본 토픽, 에러 정보를 포함한다. |
| **요구사항** | 7.3 회복성 강화 — Kafka DLQ |
| **상태** | ✅ 구현 완료 |
| **구현** | `application.yml` (Spring Cloud Stream Kafka Binder DLQ 설정) |

```mermaid
sequenceDiagram
    participant Kafka as "Kafka (handbook-events)"
    participant Consumer as EventMessageListener
    participant DLQ as "Kafka (handbook-events.DLT)"
    participant Prometheus as Prometheus

    Kafka->>Consumer: "이벤트 수신"
    Consumer->>Consumer: "JSON 역직렬화 시도"
    Consumer-->>Consumer: "예외 발생"
    Consumer->>Consumer: "재시도 1 (1초 후)"
    Consumer-->>Consumer: "예외 발생"
    Consumer->>Consumer: "재시도 2 (2초 후)"
    Consumer-->>Consumer: "예외 발생"
    Consumer->>Consumer: "재시도 3 (4초 후)"
    Consumer-->>Consumer: "예외 발생"
    Consumer->>DLQ: "DLQ에 이벤트 저장 (원본 + 에러 헤더)"
    Consumer->>Prometheus: "dlq_events_total 카운터 증가"
```

## 에이전트 연동 시나리오

Assistant가 발행한 커맨드를 실시간으로 전달하는 흐름을 담당한다.

```mermaid
sequenceDiagram
    participant Asst as Assistant
    participant K as Kafka
    participant EB as event-broadcaster
    participant UI as agent-ui

    Asst->>K: AGENT_COMMAND (mutate)
    K->>EB: 이벤트 수신
    EB-->>UI: SSE 브로드캐스트 (AGENT_COMMAND)
```

---

## 트레이서빌리티 매트릭스

| UC | 요구사항 | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---------|---|---|---|
| UC-EB1 (SSE 연결) | 3.9 (SSE 실시간 알림) | SSE 연결 및 이벤트 브로드캐스트 | MessageController, Broadcaster, WorkspaceSinkManager, WorkspaceSink | - |
| UC-EB2 (Kafka→SSE) | 3.9 (Kafka 이벤트 스트리밍) | SSE 연결 및 이벤트 브로드캐스트 | EventMessageListener, Broadcaster, WorkspaceSinkManager, WorkspaceSink | - |
| UC-EB3 (Keep-alive) | 3.9 (Keep-alive ping) | SSE 연결 및 이벤트 브로드캐스트 (loop) | MessageController | - |
| UC-EB4 (연결 정리) | 3.9 (Sink lazy 생성/자동 정리) | 연결 정리 | WorkspaceSinkManager, WorkspaceSink | - |
| UC-EB5 (다중 구독자) | 3.1 (실시간 협업), 3.9 (SSE) | SSE 연결 및 이벤트 브로드캐스트 | Broadcaster, WorkspaceSinkManager, WorkspaceSink | BroadcasterTest: 다중 구독자 동시 수신 검증, WorkspaceSinkManagerTest: 여러 구독자 동시 구독 검증 |
| UC-EB6 (SSE 재연결) | 7.3 (SSE 재연결) | SSE 재연결 | MessageController (retry 힌트) | MessageControllerTest |
| UC-EB7 (Kafka DLQ) | 7.3 (Kafka DLQ) | Kafka DLQ 처리 | application.yml (enableDlq, dlqName) | - |
