# events-expert Operational Notes

---

## 탐색 패턴

(미확보)

## 반복 함정

- **Jackson `event_type` null**: 이벤트 구현 클래스에 `@JsonProperty("event_type")` 명시 필요.

## 내부 체크리스트

- [ ] 새 EventType 추가 시 → @JsonSubTypes + 발행자 + 구독자 + DLQ + webhook 필터 + 계약 문서
- [ ] SSE 포맷 변경 시 → shell-ui EventSourceClient + 모든 *EventHandler 구독자
- [ ] Correlation ID 누락 시 → Kafka ProducerRecord 헤더 + event-broadcaster MDC

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

---

마지막 감사: — (신규)
