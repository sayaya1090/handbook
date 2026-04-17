# Handbook

운영 중 스키마 변경과 이력 관리를 지원하는 문서 관리 시스템.
워크스페이스 단위로 타입(스키마)을 정의하고, 해당 타입을 따르는 문서를 협업 편집하며,
변경 이력과 AI 에이전트 지원을 제공한다.

---

## 주요 도메인

| 도메인 | 요약 | 구현 수준 |
|--------|------|----------|
| 워크스페이스 | 조직·그룹·역할 분리, 워크스페이스별 격리 | 골격 구현 (CUD + 진입 흐름) |
| 타입 (스키마) | 캔버스 기반 타입 정의, 필드·관계·레이아웃, 이력 | 부분 구현 (CQRS 분리, UI) |
| 문서 | 스프레드시트 편집, 타입 정합성 검증, 이력 | 부분 구현 (CUD + 조회) |
| 인증·권한 | OAuth2 (Google) + JWT RS256, RBAC | 기본 구현 |
| 실시간 이벤트 | Kafka → SSE 브로드캐스트 (`/workspace/{id}/messages`) | 구현 |
| AI 어시스턴트 | 자연어 변경 제안, 커맨드 프로토콜 | 부분 구현 |

상세 요구사항은 [docs/requirements.md](docs/requirements.md),
도메인별 범위는 [docs/requirements/README.md](docs/requirements/README.md) 참조.

---

## 기술 스택

- **Frontend**: GWT 2.13, Kotlin 2.3, Elemento 2.4.9, sayaya-ui, Dagger, Handsontable
- **Backend**: Spring Boot 4.0.1, Spring Cloud Gateway (WebFlux), Spring Cloud Stream
- **Data**: PostgreSQL 17 (R2DBC), MinIO / S3
- **Messaging**: Kafka (Strimzi / Streams for Apache Kafka, KRaft)
- **Auth**: OAuth2 (Google), JWT RS256 (JJWT 0.13), BouncyCastle
- **Test**: Kotest 6.1.3 + MockK, Testcontainers (PostgreSQL), Playwright 1.52, JUnit 5
- **Deploy**: Helm + Kargo + ArgoCD, Istio ambient mesh, Kubernetes Gateway API

기술 스택 전문은 [.claude/skills/architecture.md](.claude/skills/architecture.md) 참조.

---

## 시스템 구성

런타임 서비스 카탈로그와 토폴로지 다이어그램은 [docs/system-overview.md](docs/system-overview.md) 참조.

### 백엔드 서비스 (Spring Boot)

| 서비스 | 포트 | 역할 |
|--------|------|------|
| gateway | 8080 | API Gateway, 라우팅, `/menus` 집계, CircuitBreaker |
| login | 8081 | OAuth2 + JWT 발급 |
| search-type | 8082 | 타입 조회 (CQRS Read) |
| persist-type | 8083 | 타입 CUD + 이벤트 발행 |
| search-document | 8084 | 문서 조회 (CQRS Read) |
| persist-document | 8085 | 문서 CUD + 이벤트 발행 |
| persist-workspace | 8086 | 워크스페이스 CUD + 이벤트 발행 |
| assistant | 8087 | AI 에이전트 (OpenAI) |
| event-broadcaster | 8088 | Kafka → SSE 실시간 브로드캐스트 |

### 프론트엔드 모듈 (GWT SPA)

- `app` — SPA 조합 루트 (엔트리 포인트)
- `shell-ui` — 애플리케이션 프레임 (Drawer, MenuRail)
- `login-ui` / `workspace-ui` / `dashboard-ui` — 로그인 · 워크스페이스 · 대시보드
- `type-ui` — 캔버스 기반 타입 스키마 편집기
- `document-ui` — Handsontable 기반 문서 편집기
- `agent-ui` — AI 에이전트 채팅 UI
- `ui-components` — Action / ActionManager / ChangeTracker / ToastContainer 공용 컴포넌트
- `agent-bridge` — 모듈 간 CustomEvent 브리지

### 도메인 / 공용 라이브러리

- 도메인: `workspace`, `schema`, `document`, `event`
- 공용: `authentication`, `agent-protocol`, `activity`, `test-utils`

모듈 의존성 그래프는 [docs/architecture.md](docs/architecture.md) 참조.

---

## 빌드 & 테스트

```bash
./gradlew test                    # 전체 테스트 (백엔드 + GWT Playwright, E2E 제외)
./gradlew test --parallel         # 모듈별 고유 포트 사용, 병렬 실행
./gradlew :모듈:compileKotlin     # 모듈 컴파일 (Kotlin)
./gradlew :모듈:compileJava       # 모듈 컴파일 (Java/GWT)
./gradlew :모듈:test              # 단일 모듈 테스트
./gradlew :모듈:gwtDev            # GWT DevMode (핫 리로드)
./gradlew :gateway:bootRun        # 로컬 게이트웨이 실행
E2E=true ./gradlew :e2e:test      # E2E (서버 기동 필요)
```

- 통합 테스트(`R2dbc*IntegrationTest`)는 Testcontainers 로 PostgreSQL 자동 기동 — Docker 필요
- GWT 테스트는 모듈별 고유 포트를 할당받아 병렬 실행 가능
- 환경 설정 / 포트 상세는 [.claude/skills/dev-environment.md](.claude/skills/dev-environment.md) 참조

---

## 실행 & 배포

- 개발 호스트: `handbook.apps.sayaya.cloud` (OpenShift Router wildcard TLS)
- 배포 체인: **GitHub Actions → Kargo Warehouse → ArgoCD → Kubernetes (Istio ambient)**
- 각 서비스는 Helm 서브차트(`charts/handbook/charts/<service>`) 로 배포되며,
  공통 템플릿은 라이브러리 차트 `charts/handbook-lib` 가 제공
- Kargo Warehouse 는 jib 가 push 한 commit SHA 태그를 `strictSemvers: false` 로 구독
- 배포 흐름과 Kargo Stage 구조는 [.claude/skills/deployment.md](.claude/skills/deployment.md) 및
  [docs/system-overview.md](docs/system-overview.md) 참조

---

## 문서 내비게이션

| 영역 | 진입점 |
|------|-------|
| 프로젝트 가이드 (규칙·컨벤션·디버깅) | [CLAUDE.md](CLAUDE.md) |
| 요구사항 (기능·비기능) | [docs/requirements.md](docs/requirements.md) · [docs/requirements/README.md](docs/requirements/README.md) |
| 유스케이스 | [docs/usecases.md](docs/usecases.md) · [docs/usecases/README.md](docs/usecases/README.md) |
| 시스템 아키텍처 | [docs/architecture.md](docs/architecture.md) · [docs/system-overview.md](docs/system-overview.md) |
| 디자인 언어 (MD3 토큰) | [docs/design.md](docs/design.md) · [docs/design-patterns.md](docs/design-patterns.md) |
| 공유 계약 카탈로그 | [docs/contracts/README.md](docs/contracts/README.md) |
| 클래스 다이어그램 | [docs/class-diagram.md](docs/class-diagram.md) |
| 데이터베이스 스키마 | [docs/database-schema.md](docs/database-schema.md) |
| Kafka 이벤트 | [docs/kafka-events.md](docs/kafka-events.md) |
| 에러 핸들링 | [docs/error-handling.md](docs/error-handling.md) |
| 개발 환경 / 빌드 | [docs/development.md](docs/development.md) |
| 인그레스 설계 | [docs/ingress-options.md](docs/ingress-options.md) |

---

## 기여 / 규칙 요약

- **문서 우선 (DOCS FIRST)**: 요구사항 → 유스케이스 → 설계 → 구현 → 테스트.
  문서와 구현 동시 진행 금지. 상세 체크리스트는 [.claude/skills/doc-structure.md](.claude/skills/doc-structure.md).
- **유스케이스 ↔ 테스트**: 모든 UC 는 시퀀스 다이어그램과 대응 테스트를 가진다.
- **I18N**: UI 텍스트는 `LabelProvider` 를 통하며 한국어 하드코딩 금지.
  언어 파일은 `src/main/i18n/language.{ko,en}.json` 에서 빌드 시 머지.
- **디자인 토큰**: MD3 토큰만 사용 — 색·크기·타이포 하드코딩 금지
  ([.claude/skills/design-tokens.md](.claude/skills/design-tokens.md)).
- **UI 컴포넌트**: sayaya-ui 사용 필수, 네이티브 HTML 금지
  ([.claude/skills/sayaya-ui.md](.claude/skills/sayaya-ui.md)).
- **Javadoc/KDoc**: 모든 클래스에 역할·책임·의존관계·주의점 명시.
- **커밋**:
  - Conventional Commits (`feat/fix/docs/refactor/chore/test`), 한국어 메시지
  - `Co-Authored-By` 태그 사용 금지
  - GWT 캐시 아티팩트(`*.cache.js`, `*.nocache.js`, `*.devmode.js`, `compilation-mappings.txt`, `clear.cache.gif`) 커밋 금지

전체 규칙과 Kotlin/Java 컨벤션, 디버깅 체크리스트는 [CLAUDE.md](CLAUDE.md) 참조.

---

## 라이선스

MIT License. 자세한 내용은 [LICENSE](LICENSE) 참조.
