## 요청 로그

- 2026-05-24: document-ui 컴파일 에러 해결 (도메인 모델 이름 변경 및 Fluent Accessor 적용) -> 완료
- 2026-05-12: 문서 검색 엔진 전환 -> Elasticsearch 9.3.3 도입으로 검색 성능 강화
- 2026-04-18: document-query MenuController allowedSessionStates -> IN_WORKSPACE 선언 + 테스트

---

# document-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

(미확보)

## 반복 함정

- **R2DBC JSONB**: `io.r2dbc.postgresql.codec.Json` 타입 (공용, GEMINI.md)
- **`switchIfEmpty` eager**: `Mono.defer { }` 로 감쌀 것
- **영속 상태 판별 (2026-05-03)**: `id == null` 인 데이터는 서버에 저장되지 않은 로컬 전용 신규 작업물로 간주한다.
- **비파괴적 병합 (Smart Merge) (2026-05-03)**: 외부 이벤트로 데이터 갱신 시, 서버 수신 목록을 베이스라인으로 삼고 로컬 신규 항목(`id == null`) 중 `serial`이 서버와 중복되지 않는 것만 보존하여 병합한다.

## 내부 체크리스트

- [ ] 문서 검증 시 `Compliance` 결과를 통해 어떤 타입 버전을 만족하는지 추적
- [ ] DRAFT 외 상태에서 수정 시도 거부 로직 검증
- [ ] PATCH 시 ChangeTracker 의 변경 필드만 전송 — 전체 전송 안 됨
- [ ] `Document` 클래스의 `equals/hashCode`가 `serial`을 폴백으로 사용하는지 확인 (2026-05-03)

## 과거 실수

(미확보)

## 원칙 갱신 제안

- **CQRS 검색 엔진 분리**: 복합 필터와 전문 검색이 필요한 도메인(문서)은 RDBMS JSONB 대신 검색 전문 엔진(Elasticsearch 9.3.3)을 Read Model로 사용한다. (2026-05-12)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
