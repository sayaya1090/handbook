# document-expert Operational Notes

---

## 탐색 패턴

(미확보)

## 반복 함정

- **R2DBC JSONB**: `io.r2dbc.postgresql.codec.Json` 타입 (공용, CLAUDE.md)
- **`switchIfEmpty` eager**: `Mono.defer { }` 로 감쌀 것

## 내부 체크리스트

- [ ] 문서 검증 시 `Compliance` 결과를 통해 어떤 타입 버전을 만족하는지 추적
- [ ] DRAFT 외 상태에서 수정 시도 거부 로직 검증
- [ ] PATCH 시 ChangeTracker 의 변경 필드만 전송 — 전체 전송 안 됨

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

---

마지막 감사: — (신규)
