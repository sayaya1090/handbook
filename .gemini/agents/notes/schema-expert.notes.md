## 요청 로그
- 2026-05-24: UI 레이아웃 변경 반영 → USECASE.md 표 갱신 및 에이전트 연동 섹션 추가
- 2026-05-24: 에이전트 연동 누락 보완 → README.md, USECASE.md 상단 내부 전용 명시
- 2026-05-01: TypeApi 리팩토링 → TypeValue를 Type으로 변경 및 컴파일 오류 해결

- 2026-05-01: type-ui 내 TypeValue/AttributeValue를 Type/Attribute로 일괄 리팩토링 진행
- 2026-05-01: Type/Attribute 모델 정규화 및 리팩토링 시작 → FQCN 변경 및 필드 접근 수정 준비
- 2026-04-28: type-ui 도메인 모델 교체 → Type/Attribute 등 FQCN 수정 및 컴파일 확인 진행 중

- 2026-05-04: 도메인 모델 전환 대응 (Java Fluent API) 및 테스트 복구 -> 완료

---

# schema-expert Operational Notes

에이전트 자신이 갱신하는 업무 노트.

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

- **GWT Shared Domain JVM 호환성 (2026-05-04)**: `JsPropertyMap`을 포함한 도메인을 백엔드(JVM)에서 사용할 때 `java.lang.reflect.Proxy`를 사용하여 `UnsatisfiedLinkError`를 방지한다.

## 반복 함정

- **낙관적 잠금 초기값 (rev = -1) (2026-05-04)**: GWT의 primitive `long` 제약으로 인해 `null` 대신 `-1L`을 미초기화(INSERT) 상태로 사용한다.

## 내부 체크리스트

- [ ] 스키마 변경 시 -> 불변 이력 보존 및 새 버전 생성 규칙 준수 여부 확인
- [ ] 검증기 추가 시 -> 정규식 및 범위 제한 유효성 테스트

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
