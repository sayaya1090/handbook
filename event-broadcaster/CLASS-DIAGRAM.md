# Event-Broadcaster 클래스 다이어그램

```mermaid
classDiagram
    class Broadcaster {
        -ObjectMapper objectMapper
        -WorkspaceSinkManager sinkManager
        +broadcast(event: String)
        +listen(workspace: UUID): Flux~Event~
    }

    class WorkspaceSinkManager {
        -ConcurrentHashMap sinks
        +tryEmitNext(event)
        +listen(workspace): Flux~Event~
    }

    class WorkspaceSink {
        -AtomicInteger activeSubscribers
        +asFlux(): Flux~Event~
        +releaseSubscriptionAndGetCount(): Int
    }

    class EventMessageListener {
        -Broadcaster broadcaster
        +accept(event: String)
    }

    class MessageController {
        -Broadcaster broadcaster
        -ObjectMapper objectMapper
        +messages(workspace): Flux~SSE~
    }

    Broadcaster --> WorkspaceSinkManager
    WorkspaceSinkManager --> WorkspaceSink
    EventMessageListener --> Broadcaster
    MessageController --> Broadcaster
```

## Configuration

```mermaid
classDiagram
    class BroadcasterConfig {
        <<@Configuration>>
        +objectMapper(): ObjectMapper
        +workspaceSinkManager(): WorkspaceSinkManager
        +broadcaster(objectMapper, sinkManager): Broadcaster
    }

    BroadcasterConfig ..> WorkspaceSinkManager : creates
    BroadcasterConfig ..> Broadcaster : creates
```

## 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **Pub/Sub** | Broadcaster, WorkspaceSinkManager | Kafka 이벤트를 워크스페이스별 SSE 스트림으로 팬아웃 |
| **Sink per Workspace** | WorkspaceSinkManager, WorkspaceSink | 워크스페이스별 Sinks.Many를 관리하여 격리된 이벤트 전달 |
| **SSE (Server-Sent Events)** | MessageController | 클라이언트에 실시간 이벤트 스트림 제공 |
