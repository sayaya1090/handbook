# Assistant 모듈

AI 에이전트 오케스트레이터. 자연어 명령을 파싱하여 실행 계획을 생성하고, Kafka 이벤트를 통해 UI 커맨드를 워크스페이스 멤버에게 브로드캐스트한다. 또한 백그라운드 데이터 품질 감시를 수행하여 결측치, 중복, 이상값을 탐지하고 워크스페이스에 실시간 알림한다.

에이전트는 "세 번째 협업자"로서 워크스페이스 이벤트 채널을 공유한다. AGENT_COMMAND 이벤트는 DOCUMENT_CREATED, TYPE_CREATED 등 다른 도메인 이벤트와 동일한 Kafka 토픽("handbook-events")으로 발행되어, event-broadcaster를 통해 워크스페이스 SSE(`/workspaces/{id}/messages`)로 전달된다. 같은 워크스페이스의 모든 참여자(사용자 + 에이전트)가 동일한 SSE 스트림을 구독하므로, 에이전트의 작업 과정이 모든 멤버에게 실시간으로 공유된다.

---

## 핵심 기능

- **자연어 인터페이스**: 사용자의 자연어 요청을 분석하여 Intent와 실행 단계를 추출한다 (OpenAI GPT-4o 연동).
- **실행 계획 (ExecutionPlan)**: `CommandType`에 정의된 10종의 커맨드(navigate, highlight, preview, mutate 등)를 조합하여 실행 계획을 생성한다.
- **병렬 실행**: 같은 그룹(`group` 필드) 내의 단계는 병렬로 실행하여 속도를 최적화한다.
- **사용자 확인 (AWAIT_CONFIRM)**: 데이터 변경 등 파괴적인 작업 전 사용자 승인을 대기하는 메커니즘을 제공한다.
- **데이터 품질 감시**: 워크스페이스 내 문서를 정기적으로 스캔하거나, 생성 이벤트 발생 시 비동기 검증을 수행하여 이슈를 리포팅한다.
- **감사 추적 (Audit Trail)**: 에이전트의 모든 판단 근거(의도, 신뢰도)와 실행 이력을 보존한다.
- **아티팩트 보존**: 실행 완료 시 결과를 Artifact(executionId, summary, changes, timestamp)로 수집하여 AuditEntry에 저장한다.

---

## 작동 모드

- **협업 모드** (UC-A1~A4): 사용자 요청 기반. 에이전트 커맨드가 프론트엔드에서 시각적으로 실행되어 화면 조작 UX 제공. 복수 동시 실행 지원 (executionId 기반).
- **감시 모드** (UC-A5, A7, A8): 스케줄러나 Kafka 이벤트 트리거로 백그라운드에서 동작. 이슈 발견 시 `NOTIFY` 커맨드 발행.
- **조회 모드** (UC-A9~A10): 실행 상태/진행률 조회, 완료된 실행의 아티팩트 조회.

---

## API 엔드포인트

| Method | Path | Content-Type | 설명 |
|--------|------|--------------|------|
| POST | `/assistant/request` | `application/vnd.sayaya.handbook.v1+json` | 자연어 메시지 전달 → 실행 계획 반환 |
| POST | `/assistant/execute` | `application/json` | 실행 계획 승인 및 실행 시작 |
| POST | `/assistant/respond` | `application/vnd.sayaya.handbook.v1+json` | 사용자 응답 전달 (await_confirm 후). executionId로 특정 실행을 대상으로 한다. Sinks.One에 응답을 emit하여 대기 중인 커맨드 스트림을 재개한다. "cancel" 응답 시 실행을 중단한다. |
| POST | `/assistant/abort` | - | 특정 실행 취소 (executionId 지정) |
| GET | `/assistant/executions` | - | 워크스페이스 내 진행 중인 실행 상태/진행률 조회 |
| GET | `/assistant/artifacts` | - | 워크스페이스 내 완료된 실행의 아티팩트 조회 |
| GET | `/assistant/audit` | - | 워크스페이스 감사 로그 조회 |

---

## 구조

```
├── domain/          CommandType, AgentCommand, ExecutionStep, ExecutionPlan, QualityIssue, Artifact, ArtifactChange, ExecutionContext, ExecutionRequest
├── usecase/         AssistantService, IntentParser, QualityMonitorService, SchemaDesigner,
│                    GroupedPlanExecutor, ArtifactAggregator, ExecutionContextManager,
│                    SubAgentOrchestrator (서브 에이전트 오케스트레이션)
└── interfaces/
    ├── api/         AssistantController, AuditController, QualityController
    ├── database/    InMemoryAuditRepository
    ├── llm/         OpenAiIntentParser, DefaultSubAgentPlanExecutor, DefaultArtifactAggregator
    ├── event/       KafkaAgentCommandEventPublisher (Kafka 발행 어댑터),
    │                ValidationEventListener (VALIDATION_REQUESTED Kafka 소비)
    ├── schedule/    ScheduledQualityMonitor (cron 기반 자동 품질 스캔),
    │                WorkspaceProvider, WebClientWorkspaceProvider
    └── config/      AssistantConfig, SchedulingConfig, LlmConfig
```

## 실행 계획 처리 흐름

1. `AssistantController`가 요청을 받아 `AssistantService`로 전달한다.
2. `IntentParser`가 LLM을 호출하여 사용자의 의도를 `ExecutionPlan`으로 변환한다.
3. `GroupedPlanExecutor`가 각 단계를 그룹별로 실행한다. 같은 그룹의 단계는 병렬(Flux.merge)로, 그룹 간에는 순차로 `AgentCommand`를 생성한다.
4. `AgentCommandEventPublisher`가 생성된 커맨드를 Kafka 토픽(`handbook-events`)으로 발행한다.
    └──────────────── event-broadcaster → SSE /workspaces/{id}/messages

---

## 설계 결정

| 결정 | 이유 |
|------|------|
| ConcurrentHashMap으로 다중 실행 관리 | 복수의 동시 실행을 executionId로 식별하여 독립적으로 관리. 동시성 안전한 abort 지원 |
| SubAgentOrchestrator 추출 | AssistantService SRP 위반 해소. 서브 에이전트 오케스트레이션 로직을 독립 클래스로 분리 (7.6 코드 품질) |
| IntentParser 인터페이스화 | LLM 프로바이더(OpenAI, Anthropic 등) 교체 용이성 |
| Sinks.Many를 통한 이벤트 스트리밍 | 실행 과정을 비동기적으로 프론트엔드에 전달 |
| 런타임 Artifact 수집 | 에이전트의 작업 결과를 구조화하여 사후 리포팅에 활용 |

## 인프라 기능

| 기능 | 구현 | 설명 |
|------|------|------|
| Prometheus | `application.yml` | `/actuator/prometheus` 메트릭 노출 |
| 구조화 로깅 | `application.yml` | 로그 패턴에 correlationId 포함 |

## 의존성

- activity (Menu, Tool, Progress, StateProvider)
- agent-protocol (AgentCommand 도메인)
- event (AGENT_COMMAND EventType)
- authentication (JWT 검증)
- Spring WebFlux, Kafka, Jackson

## 실행

```bash
./gradlew :assistant:bootRun
./gradlew :assistant:test
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
