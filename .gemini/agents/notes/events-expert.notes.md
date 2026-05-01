## 요청 로그

- 2026-04-20: 문서 편집 이벤트 검증 -> 계약/구현 불일치(StatusChanged, CorrelationId 누락) 식별
- 2026-04-18: Kafka 직렬화 수정 -> workspace-command Kafka 500 에러 해결을 위해 StringSerializer를 ByteArraySerializer로 교체

---

# events-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

- **계약-구현 교차 검증**: `events.md`의 표와 `Event.kt`의 `EventType`, `JsonSubTypes`를 가장 먼저 대조하여 누락된 이벤트를 식별한다.

## 반복 함정

- **Avro-Jackson 이원화**: 현재 `handbook-event.avsc`는 페이로드를 `string`으로 취급하는 반면, 백엔드 코드는 Jackson으로 `Document` 객체를 통째로 직렬화한다. 이로 인해 Avro 스키마만 보고 필드를 판단하면 실제 런타임 데이터와 괴리가 발생한다.
- **CorrelationId 유실**: 신규 발행자 구현 시 `Event` 인터페이스만 구현하고 `correlationId` 전파 로직(MDC 또는 Context 조회)을 누락하는 경우가 반복됨.

## 내부 체크리스트

- [ ] 새 이벤트 타입 추가 시 -> Avro 스키마 정의 및 모듈 배포 확인
- [ ] 발행자 구현 시 `correlationId` 및 `timestamp` 필드가 할당되는가?
- [ ] `events.md`의 EventType 목록이 `Event.kt` 및 Avro Enum과 1:1 일치하는가?

## 과거 실수

- `DOCUMENT_STATUS_CHANGED` 이벤트를 요구사항에 정의했으나, 발행자(`KafkaDocumentEventPublisher`) 및 `DocumentEvent.ALLOWED_TYPES`에서 누락됨.

## 원칙 갱신 제안

- `Event` 인터페이스에 `timestamp`와 `correlationId`를 필수로 포함하고, 추상 클래스나 팩토리 메서드에서 이를 강제하도록 변경해야 함.

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
