# Handbook 프로젝트 가이드

## 프로젝트 개요

운영 중 스키마 변경과 이력 관리를 지원하는 문서 관리 시스템. GWT 프론트엔드 + Spring Boot 백엔드 + Kafka 이벤트 스트리밍.

## 개발 규칙

### 문서 우선 (DOCS FIRST)
- **문서 작성 완료 후 개발 개시. 동시 진행 금지.**
- 기능 추가/변경 시: 요구사항(docs/requirements.md) → 유스케이스(docs/usecases.md + 모듈/USECASE.md) → 설계(모듈/DESIGN.md, CLASS-DIAGRAM.md) → 구현 → 테스트

### 커밋
- Co-Authored-By 태그 사용 금지
- 커밋 메시지 한국어, conventional commits (feat/fix/docs/refactor/chore/test)
- GWT 캐시 파일(*.cache.js, *.nocache.js, *.devmode.js, compilation-mappings.txt, clear.cache.gif) 커밋 금지

### GWT
- @JsOverlay 인스턴스 메서드에서 재귀 호출 금지 (GWT ReferenceError 발생). static 헬퍼로 우회.

## 아키텍처

### 프론트엔드 (GWT 2.13.0)
- **ui-components**: 공유 컴포넌트 (Action, ActionManager, ChangeTracker, ToastContainer, ConfirmDialog 등)
- **shell-ui**: SPA 프레임 (Drawer, MenuRail, ToolRail, 동적 모듈 로딩)
- **document-ui**: Handsontable 6.2.4 MIT 스프레드시트 편집기
- **type-ui**: 캔버스 기반 타입 스키마 편집기
- **agent-ui**: AI 에이전트 채팅 인터페이스
- **dashboard-ui**: 워크스페이스 현황 대시보드
- **workspace-ui**: 워크스페이스 생성/조인
- **login-ui**: 터미널 스타일 로그인

### 백엔드 (Spring Boot + Kotlin)
- **gateway**: API 게이트웨이, 메뉴 집계
- **login**: OAuth2 인증, JWT 발급
- **persist-document / persist-type / persist-workspace**: CUD + Kafka 이벤트 발행
- **search-document / search-type**: 읽기 전용 CQRS
- **event-broadcaster**: Kafka → SSE 실시간 브로드캐스트
- **assistant**: AI 에이전트 백엔드

### 공통 패턴
- **Action + ActionManager**: Command 패턴 Undo/Redo (ui-components에 정의)
- **ChangeTracker**: 키 기반 더티 트래킹 (ui-components에 정의)
- **BehaviorSubject**: 반응형 상태 관리
- **WindowMutationBridge**: GWT 모듈 간 CustomEvent 통신 (agent-bridge)
- **Port & Adapter**: usecase 포트를 API 어댑터가 구현 (헥사고날)

## 문서 구조

### 프로젝트 레벨 (docs/)
| 파일 | 역할 |
|------|------|
| requirements.md | 기능/비기능 요구사항 |
| architecture.md | 시스템 아키텍처, 모듈 구조 |
| design.md | UI/UX 디자인 시스템 (MD3 토큰) |
| usecases.md | 글로벌 유스케이스 (UC-01~UC-93) |
| design-patterns.md | 공통 설계 패턴 (Action, 더티 트래킹, 프레즌스) |
| error-handling.md | 오류 처리 전략 |
| development.md | 빌드/테스트 가이드 |

### 모듈 레벨 (각 모듈/)
| 파일 | 역할 |
|------|------|
| README.md | 모듈 요약 (목적, 컴포넌트, API, 실행) |
| DESIGN.md | 모듈 전용 설계 (비주얼, 상태 머신, 협업) |
| USECASE.md | 모듈 유스케이스 + 시퀀스 다이어그램 |
| CLASS-DIAGRAM.md | 클래스 구조 (mermaid) |

## 빌드 & 테스트

```bash
./gradlew :모듈:compileJava       # 컴파일
./gradlew :모듈:gwtDev             # GWT DevMode
./gradlew :모듈:test               # 테스트 (Playwright + KoTest)
./gradlew :gateway:bootRun         # 백엔드 실행
```

## 디자인 토큰 (MD3)

- 색상: `--md-sys-color-*` (primary, secondary, tertiary, error, surface 계열)
- 타이포: `--md-sys-typescale-*` (headline, body, label)
- 모션: `--md-sys-motion-duration-medium2` (300ms), `--md-sys-motion-easing-standard`
- 형태: `--md-sys-shape-corner-small` (8px), `corner-medium` (12px), `corner-extra-large` (28px)
- 상태 색상: created=tertiary, changed=tertiary, deleted=투명화, valid=primary, invalid=error, conflict=secondary
