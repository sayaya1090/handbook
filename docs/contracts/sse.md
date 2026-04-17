# SSE 스트림 계약

워크스페이스별 실시간 이벤트 브로드캐스트의 프로토콜·포맷·재연결 규약.

## 공급자 (Providers)

- **event-broadcaster** — Kafka 이벤트를 워크스페이스별 SSE 스트림으로 변환
  - `interfaces/api/MessagesController.kt` 또는 유사
  - `GET /workspace/{workspace}/messages` 엔드포인트

## 소비자 (Consumers)

- **shell-ui** — EventSource 구독 + `WindowWorkspaceEventBridge` 로 전파
  - `client/interfaces/EventSourceClient.java`
- **document-ui / type-ui / agent-ui / dashboard-ui** — `CustomEvent('handbook-workspace-event')` 수신
  - 각 모듈의 `*EventHandler.java`

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 엔드포인트 경로 변경 | shell-ui EventSource URL + gateway 라우팅 |
| 이벤트 포맷 변경 | 모든 프론트엔드 구독자의 파싱 로직 + CustomEvent detail 포맷 |
| Keep-alive 주기 변경 | 클라이언트 재연결 타이머 조정 |

---

## 엔드포인트

| Method | Path | Content-Type |
|--------|------|--------------|
| GET | `/workspace/{workspace}/messages` | `text/event-stream` |

인증 필요 — 워크스페이스 접근 권한 있는 사용자만.

## 이벤트 포맷

```
event: EVENT_TYPE
id: {correlationId}
data: {payload_json}

```

- `event` — `EventType` 값 ([events.md](events.md) 참조)
- `id` — correlation ID (추적용)
- `data` — JSON 페이로드

## Keep-alive

- 10초마다 ping (빈 comment 또는 heartbeat 이벤트)
- HTTP/1.1 연결 유지 목적

## Replay Buffer

- 10ms 정도의 작은 버퍼
- 새 구독자가 연결 직후 최근 이벤트를 수신할 수 있도록

## 프론트엔드 수신 경로

```
SSE → shell-ui (EventSource)
    → WindowWorkspaceEventBridge.publish()
    → CustomEvent('handbook-workspace-event', detail: "EVENT_TYPE:payload_json")
    → 각 모듈의 WorkspaceEventReceiver.events() 구독
    → *EventHandler (DocumentEventHandler / TypeEventHandler / AgentCommandHandler)
```

## 재연결 (회복성 요구사항 §7.3)

- 클라이언트 측 exponential backoff: 1s → 2s → 4s → 최대 30s
- 재연결 시 `Last-Event-ID` 헤더로 이어받기 (선택적 구현)

## Sink 생명주기 (서버 측)

- 워크스페이스별 Sink 는 lazy 생성
- 모든 구독자 해제 시 자동 정리
- 구독자 등록/해제와 Sink 생성/제거는 원자적 (경합 방지)
