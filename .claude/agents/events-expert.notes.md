# events-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: persist-workspace Kafka 500 → StringSerializer→ByteArraySerializer 교체

## 탐색 패턴

(미확보)

## 반복 함정

- **Jackson `event_type` null**: 이벤트 구현 클래스에 `@JsonProperty("event_type")` 명시 필요.
- **StreamBridge + StringSerializer ClassCastException**: Spring Cloud Stream 은 MessageConverter(application/json) 가 byte[] 로 직렬화하므로 binder producer value-serializer 는 `ByteArraySerializer` 여야 한다. `KafkaTemplate<String,String>` 직접 사용 서비스(persist-document/persist-type)는 StringSerializer 유지. 발행 패턴과 serializer 짝 규약은 `docs/contracts/events.md` "Producer Serializer 규약" 표.

## 내부 체크리스트

- [ ] 새 EventType 추가 시 → @JsonSubTypes + 발행자 + 구독자 + DLQ + webhook 필터 + 계약 문서
- [ ] SSE 포맷 변경 시 → shell-ui EventSourceClient + 모든 *EventHandler 구독자
- [ ] Correlation ID 누락 시 → Kafka ProducerRecord 헤더 + event-broadcaster MDC

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
