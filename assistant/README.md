# Assistant 모듈

AI 에이전트 오케스트레이터. 자연어 명령을 파싱하여 실행 계획을 생성하고, Kafka 이벤트를 통해 UI 커맨드를 워크스페이스 멤버에게 브로드캐스트한다. 또한 백그라운드 데이터 품질 감시를 수행하여 결측치, 중복, 이상값을 탐지하고 워크스페이스에 실시간 알림한다.

에이전트는 "세 번째 협업자"로서 워크스페이스 이벤트 채널을 공유한다. AGENT_COMMAND 이벤트는 DOCUMENT_CREATED, TYPE_CREATED 등 다른 도메인 이벤트와 동일한 Kafka 토픽("handbook-events")으로 발행되어, event-broadcaster를 통해 워크스페이스 SSE(`/workspaces/{id}/messages`)로 전달된다. 같은 워크스페이스의 모든 참여자(사용자 + 에이전트)가 동일한 SSE 스트림을 구독하므로, 에이전트의 작업 과정이 모든 멤버에게 실시간으로 공유된다.

### UX 원칙

기술적으로는 Kafka 이벤트이지만, 프론트엔드에서 각 커맨드를 시각적 애니메이션으로 실행하여 **"동료가 내 화면을 대신 조작해주는 느낌"**을 제공한다:
- `navigate` → 화면 전환 애니메이션
- `mutate` → 셀이 하나씩 채워지는 효과
- `highlight` → 시선 유도 (펄스 + 자동 스크롤)

### 감사 추적 (Audit Trail)

에이전트의 모든 행동은 추적 가능하도록 설계된다:
- **의도 근거 보존**: 사용자 원본 메시지 + LLM 해석 결과(ExecutionPlan)를 함께 기록
- **커맨드별 사유**: 각 AgentCommand의 `description` 필드에 실행 이유를 기록
- **Kafka 불변 이벤트 로그**: 발행된 AGENT_COMMAND 이벤트가 감사 추적의 근거
- **실행 계획 보존**: ExecutionPlan 전체(intent, steps, confidence)를 보존하여 판단 과정 재현 가능
- **아티팩트 보존**: 실행 완료 시 결과를 Artifact(executionId, summary, changes, timestamp)로 수집하여 AuditEntry에 저장
- **시간순 감사 조회**: Dashboard-UI에서 에이전트 활동 이력을 시간순으로 조회

### 협업 모드 vs 감시 모드

- **협업 모드** (UC-A1~A4): 사용자 요청 기반. 에이전트 커맨드가 프론트엔드에서 시각적으로 실행되어 화면 조작 UX 제공. 복수 동시 실행 지원 (executionId 기반).
- **감시 모드** (UC-A5): 백그라운드 서버 자동 실행. `notify` 커맨드로 알림만 전달하며, 화면 조작 없음.
- **조회 모드** (UC-A9~A10): 실행 상태/진행률 조회, 완료된 실행의 아티팩트 조회.

## API 엔드포인트

| Method | Path | Content-Type | 설명 |
|--------|------|-------------|------|
| POST | `/assistant/request` | `application/vnd.sayaya.handbook.v1+json` | 자연어 메시지 → 실행 계획 파싱 |
| POST | `/assistant/execute` | `application/vnd.sayaya.handbook.v1+json` | 실행 계획 실행 (커맨드는 Kafka로 발행) |
| POST | `/assistant/respond` | `application/vnd.sayaya.handbook.v1+json` | 사용자 응답 전달 (await_confirm 후). executionId로 특정 실행을 대상으로 한다. Sinks.One에 응답을 emit하여 대기 중인 커맨드 스트림을 재개한다. "cancel" 응답 시 실행을 중단한다. |
| POST | `/assistant/abort` | - | 특정 실행 취소 (executionId 지정) |
| GET | `/assistant/executions` | - | 워크스페이스 내 진행 중인 실행 상태/진행률 조회 |
| GET | `/assistant/artifacts` | - | 워크스페이스 내 완료된 실행의 아티팩트 조회 |

## 구조

```
├── domain/          CommandType, AgentCommand, ExecutionStep, ExecutionPlan, QualityIssue, Artifact, ArtifactChange, ExecutionContext, ExecutionRequest
├── usecase/         IntentParser, PlanExecutor, SchemaDesigner, AssistantService,
│                    SubAgentOrchestrator (서브 에이전트 오케스트레이션 — AssistantService에서 분리),
│                    AgentCommandEventPublisher (이벤트 발행 포트),
│                    QualityMonitor (데이터 품질 감시)
└── interfaces/
    ├── api/         AssistantController (REST)
    ├── event/       KafkaAgentCommandEventPublisher (Kafka 발행 어댑터),
    │                ValidationEventListener (VALIDATION_REQUESTED Kafka 소비 → QualityMonitorService 위임)
    ├── schedule/    ScheduledQualityMonitor (cron 기반 자동 품질 스캔),
    │                WorkspaceProvider (활성 워크스페이스 목록 포트),
    │                WebClientWorkspaceProvider (Gateway 경유 활성 워크스페이스 조회)
    └── config/      AssistantConfig (Spring Bean 등록, ObjectMapper),
                     SchedulingConfig (스케줄 관련 Bean 등록)
```

## 아키텍처

```
자연어 메시지
    │
    ▼
IntentParser (LLM) ──→ ExecutionPlan
    │                      │
    │                      ▼
    │               PlanExecutor ──→ AgentCommand
    │                                      │
    │                                      ▼
    │                          AgentCommandEventPublisher
    │                                      │
    │                                      ▼
    │                          Kafka ("handbook-events")
    │                                      │
    │                                      ▼
    └──────────────── event-broadcaster → SSE /workspaces/{id}/messages
```

1. 사용자가 자연어 메시지를 전송한다.
2. `IntentParser`가 LLM을 통해 의도를 분석하고 `ExecutionPlan`을 생성한다.
3. `GroupedPlanExecutor`가 각 단계를 그룹별로 실행한다. 같은 그룹의 단계는 병렬(Flux.merge)로, 그룹 간에는 순차로 `AgentCommand`를 생성한다.
4. `AgentCommandEventPublisher`가 각 커맨드를 AGENT_COMMAND 이벤트로 Kafka에 발행한다.
5. event-broadcaster가 워크스페이스 SSE를 통해 모든 멤버에게 커맨드를 전달한다.

### 데이터 품질 감시

에이전트가 백그라운드에서 워크스페이스 내 데이터의 품질을 선제적으로 감시한다.

- **결측치 감지**: 필수 속성이 비어 있거나, 참조 대상이 존재하지 않는 문서 탐지
- **중복 데이터 감지**: 동일 타입 내 핵심 속성 조합이 유사한 문서 탐지
- **이상값 감지**: 수치형 속성의 통계적 이상값, 날짜형의 비논리적 값, 텍스트형 패턴 불일치 탐지
- **실행 방식**: 사용자 자연어 요청, 주기적 스케줄 (`ScheduledQualityMonitor`, cron 설정 `quality.monitor.cron`), 또는 DOCUMENT_CREATED 이벤트 트리거
- **알림**: 심각도(info/warning/error)에 따라 AGENT_COMMAND(notify) 이벤트로 워크스페이스에 브로드캐스트

## 설계 결정

| 결정 | 이유 |
|------|------|
| usecase 포트 인터페이스 분리 | LLM 클라이언트/API 클라이언트 구현을 교체 가능하게 |
| Kafka 이벤트 브로드캐스트 | 워크스페이스 이벤트 채널 공유로 에이전트를 협업자로 통합. 별도 SSE 불필요 |
| ConcurrentHashMap으로 다중 실행 관리 | 복수의 동시 실행을 executionId로 식별하여 독립적으로 관리. 동시성 안전한 abort 지원 |
| @Configuration Bean 등록 | usecase에 Spring 어노테이션 없이 순수 Kotlin 유지 |
| SubAgentOrchestrator 추출 | AssistantService SRP 위반 해소. 서브 에이전트 오케스트레이션 로직을 독립 클래스로 분리 (7.6 코드 품질) |

## 의존성

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
