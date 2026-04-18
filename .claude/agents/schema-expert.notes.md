# schema-expert Operational Notes

에이전트 자신이 갱신하는 업무 노트.

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: search-type allowedSessionStates → IN_WORKSPACE 명시 + 테스트

## 탐색 패턴

(미확보)

## 반복 함정

- **R2DBC JSONB**: `io.r2dbc.postgresql.codec.Json` 타입 사용. String 은 "bad SQL grammar".
  (CLAUDE.md 디버깅 표 참조 — 공용 함정)
- **`@Version` rev 누락**: `fromDomain()` 에서 rev 반드시 전달. 누락 시 `DuplicateKeyException`.
  (versioning 계약 참조)

## 내부 체크리스트

- [ ] 속성 타입 추가 시 → schema 도메인 + type-ui ValidatorEditorFactory + 지원 타입 목록 업데이트
- [ ] 타입 새 버전 생성 시 → `VALIDATION_REQUESTED` 이벤트 발행되는지 확인 → document-expert 재검증 영향
- [ ] 타입 필드 read_roles/write_roles 추가 시 → permissions 계약 + document-ui 마스킹

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
