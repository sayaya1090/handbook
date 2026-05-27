## 요청 로그
- 2026-05-12: 레이아웃 저장 실패(0행 업데이트) 및 데이터 소실(Proxy 직렬화) 최종 조치 -> Persistable 구현 및 Map 변환 로직 추가
- 2026-05-12: WebFlux 환경에서 JsPropertyMap 역직렬화 누락 해결 -> WebFluxConfigurer 강제 적용
- 2026-05-12: 타입 저장 실패 분석 및 조치 -> Type 도메인 모델 rev 필드 추가 및 R2DBC 엔티티 매핑(rev) 보완
- 2026-05-12: 모바일 바텀시트 모달 동작 제거 및 Speed Dial z-index 조정 -> UI 사용성 개선
- 2026-05-12: type-ui 문서 갱신 → Speed Dial 분리 및 StatusHeaderElement 동적 재배치 코디네이터 역할 반영
- 2026-05-24: UI 레이아웃 변경 반영 → USECASE.md 표 갱신 및 에이전트 연동 섹션 추가
- 2026-05-24: 에이전트 연동 누락 보완 → README.md, USECASE.md 상단 내부 전용 명시
- 2026-05-01: TypeApi 리팩토링 → TypeValue를 Type으로 변경 및 컴파일 오류 해결
- 2026-05-04: 도메인 모델 전환 대응 (Java Fluent API) 및 테스트 복구 -> 완료

---

# schema-expert Operational Notes

에이전트 자신이 갱신하는 업무 노트.

## 탐색 패턴

- **GWT Shared Domain JVM 호환성 (2026-05-04)**: `JsPropertyMap`을 포함한 도메인을 백엔드(JVM)에서 사용할 때 `java.lang.reflect.Proxy`를 사용하여 `UnsatisfiedLinkError`를 방지한다.

## 반복 함정

- **저장 후 리비전 동기화 누락 (2026-05-12)**: CUD API가 성공 시 `Void`를 반환하면 클라이언트는 서버에서 갱신된 리비전(rev)을 알 수 없음. 이후 조작 시 구버전 리비전을 전송하여 409 Conflict 유발. API는 반드시 갱신된 최신 도메인 객체를 응답 바디에 포함해야 함.
- **레이아웃 저장 시 UPDATE 오인 (2026-05-12)**: ID가 채워진 엔티티를 `save()`하면 Spring Data R2DBC가 기존 데이터로 간주하여 `UPDATE`를 시도함. 신규 저장 시에도 ID를 직접 생성한다면 반드시 `Persistable`을 구현하여 `isNew`를 명시해야 함.
- **JsPropertyMap 직렬화 누락 (2026-05-12)**: Jackson은 GWT Proxy 객체의 속성을 읽지 못함. 저장(직렬화) 전 반드시 일반 `Map`으로 변환 필요.
- **WebFlux 커스텀 매퍼 무시 (2026-05-12)**: `@Bean ObjectMapper`만으로는 부족하며, `WebFluxConfigurer`를 통해 코덱에 직접 등록해야 함.
- **타입 저장 시 rev 누락 (2026-05-12)**: `Type` 도메인 객체를 `R2dbcTypeEntity`로 변환할 때 `rev` 필드가 누락되면 Spring Data R2DBC가 신규 레코드로 오인하여 `DuplicateKeyException`을 발생시킴. `fromDomain` 및 UI 액션(`EditTBoxDateAction` 등)에서 `rev` 필드 보존 필수.
- **낙관적 잠금 초기값 (rev = -1) (2026-05-04)**: GWT의 primitive `long` 제약으로 인해 `null` 대신 `-1L`을 미초기화(INSERT) 상태로 사용한다. 백엔드 매핑 시 `-1L` -> `null` 변환 필수.

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

마지막 감사: 2026-05-12
