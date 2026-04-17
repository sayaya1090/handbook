# 유스케이스 도메인 인덱스

`docs/usecases.md` 의 UC 번호별 도메인 매핑. 각 에이전트가 자신 스코프의 UC 만 참조할 수 있도록.

---

## UC 매핑

| UC 범위 | 주제 | 담당 에이전트 |
|---------|------|-------------|
| UC-01~UC-03 | 인증 (OAuth2 로그인, 토큰 갱신, 로그아웃) | auth-expert |
| UC-04~UC-06 | 워크스페이스 진입 (홈 화면, 전환, 조인) | workspace-expert |
| UC-07~UC-09 | 랜딩 (SEO 방문, 자동 리다이렉트, 앱 내부 랜딩) | landing-expert |
| UC-10~UC-11 | 워크스페이스 관리 (생성, 삭제) | workspace-expert |
| UC-20~UC-24 | 사용자·그룹·역할 | workspace-expert + auth-expert |
| UC-30~UC-34 | 타입 관리 (정의, 변경, 삭제, 조회, 이력) | schema-expert |
| UC-40~UC-41 | 타입 시각화 (캔버스 배치, 레이아웃) | schema-expert |
| UC-50~UC-57 | 문서 관리 (생성, 변경, 삭제, 조회, 검색, 이력, 임포트, 익스포트) | document-expert |
| UC-60~UC-63 | 정합성 검증 (문서 검증, 재검증, 결과 조회, 보정) | document-expert + schema-expert |
| UC-66~UC-67 | 권한·차트 (필드 레벨 권한, 대시보드 차트) | auth-expert + ui-platform-expert |
| UC-70~UC-72 | Shell 네비게이션 (메뉴, 도구, URL 라우팅) | ui-platform-expert |
| UC-80~UC-84 | AI 어시스턴트 (대화형 설계, 자연어 변경, UI 안내) | assistant-expert |
| UC-85 | 외부 AI 에이전트 Tool Use | landing-expert |
| UC-90~UC-95 | 운영 (감사 로그, 대시보드, 품질, 에이전트 활동·실행·아티팩트) | ui-platform-expert (대시보드) + assistant-expert (활동/실행) |

---

## 모듈 USECASE.md 트레이서빌리티

각 모듈의 `USECASE.md` 는 자신이 구현하는 UC 를 트레이서빌리티 매트릭스로 유지한다
(`docs/doc-structure.md` 참조). docs-keeper 가 글로벌 UC ↔ 모듈 UC ↔ 테스트 3중 정합성을 감시한다.

## 후속 작업 (docs-keeper 대상)

- UC 를 도메인별 파일(`docs/usecases/<domain>.md`) 로 이동
- 이동 완료 후 `docs/usecases.md` 는 개요 다이어그램 + 이 인덱스로 대체
