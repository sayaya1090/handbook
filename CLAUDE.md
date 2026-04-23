# Handbook 프로젝트 가이드

운영 중 스키마 변경과 이력 관리를 지원하는 문서 관리 시스템. GWT + Spring Boot + Kafka.
아키텍처/기술 스택 상세는 `.claude/skills/architecture.md` 참조.

## 개발 규칙

### 문서 우선 (DOCS FIRST)
- **문서 작성 완료 후 개발 개시. 동시 진행 금지.**
- 기능 추가/변경 시: 요구사항 → 유스케이스 → 설계 → 구현 → 테스트
- **문서 수정 후 반드시 크로스체크.** 체크 대상은 `.claude/skills/doc-structure.md` 참조.

### 유스케이스 작성 규칙
- **모든 유스케이스에는 시퀀스 다이어그램(mermaid)과 대응 테스트가 있어야 한다.**
- 테스트가 없는 UC는 매트릭스에 "미구현"으로 표시.

### 커밋
- Co-Authored-By 태그 사용 금지
- 커밋 메시지 한국어, conventional commits (feat/fix/docs/refactor/chore/test)
- GWT 캐시 파일(*.cache.js, *.nocache.js, *.devmode.js, compilation-mappings.txt, clear.cache.gif) 커밋 금지
- **커밋 전 로컬 테스트 필수.** 수정한 모듈은 `./gradlew :<module>:test` (프로덕션 코드 변경 시 의존 모듈 포함) 로 그린 확인 후 커밋. CI 에서 회귀 실패를 내보내지 않는다. 테스트 없이 커밋 가능한 예외: 순수 문서(*.md) / helm values / 주석만 수정.

### I18N (다국어)
- **UI 텍스트는 LabelProvider를 통해 다국어 처리.** 한국어 하드코딩 금지.
- 언어 파일: `src/main/i18n/language.{ko,en}.json` → 빌드 시 머지

### 디자인 언어 통일
- **MD3 디자인 토큰만 사용.** 하드코딩 금지. 상세는 `.claude/skills/design-tokens.md` 참조.

### 클래스 Javadoc (필수)
- **모든 클래스에 역할/책임/의존관계/주의점 포함 Javadoc(KDoc) 작성.**
- 신규 클래스 생성 시 필수. 기존 클래스 수정 시 없으면 추가.

### 신규 모듈 운영테스트 (필수)
**처음 구축하는 모듈(신규 Spring Boot 서비스 또는 GWT UI 모듈)은 단위·통합 테스트 통과 후 실제 dev 클러스터(`handbook-dev`)에 배포해 gateway 경유 엔드포인트가 정상 응답하는지 확인한 뒤 커밋**한다.

절차:
1. `./gradlew :<module>:test` 로컬 통과
2. jib 로 이미지 빌드 & push (`./gradlew :<module>:jib -Djib.to.image=...`)
3. helm chart 혹은 ArgoCD sync 로 `handbook-dev` 네임스페이스 배포
4. `oc get pod -n handbook-dev -l app=<module>` Ready 확인
5. gateway 경유 호출 검증 — 예: `curl https://<dev-host>/menus` 응답에 해당 모듈 엔트리 포함
6. 성공 로그(엔드포인트 + 응답) 커밋 메시지 본문에 남김

dev 네임스페이스는 자유롭게 실험 가능. staging/prod 는 Kargo promotion 경로로만 전파.

### 에이전트 연동 체크리스트 (필수)
**모든 신규 모듈 / 기존 모듈의 공개 API 변경 시 아래를 요구사항·문서·구현에서 반드시 확인**한다. 상세 템플릿은 `.claude/skills/doc-structure.md` 의 "에이전트 연동 섹션" 참조.

1. **내부 assistant 연동** — 이 모듈을 assistant 가 어떻게 호출/사용하는가 (직접 REST, 또는 `AGENT_COMMAND` navigate/mutate 타겟)
2. **외부 AI Tool Use** — 어떤 엔드포인트를 `/openapi.json` 에 노출할지 / 차단할지 결정 (`docs/contracts/api.md`)
3. **OpenAPI 어노테이션** — 노출되는 엔드포인트에 springdoc `@Operation(summary, description, example)` 기입 (AI function calling 품질)
4. **감사 경로** — `caller_type=EXTERNAL_AGENT/MCP_CLIENT` 호출 시 `AuditEntry` 발행 지점 확인 (`docs/contracts/audit.md`)
5. **Agent Command 타겟** — navigate/highlight/mutate 커맨드가 이 모듈 화면/필드를 가리킬 수 있도록 selector·URL 패턴 명시

→ 모듈 `README.md` + `USECASE.md` 에 **"에이전트 연동"** 섹션 필수.
→ `docs-keeper` 가 주기 감사에서 섹션 누락을 플래그한다.

### 클래스 크기 경계
- 경고 기준: 메서드 10개 이상, 의존성 7개 이상, 200줄 이상
- SRP 위반 징후 시 핸들러 분리, 전략 패턴, 헬퍼 추출 제안

### 스킬스 자동 업데이트
- 새 패턴/컨벤션/규칙 발견 시 CLAUDE.md를 선제적으로 업데이트한다.

## 코드 컨벤션

### Kotlin (백엔드)
- R2DBC 엔티티의 JSONB 컬럼은 `io.r2dbc.postgresql.codec.Json` 타입 사용
- `@Version` 있는 엔티티는 `fromDomain()`에서 rev를 반드시 전달
- `switchIfEmpty` 인자는 `Mono.defer { }` 감싸기 (eager evaluation 방지)
- 이벤트 구현 클래스에 `@JsonProperty("event_type")` 명시
- **Spring Boot 엔트리포인트는 top-level `fun main` 패턴만 사용.** `class Application { companion object { @JvmStatic fun main } }` 금지 — `Application` + `Application$Companion` 둘 다에 main이 생겨 jib `MainClassInferenceException` 발생.
- **다른 서비스가 project dependency로 참조하는 Spring Boot 모듈에는 `tasks.jar { enabled = false }` 금지.** bootJar만 만들면 library 소비자가 plain jar variant를 못 받아 jib가 `Obtaining project build output files failed` 로 실패. Spring Boot 3.x/4.x 기본 동작(bootJar + `-plain.jar` 공존)에 맡긴다.
- **Jackson 3 패키지**: Spring Boot 4 는 Jackson 3(`tools.jackson.*`) 를 사용한다. 신규 코드는 `tools.jackson.databind.*`, `tools.jackson.core.*`, `tools.jackson.module.kotlin.*` 을 쓰고 annotation(`@JsonProperty` 등) 만 `com.fasterxml.jackson.annotation.*` 에 그대로 둔다.
  - `ObjectMapper` 는 생성 후 불변 — 뮤테이터 체인(`ObjectMapper().setVisibility().registerModule()...`) 금지. `JsonMapper.builder().addModule(...).propertyNamingStrategy(...).build()` 패턴만 사용.
  - 날짜 관련 feature 는 `SerializationFeature` 에서 떨어져나왔다: `DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS`, `DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS` (`tools.jackson.databind.cfg.DateTimeFeature`).
  - `visibility(PA, V)` → `changeDefaultVisibility { it.withVisibility(PA, V) }`, `serializationInclusion/defaultPropertyInclusion(...)` → `changeDefaultPropertyInclusion { it.withValueInclusion(...) }` (UnaryOperator 기반).
  - `jackson-datatype-jsr310` 은 붙이지 않는다. Jackson 3 가 `java.time` 을 기본 내장하므로 `JavaTimeModule` 등록 불필요. 만약 커스텀 포맷이 필요해지면 그 지점에서만 해당 모듈을 재도입.
  - WebFlux codec: Spring Boot 4 부터 `Jackson2JsonEncoder/Decoder` 대신 `JacksonJsonEncoder/Decoder` (`JsonMapper` 를 요구). `CodecConfigurer.defaultCodecs().jackson2Json*` 도 `.jacksonJson*` 로.

### Java (GWT 프론트엔드)
- **JSNI 사용 금지.** Elemental2/JsInterop으로 대체. 사용법은 `.claude/skills/sayaya-ui.md` 참조.
- `@JsOverlay` 인스턴스 메서드에서 재귀 호출 금지 → static 헬퍼로 우회
- **Java record 사용 가능** (GWT 2.12+). 단, 다중 생성자 금지 → static 팩토리 메서드(`of()`)로 대체.
- Dagger `@Module`에 새 의존성 추가 시 `@Provides` 누락 주의
- 테스트 Mock에서 인터페이스 메서드 추가 시 구현도 함께 추가
- **UI 모듈 `Application.onModuleLoad()` 는 `body().add()` 금지 — `WindowRenderBridge.next(render)` 경유 필수.** 전역 CSS `body{position:fixed; inset:0}` + shell `#content{height:100dvh}` 뒤에 스택되어 뷰포트 밖으로 밀려나 보이지 않는 회귀가 반복적으로 발생. shell `FrameUpdater` 가 Frame 엘리먼트를 배치·여백 관리하고 모듈은 `frame.append(container)` 로 Frame 내부에만 mount. 계약은 `docs/contracts/frame.md`. 예외: login-ui 의 `LogoutApplication` 처럼 전면 리다이렉트 페이지만 허용.

### 테스트
- 백엔드: Kotest BehaviorSpec + MockK / Testcontainers PostgreSQL
- 프론트엔드: GWT 컴파일 + Playwright (./gradlew test에 포함)
- E2E: Playwright + HttpClient (E2E 환경변수, 서버 실행 필요)
- **Then 검증은 구체적으로:** `shouldNotBe null`만으로 끝내지 않는다. 속성 값, 개수 변화, 상태 토글, 양방향(추가→제거) 검증 필수.
- 상세 패턴은 `.claude/skills/debugging.md`의 "Playwright 테스트 검증 패턴" 참조
- **UI 컴포넌트는 sayaya-ui 사용 필수.** 네이티브 HTML 금지. 사용법은 `.claude/skills/sayaya-ui.md` 참조
- **모든 UI 요소에 테스트 가능한 고유 CSS 클래스 필수.** 제네릭 클래스(`.type-ctrl-btn`)만 사용 금지 → `.css("type-ctrl-btn", "type-ctrl-btn-add")` 형태로 구체적 클래스 추가. `:first-child`/`:last-child` 대신 `data-*` 속성이나 고유 클래스 사용.

## 빌드 & 테스트

### 로컬 인프라 구성
로컬 개발을 위해 Docker Compose로 필수 인프라를 실행합니다.
```bash
docker-compose up -d     # PostgreSQL, Kafka, Zookeeper, Elasticsearch 실행
docker-compose down      # 인프라 중지
docker-compose down -v   # 데이터 초기화 포함 중지
```

### 빌드 및 실행 명령어
```bash
./gradlew test                    # 전체 테스트 (백엔드 + GWT Playwright, E2E 제외)
./gradlew test --parallel         # 전체 테스트 병렬 실행 (모듈별 고유 포트)
./gradlew :모듈:compileJava       # Java 컴파일
./gradlew :모듈:compileKotlin     # Kotlin 컴파일
./gradlew :모듈:test              # 모듈별 테스트
./gradlew :모듈:gwtDev            # GWT DevMode (핫 리로드)
./gradlew :gateway:bootRun        # 백엔드 실행
E2E=true ./gradlew :e2e:test      # E2E 테스트 (서버 실행 필요)
```

### 테스트 주의사항
- 통합 테스트(`R2dbc*IntegrationTest`)는 Testcontainers로 PostgreSQL 자동 실행 — Docker 필요
- GWT 테스트는 모듈별 고유 포트 할당 — `--parallel` 플래그로 병렬 실행 가능
- E2E 테스트는 게이트웨이 + 모든 백엔드 서비스 실행 필요
- 환경 설정/포트 상세는 `.claude/skills/dev-environment.md` 참조

## 디버깅 가이드

| 에러 | 원인 | 해결 |
|------|------|------|
| `bad SQL grammar` + JSONB | JSONB 컬럼이 `String` 타입 | `io.r2dbc.postgresql.codec.Json` 사용 |
| `no Identifier. Update not possible` | `@Id` 누락 | 엔티티에 `@Id` 추가 |
| `DuplicateKeyException` on save | `@Version rev` null → INSERT | `fromDomain()`에서 rev 전달 |
| `switchIfEmpty` eager evaluation | 인자가 즉시 실행됨 | `Mono.defer { }` 감싸기 |
| Jackson `event_type` null | 역직렬화 시 필드 매핑 실패 | `@JsonProperty("event_type")` 추가 |
| GWT `ReferenceError` | `@JsOverlay` 재귀 호출 | static 헬퍼로 우회 |
| GWT 컴파일 실패 (record) | Java record를 GWT 모듈에서 사용 | GWT 2.13.0은 record 미지원, 일반 class 사용 |
| MockK slot 다중 캡처 | 같은 mock에서 여러 verify | 각 Given에서 별도 mock 생성 |
| jib `Obtaining project build output files failed` | 의존 서비스 모듈에 `tasks.jar { enabled = false }` → 라이브러리 variant 없음 | 해당 모듈에서 `tasks.jar` 비활성화 라인 제거 (Spring Boot 기본 동작 복원) |
| jib `MainClassInferenceException: Multiple valid main classes` | Kotlin `companion object` + `@JvmStatic fun main` → `Application`과 `Application$Companion` 둘 다 main 후보 | top-level `fun main { runApplication<Application>(*args) }` 패턴으로 변경 |
| CI `gradle: command not found` | `gradle/actions/setup-gradle@v5`는 gradle CLI를 PATH에 깔지 않음 | 워크플로에서 `./gradlew` 사용 (wrapper 호출) |
| 배포 후 routes/kafka 가 localhost 로 fallback 됨 | ConfigMap 의 `application.yml` 에 운영 설정이 빠져 있음. (구 모델에서 jar 의 application.yml 이 default 로 로드된다고 가정한 경우) | ConfigMap 의 `application.yml` 키에 jar 의 운영 설정을 모두 적는다. `/app/resources/application.yml` 에 subPath 마운트되어 jar 의 동명 파일을 file 단위로 overwrite. 머지 아님 — `SPRING_CONFIG_ADDITIONAL_LOCATION` 사용 금지 |
| GWT 빌드 결과가 48KB (dead code) | jar 에 Dagger 생성 소스 누락 → GWT 가 jar 소스를 읽다가 DaggerComponent 를 못 찾아 tree-shake | jar 태스크의 `from(sourceSets.main.get().allSource)` 제거. GWT 가 빌드 디렉토리의 소스+클래스를 직접 참조하도록 |
| Istio ambient 에서 외부 요청 500 | 서비스 port(80) ≠ HTTPRoute backendRef port(8080) | 서비스 port=targetPort=8080, name=http-xxx, appProtocol=http 로 통일 |
| Kargo warehouse 가 새 이미지 감지 못 함 | `strictSemvers: true` + jib `latest` 태그만 push | `strictSemvers: false` + jib 에 commit SHA 태그 추가 (`-Djib.to.tags=$SHORT_SHA`) |
| `aws s3 sync` 가 같은 크기 파일 스킵 | Gradle 재현가능 빌드의 mtime 고정 + 동일 사이즈 | `aws s3 cp --recursive` 로 무조건 업로드. helm vendored tgz 갱신 필요 |
| Gateway 라우트 0개 로딩 | 1) `spring.cloud.gateway.routes` (구 경로) 사용 — Spring Cloud Gateway 5.0부터 `spring.cloud.gateway.server.webflux.routes` 로 변경됨. 2) servlet classpath 오염 — activity 의존에서 `gwt-servlet-jakarta` 미제외 시 reactive auto-config 실패 | 프로퍼티 경로를 `spring.cloud.gateway.server.webflux.routes` 로 변경. activity 의존에 `exclude(group = "org.gwtproject", module = "gwt-servlet-jakarta")` 추가 |
| UI 모듈 렌더 안 됨 / 뷰포트 밖으로 밀림 | `Application.onModuleLoad()` 에서 `body().add(container)` 직접 호출. 전역 `body{position:fixed; inset:0}` + shell `#content{height:100dvh}` 뒤에 스택되어 y=100dvh 위치에 렌더됨 | `WindowRenderBridge.next(render)` 경유로 shell FrameUpdater 에 Render 전달 → Frame 엘리먼트 내부에 mount. 계약 상세는 `docs/contracts/frame.md` |

상세 패턴/코드 예시는 `.claude/skills/debugging.md` 참조.

---

## 에이전트 라우팅 (Claude 내부용)

Claude 가 큰 문서를 메인 컨텍스트에 직접 적재하지 않고, 업무별 서브에이전트에 위임하는 규칙.
설계 배경은 `.claude/agents/DESIGN.md` 참조.

### 도메인 → 에이전트 매핑

| 도메인 | 에이전트 | 주요 스코프 |
|--------|---------|------------|
| 인증·권한·JWT·RBAC·PAT | `auth-expert` | `login/`, `login-ui/`, `authentication/`, `docs/contracts/permissions.md` |
| 타입·속성·검증·레이아웃 | `schema-expert` | `schema/`, `type-ui/`, `persist-type/`, `search-type/` |
| 문서·이력·편집·임포트 | `document-expert` | `document/`, `document-ui/`, `persist-document/`, `search-document/` |
| 워크스페이스·그룹·프레즌스 | `workspace-expert` | `workspace/`, `workspace-ui/`, `persist-workspace/`, `search-workspace/` |
| 내부 AI·커맨드·감사 | `assistant-expert` | `assistant/`, `agent-protocol/`, `agent-ui/`, `docs/contracts/agent-commands.md` |
| SEO 랜딩·외부 AI·MCP | `landing-expert` | `landing-content/`, `landing-ui/`, §3.22, §3.23 |
| Shell·UI 공용·디자인·모바일·대시보드 | `ui-platform-expert` | `shell-ui/`, `ui-components/`, `app/`, `dashboard-ui/`, `docs/design.md` |
| Kafka·SSE·실시간 | `events-expert` | `event/`, `event-broadcaster/`, `docs/contracts/events.md` |
| 배포·Istio·Kargo·런타임 | `cluster-ops` | `charts/`, `.claude/skills/deployment.md`, `oc` 실행 가능 |
| 문서 구조·크로스체크·커버리지 | `docs-keeper` | `docs/**`, 모든 모듈 문서, 계약 매트릭스 |
| 증상 기반 triage·라우팅 계획 | `triage` | 도메인 판별·호출 순서만 결정 (도메인 답변 금지) |

### 라우팅 규칙

1. **도메인 깊이가 있는 요구사항/코드 조회** → 해당 도메인 에이전트 호출
2. **크로스 도메인 영향도** → 관련 전문가 **병렬 호출** 후 응답 합성
3. **계약 터치 (`docs/contracts/*.md` 중 하나에 해당)** → 계약 매트릭스(`docs/contracts/README.md`) 의 OWNER/소비자 전원 병렬 호출
4. **클러스터 상태·배포 문제** → `cluster-ops`
5. **문서 구조 변경·크로스체크·테스트 커버리지 감사** → `docs-keeper`
6. **코드 패턴 탐색 (keyword 검색)** → 내장 `Explore`
7. **설계 검토** → 내장 `Plan`
8. **증상 기반 버그 리포트** (사용자 관찰 현상 — "X 가 안 뜬다", "Y 가 깨진다") → 해당 도메인의 공급자(백엔드)·소비자(프론트) 에이전트 **양쪽 병렬 호출**. 원인이 어느 레이어인지 사전 판단 불가하므로 단일 에이전트 라우팅 금지.
9. **운영 장애** (런타임 증상 + 도메인 로직 혼합 — 예: "배포 후 OAuth 무한 리다이렉트") → `cluster-ops` **단독 호출 금지**. `cluster-ops` + 증상에 대응하는 도메인 에이전트 **병렬 호출**. cluster-ops 는 인프라·라우팅·Istio·런타임 진단, 도메인 에이전트는 서비스 내부 로직 검증.
10. **기존 10개 스코프에 맞지 않는 신규 모듈** → `docs-keeper` 에게 배치 제안 의뢰 (모듈 성격·관련 계약·근접 에이전트 분석) → 메인 Claude 가 판정: **기존 에이전트 스코프 확장**(선호) vs **신규 에이전트 신설**(복잡도 감당 불가 시). 결정 후 해당 에이전트 정의 파일 + CLAUDE.md 도메인 매핑 + `docs/contracts/README.md` 매트릭스 동시 갱신.
11. **응답의 `followup` 필드 자동 중계** → 도메인 에이전트가 응답에 구조화된 `=== followup ===` YAML 블록을 포함하면 메인 Claude 는 다음 규칙으로 처리한다 (DESIGN.md §11):
    - `priority: required` — 같은 세션에서 자동 후속 호출 (사용자 재승인 불필요)
    - `priority: optional` — "권장 추가 조사" 로 사용자 제시
    - **순환 방지**: 같은 에이전트가 세션 내 2회 이상 followup 대상이면 스킵 후 사용자 보고
    - **깊이 제한**: 후속 호출 최대 2단계. B 의 followup 으로 호출된 C 의 followup 은 자동 전개 금지
    - 에이전트 간 **직통 통신 금지** — 모든 중계는 메인 Claude 를 경유
12. **증상 기반 + 3개 이상 도메인 후보 / 다중 도메인 요청 / 매트릭스상 호출 대상 모호** → `triage` **먼저** 호출하여 `triage plan` 수신 (DESIGN.md §12) → plan 대로 parallel/sequential batch 실행. 단일 도메인이 명백하면 triage 생략 가능 (자체 호출 비용 방지).

### 계약 변경 감지 시 강제 절차

인터페이스·계약(Menu / Event / Command / Permission / API / Audit / Versioning / SSE / Design Token) 이 걸린 작업:

1. 해당 계약 문서(`docs/contracts/<X>.md`) 먼저 확인 — 공급자/소비자 인벤토리 파악
2. `docs/contracts/README.md` 매트릭스에서 OWNER · O · W 식별
3. 해당 에이전트 **병렬 호출** 하여 각자 영향도 수집
4. 응답 합성해 사용자에게 변경 범위·위험·작업 항목 제시
5. 승인 시 계약 문서 + 영향 받는 영역 동시 갱신

### 에이전트 노트 관리

각 에이전트는 `.claude/agents/<name>.notes.md` 를 자율 갱신한다.
Claude 는:

- 에이전트 응답의 `=== 노트 갱신 ===` 라인은 로그로만 수신, 개별 개입하지 않음
- 작업 흐름 중 `notes.md` 를 직접 건드리지 않음 (에이전트 자율 영역)
- 주기(주 1회) 또는 사용자 요청 시 `docs-keeper` 에게 노트 전수 감사 의뢰 — 매 호출 로그 누적 모델이므로 사이클 단축
- 반환된 "승격 후보" 검토 후 직접 수행:
  - **작업 원칙** → 해당 에이전트 정의(`.claude/agents/<name>.md`) 프롬프트 편집
  - **도메인 사실** → 요구사항/계약 문서로 이동
  - **공용 패턴** → 이 파일(CLAUDE.md) 또는 `.claude/skills/` 로 승격
- 승격 후 원본 항목은 notes 에서 제거 (단일 출처 유지)

### 작업 규칙 (Claude 자신)

#### 작업 착수 전 체크포인트 (필수, 자기 강제)

사용자 요청을 받자마자 **코드/파일을 건드리기 전에** 아래 5개 항목을 평가한다.
해당되면 즉시 서브에이전트를 병렬 호출. 이 체크포인트를 거르면 라우팅 규칙
§3/§9/§10 이 사문화된다 (규칙 제정자가 스스로 위반하는 회귀 — 2026-04-17 search-workspace 사례).

| # | 조건 | 위임 대상 | 근거 |
|---|------|---------|------|
| 1 | 특정 도메인 문서·요구사항·코드를 수정 예정 | 해당 `<domain>-expert` | §1 도메인 깊이 |
| 2 | `docs/contracts/<X>.md` 중 하나라도 touch | 매트릭스 OWNER·O 전원 **병렬** | §3 계약 변경 강제 절차 |
| 3 | `charts/` 수정 or 신규 Spring Boot 모듈 추가 or 배포 동반 | `cluster-ops` **필수 1회 이상** (+ 관련 도메인 병렬) | §9, §10 |
| 4 | 파일 3개 이상 Read 예정 or keyword 광역 탐색 | 내장 `Explore` 위임 | §6 + 컨텍스트 절약 |
| 5 | 기존 모듈 패턴을 새 모듈로 이식 (예: login → search-*) | 패턴 소유 도메인 에이전트에 "최소 의존성·설정 템플릿" 선제 질의 | 빌드 재시도 회귀 방지 |

#### 체크포인트 스킵 허용

- 단일 파일의 명백한 수정 (오타, 한 줄 테스트 추가)
- 사용자가 명시적으로 "빠르게" / "직접" 지시한 경우
- 스킵할 때도 **왜 스킵했는지** 응답 앞부분에 한 줄 명시 (예: "단일 문서 오타 수정 — 체크포인트 스킵")

#### 작업 마무리 자기 감사

커밋 직전 자문:
- 이번 작업에서 서브에이전트 호출 수는? 0이면 왜 0이었는지 정당화 가능한가?
- 계약 매트릭스 행을 변경했는데 OWNER·O 중 병렬 호출 안 한 에이전트가 있나?
- 빌드·배포 재시도가 2회 이상 발생했다면, 도메인 에이전트에 선제 질의했으면 막을 수 있었나?

답이 "없다/있다" 로 불편하면 다음 회 체크포인트 적용을 강화한다.

#### 기타 규칙

- 사용자에게 에이전트 존재를 노출하지 않음 — 합성된 결과만 전달
- 에이전트 정의 파일(`<name>.md`) 은 Claude 만 수정. 에이전트는 `notes.md` 만 수정
- "단일 도메인 요청으로 보이는 것" 도 공통 계약 건드리면 병렬 호출로 확장

### 에이전트 호출 시 맥락 전달 (one-shot 보정)

서브에이전트는 대화 상태를 보존하지 않는 **one-shot 호출** 이다. 필요한 맥락은 프롬프트에 명시적으로 포함해야 한다:

- **작업 배경 요약** — 왜 이 조회가 필요한지 (1-2문장)
- **시간 범위 / 커밋 범위** — "최근 커밋 abc1234 이후" 같은 경계
- **제약 / 비목적** — "코드 수정 아니고 조회만", "요약만 반환 (100단어)"
- **반환 포맷** — 자유 형식 vs 표 vs 특정 필드

예시:

```
좋음:
  Agent(schema-expert, "
    2026-04-17 커밋 df4e976 에서 landing-content 모듈 신설됨.
    이 모듈이 schema 도메인과 상호작용하는지 검토 — 공유 의존성 또는
    잠재 충돌이 있는가? 150단어 이내 요약.
  ")

나쁨:
  Agent(schema-expert, "랜딩 모듈 영향 봐줘")
    → 에이전트가 어떤 랜딩? 언제? 어떤 영향? 알 수 없음
```

### 도메인 파일 해상도 (폴백 규칙)

에이전트 정의의 스코프에 언급된 문서 경로는 다음 순서로 해상된다:

1. `docs/requirements/<domain>.md` 가 존재하면 → 그 파일 사용
2. 없으면 → `docs/requirements.md` 에서 해당 도메인의 §X.Y 섹션 사용
3. 도메인 ↔ 섹션 매핑은 `docs/requirements/README.md` 인덱스 참조
4. 위 둘 다 없으면 → `.claude/skills/` 또는 `docs/` 루트의 관련 문서로 폴백

마찬가지로 `docs/usecases/` 도 `docs/usecases/README.md` 인덱스 참조, 없으면 `docs/usecases.md` UC 번호 범위로 폴백.

docs-keeper 가 migration 진행하면서 단계적으로 1번 경로로 전환된다.
