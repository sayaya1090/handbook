# Event 모듈

도메인 이벤트를 정의한다. 불변 이력 모델에 따라 UPDATE 이벤트는 존재하지 않는다.

## 도메인 구조

```
dev.sayaya.handbook.domain.event/
├── Event              # 도메인 이벤트 인터페이스 (sealed, Jackson 폴리모픽)
├── DocumentEvent      # 문서 이벤트 (CREATED, DELETED)
├── TypeEvent          # 타입 이벤트 (CREATED, DELETED)
├── ValidationEvent    # 검증 요청 이벤트
├── ValidationPayload  # 검증 요청 페이로드
└── AgentCommandEvent  # AI 에이전트 커맨드 이벤트
```

## 이벤트 타입

| 이벤트 | 설명 |
|--------|------|
| `DOCUMENT_CREATED` | 문서 생성 (새 버전 포함) |
| `DOCUMENT_DELETED` | 문서 삭제 |
| `TYPE_CREATED` | 타입 생성 (새 버전 포함) |
| `TYPE_DELETED` | 타입 삭제 |
| `VALIDATION_REQUESTED` | 검증 요청 |
| `AGENT_COMMAND` | AI 에이전트 커맨드 (navigate, highlight, mutate 등) |

> `AGENT_COMMAND`는 Assistant 모듈이 에이전트 커맨드를 Kafka로 발행할 때 사용한다. event-broadcaster가 다른 도메인 이벤트와 동일하게 워크스페이스 SSE로 브로드캐스트하므로, 에이전트는 "세 번째 협업자"로서 워크스페이스 이벤트 채널을 공유한다.
>
> `AGENT_COMMAND` 이벤트의 `description` 필드는 **감사 추적(Audit Trail)**에 사용된다. 각 커맨드가 실행되는 사유를 기록하며, Kafka 불변 이벤트 로그와 함께 에이전트의 판단 과정을 사후에 추적할 수 있는 근거가 된다.

## 의존성

- `document` — DocumentEvent의 payload가 Document
- `schema` — TypeEvent의 payload가 Type

## 테스트

```bash
./gradlew :event:test
./gradlew :event:koverVerify  # 커버리지 80% 이상 필수
```
