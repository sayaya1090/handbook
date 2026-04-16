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

### I18N (다국어)
- **UI 텍스트는 LabelProvider를 통해 다국어 처리.** 한국어 하드코딩 금지.
- 언어 파일: `src/main/i18n/language.{ko,en}.json` → 빌드 시 머지

### 디자인 언어 통일
- **MD3 디자인 토큰만 사용.** 하드코딩 금지. 상세는 `.claude/skills/design-tokens.md` 참조.

### 클래스 Javadoc (필수)
- **모든 클래스에 역할/책임/의존관계/주의점 포함 Javadoc(KDoc) 작성.**
- 신규 클래스 생성 시 필수. 기존 클래스 수정 시 없으면 추가.

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

### 테스트
- 백엔드: Kotest BehaviorSpec + MockK / Testcontainers PostgreSQL
- 프론트엔드: GWT 컴파일 + Playwright (./gradlew test에 포함)
- E2E: Playwright + HttpClient (E2E 환경변수, 서버 실행 필요)
- **Then 검증은 구체적으로:** `shouldNotBe null`만으로 끝내지 않는다. 속성 값, 개수 변화, 상태 토글, 양방향(추가→제거) 검증 필수.
- 상세 패턴은 `.claude/skills/debugging.md`의 "Playwright 테스트 검증 패턴" 참조
- **UI 컴포넌트는 sayaya-ui 사용 필수.** 네이티브 HTML 금지. 사용법은 `.claude/skills/sayaya-ui.md` 참조
- **모든 UI 요소에 테스트 가능한 고유 CSS 클래스 필수.** 제네릭 클래스(`.type-ctrl-btn`)만 사용 금지 → `.css("type-ctrl-btn", "type-ctrl-btn-add")` 형태로 구체적 클래스 추가. `:first-child`/`:last-child` 대신 `data-*` 속성이나 고유 클래스 사용.

## 빌드 & 테스트

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

상세 패턴/코드 예시는 `.claude/skills/debugging.md` 참조.
