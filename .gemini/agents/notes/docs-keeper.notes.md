# docs-keeper 에이전트 노트

## 요청 로그
- 2026-05-24: 최근 UI/UX 리팩토링(shell-ui, type-ui) 문서 크로스체크 및 최신화 감식 → 발견 사항 도메인 에이전트 이관
- 2026-05-24: 대규모 프로젝트 무결성 감사 → 문서/계약/UC-테스트 매핑 전수 검증 완료
- 2026-05-24: shell-ui UC-12 매트릭스 갱신 → WorkspaceRedirectTest 매핑 및 문서/계약 무결성 검증
- 2026-05-24: 최근 변경내역 크로스체크 → 에이전트 연동 섹션 누락(document 등 10개 모듈) 및 계약 일치성 검증 완료
- 2026-05-24: 프로젝트 전수 문서 무결성 감사 → DESIGN.md 누락, UC 코드 충돌, Soft Delete 미구현 등 발견.
- 2026-05-18: 커밋 7257216 문서 크로스체크 → Shared Domain/Versioning 계약 불일치 발견 및 승격 제안.
- 2026-05-14: 최근 discrepancies.md 및 요구사항/아키텍처/테스트 크로스체크 감사 수행 → 불일치 5건 확인 및 테스트 누락 탐지
- 2026-05-14: 문서 보완 및 구현 상태 갱신 → 미구현(Soft Delete 등) 표시 및 AI/ES/SessionState 상세 보완 완료
- 2026-05-14: 요구사항-설계-테스트 전수 크로스체크 → 3.12~3.24, 6.6~6.13, 7.1~7.6 완료 (ES 9.3.3, 랜딩, SessionState 확인)
- 2026-05-14: 요구사항-설계-테스트 전수 크로스체크 → 3.1~3.11, 6.1~6.5 완료
- 2026-05-14: 에이전트 연동 섹션 보강 → 10개 모듈 README/USECASE 갱신 완료
- 2026-05-14: 요구사항-설계-UC 전수 감사 → architecture.md 보완 필요 발견 / UC 매트릭스 점검 완료
- 2026-05-14: onboarding-ui 문서 갱신 → README, USECASE, architecture 수정 반영
- 2026-04-28: 대규모 도메인 통합 감사 → engineering-standards.md 정립 및 GWT 렌더링 계약 위반 전수 수정.
- 2026-04-20: 신규 모듈 'landing-content' 배치 감사 → landing-expert 스코프 확장 제안.

## 반복 함정
- **GWT 뷰포트 밀림**: UI 모듈에서 `body().add()`를 직접 호출하는 패턴이 반복됨 → `WindowRenderBridge` 사용 강제 원칙 수립.
- **rev 전파 누락**: `fromDomain()`에서 `rev`를 빠뜨려 `DuplicateKeyException` 발생하는 사례 빈번 → 체크리스트 필수 항목.
- **Kargo Promotion 동기화**: 서비스 종류(backend/frontend)에 따라 Warehouse/Stage 설정 방식이 상이하므로 `system-overview.md` 명세 준수 필수.

## 탐색 패턴
- **계약 조회**: `docs/contracts/README.md`를 먼저 읽어 OWNER 에이전트를 식별한 뒤 병렬 호출.
- **테스트 확인**: `USECASE.md` 매트릭스의 ✅ 항목을 `glob`으로 실제 파일 경로와 대조.
- **순차 메뉴 로딩 검증**: `App -> UserApi -> UserProvider -> MenuList -> MenuApi -> Resolver` 체인이 끊김 없는지 `shell-ui/USECASE.md` 시퀀스 다이어그램으로 대조.

## 과거 실수
- 2026-04-17: `workspace-query` 모듈 추가 시 `cluster-ops` 호출을 누락하여 Helm 차트 동기화 실패 → 작업 착수 전 체크포인트 규칙 강화.
- 2026-04-17: 메뉴 호버 UX가 명시적 Close 버튼 동작을 무시하는 회귀 발생 → UC-S6 재정의(EXPAND 상태 한정)로 해결.

## 원칙 갱신 제안
- **JsPropertyMap JVM Proxy**: GWT 네이티브 인터페이스 호환을 위해 JVM에서 Reflection Proxy 사용하는 패턴을 `engineering-standards.md`에 추가 필요.
- **Versioning -1 Convention**: GWT primitive 타입 한계로 인해 `rev = -1`을 미초기화 상태로 정의하는 규약 명문화 필요.
- **Sequential Loading 명문화**: UI 진입점 결정 시 클라이언트 가상 메뉴 주입을 금지하고 백엔드 공급 기반으로만 동작해야 함. (GEMINI.md 원칙 승격 완료)
