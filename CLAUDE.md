# Handbook 프로젝트 가이드

## 프로젝트 개요

운영 중 스키마 변경과 이력 관리를 지원하는 문서 관리 시스템. GWT 프론트엔드 + Spring Boot 백엔드 + Kafka 이벤트 스트리밍.

## 개발 규칙

### 문서 우선 (DOCS FIRST)
- **문서 작성 완료 후 개발 개시. 동시 진행 금지.**
- 기능 추가/변경 시: 요구사항(docs/requirements.md) → 유스케이스(docs/usecases.md + 모듈/USECASE.md) → 설계(모듈/DESIGN.md, CLASS-DIAGRAM.md) → 구현 → 테스트

### 유스케이스 작성 규칙
- **모든 유스케이스에는 시퀀스 다이어그램(mermaid)이 있어야 한다.** 흐름을 시각적으로 이해할 수 있어야 한다.
- **모든 유스케이스에는 대응되는 테스트가 있어야 한다.** 트레이서빌리티 매트릭스에 테스트 매핑을 기록한다.
- 테스트가 없는 UC는 매트릭스에 "❌ 미구현"으로 표시하고, 구현 시 테스트를 함께 작성한다.

### 문서 크로스체크 (필수)
- **문서를 수정한 후에는 반드시 관련된 모든 문서를 크로스체크해야 한다.**
- 하나의 변경이 여러 문서에 영향을 줄 수 있다. 다음 항목을 확인:
  - 클래스/패키지 경로가 변경되었으면: docs/architecture.md, 모듈/CLASS-DIAGRAM.md, 모듈/README.md
  - 요구사항이 추가/변경되었으면: docs/requirements.md, docs/usecases.md, 모듈/USECASE.md, 모듈/DESIGN.md
  - 디자인 토큰/시각 상태가 변경되었으면: docs/design.md, docs/design-patterns.md, 모듈/DESIGN.md
  - API 엔드포인트가 변경되었으면: docs/requirements.md (4. API 엔드포인트), 모듈/README.md
  - 유스케이스가 추가되었으면: docs/usecases.md (글로벌 UC), 모듈/USECASE.md (트레이서빌리티 매트릭스)
- 크로스체크 없이 커밋하지 않는다.

### 커밋
- Co-Authored-By 태그 사용 금지
- 커밋 메시지 한국어, conventional commits (feat/fix/docs/refactor/chore/test)
- GWT 캐시 파일(*.cache.js, *.nocache.js, *.devmode.js, compilation-mappings.txt, clear.cache.gif) 커밋 금지

### I18N (다국어)
- **UI에 표시되는 모든 텍스트는 LabelProvider를 통해 다국어 처리해야 한다.** 한국어 하드코딩 금지.
- 언어 파일: `js/language.ko.json`, `js/language.en.json`
- 토스트 메시지, 버튼 레이블, 다이얼로그 텍스트, 플레이스홀더 모두 LabelProvider 사용.

### 디자인 언어 통일
- **모든 UI 모듈은 docs/design.md에 정의된 MD3 디자인 토큰을 사용해야 한다.** 하드코딩된 색상/크기/트랜지션 금지.
- 색상은 `var(--md-sys-color-*)`, 크기는 `var(--md-sys-typescale-*)`, 형태는 `var(--md-sys-shape-*)`, 모션은 `var(--md-sys-motion-*)` 사용.
- 상태 색상(created/changed/deleted/valid/invalid/conflict)은 docs/design-patterns.md에 정의된 매핑을 따른다.
- 버튼, 카드, 컨테이너, 툴바 등 공통 UI 패턴은 모듈 간 시각적으로 일관되어야 한다.

### 스킬스 자동 업데이트
- **요청을 처리하면서 새로운 패턴, 컨벤션, 규칙을 발견하면 CLAUDE.md를 선제적으로 업데이트한다.** 사용자가 별도로 요청하지 않아도 필요하다고 판단되면 즉시 반영한다.
- 예: 반복되는 실수 패턴 발견, 새 공통 규칙 도출, 기존 규칙의 예외 사항 발견 등.

### 클래스 Javadoc (필수)
- **모든 클래스에는 Javadoc(Java) 또는 KDoc(Kotlin)을 작성해야 한다.** 다음 항목을 포함:
  - **역할(Role)**: 이 클래스가 무엇을 하는지 한 줄 요약
  - **책임(Responsibility)**: 구체적으로 어떤 동작을 담당하는지
  - **의존관계(Dependencies)**: 주입받는 주요 의존성과 그 역할
  - **주의점(Note)**: 스레드 안전성, GWT 제약, 사용 시 유의사항 등
- 예시:
  ```java
  /**
   * 워크스페이스 SSE 이벤트를 구독하여 문서 목록을 자동 갱신한다.
   *
   * <p><b>책임:</b> DOCUMENT_CREATED/DELETED 이벤트 수신 시 문서 재조회 + 토스트 표시.
   * PRESENCE 이벤트 수신 시 PresenceTracker에 위치 정보 전달.</p>
   *
   * <p><b>의존관계:</b>
   * <ul>
   *   <li>{@link WorkspaceEventReceiver} — SSE 이벤트 스트림 구독</li>
   *   <li>{@link PresenceTracker} — 프레즌스 상태 관리</li>
   * </ul></p>
   *
   * <p><b>주의:</b> 이벤트 문자열은 "EVENT_TYPE:json_payload" 형식. 콜론 기준 분리.</p>
   */
  ```
- 신규 클래스 생성 시 반드시 작성. 기존 클래스 수정 시 Javadoc이 없으면 추가.

### 클래스 크기 경계
- **클래스가 비대해지는 것을 항상 경계한다.** 코드 스멜이 감지되면 즉시 리팩토링을 제안한다.
- 경고 기준: 메서드 10개 이상, 의존성 주입 7개 이상, 파일 200줄 이상
- 단일 책임 원칙(SRP) 위반 징후: 여러 이벤트 타입을 한 클래스에서 처리, 서로 다른 도메인 로직 혼재
- 리팩토링 방향: 핸들러 분리, 전략 패턴 도입, 헬퍼 클래스 추출

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
| kafka-events.md | 이벤트 카탈로그 (토픽, 발행/구독, SSE 흐름) |
| database-schema.md | DB 스키마 (ER 다이어그램, 테이블 상세, 설계 결정) |
| development.md | 빌드/테스트 가이드 |

### 모듈 레벨 (각 모듈/)
| 파일 | 역할 |
|------|------|
| README.md | 모듈 요약 (목적, 컴포넌트, API, 실행) |
| DESIGN.md | 모듈 전용 설계 — 설계 결정이 복잡한 모듈에만 작성 (document-ui 등) |
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
