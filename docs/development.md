# 로컬 개발 환경 구성 가이드

## 사전 요구사항

- **JDK 21** 이상
- **Docker** 및 **Docker Compose** (컨테이너 기반 인프라 실행용)
- **Node.js** (GWT 개발 시 선택사항)

## 인프라 실행

프로젝트 루트에서 Docker Compose를 사용하여 PostgreSQL과 Kafka를 실행합니다.

```bash
docker-compose up -d
```

실행되는 서비스:

| 서비스 | 포트 | 설명 |
|--------|------|------|
| PostgreSQL | 5432 | 메인 데이터베이스 (DB: handbook, 사용자: handbook) |
| Kafka | 9092 | 이벤트 브로커 |
| Zookeeper | 2181 | Kafka 메타데이터 관리 |

인프라 중지:

```bash
docker-compose down
```

데이터를 포함하여 완전히 초기화:

```bash
docker-compose down -v
```

## 개별 모듈 실행

각 Spring Boot 모듈은 Gradle로 개별 실행할 수 있습니다.

```bash
# Gateway
./gradlew :gateway:bootRun

# Assistant
./gradlew :assistant:bootRun

# App (통합 실행)
./gradlew :app:bootRun
```

환경 변수를 통해 포트 등을 변경할 수 있습니다:

```bash
SERVER_PORT=9090 ./gradlew :assistant:bootRun
```

## GWT 개발 모드

UI 모듈 (shell-ui, login-ui, document-ui, type-ui, workspace-ui, agent-ui, dashboard-ui 등)은 GWT로 빌드됩니다.

```bash
# GWT 컴파일
./gradlew :shell-ui:compileGwt
./gradlew :login-ui:compileGwt
./gradlew :document-ui:compileGwt
./gradlew :dashboard-ui:compileGwt

# 테스트용 HTML 확인 (각 모듈의 src/test/webapp 디렉토리)
```

GWT 개발 시에는 각 UI 모듈의 `src/test/webapp` 디렉토리에 있는 HTML 파일을 통해 개별 컴포넌트를 테스트할 수 있습니다.

## 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :assistant:test
./gradlew :login:test
./gradlew :persist-workspace:test
./gradlew :dashboard-ui:test

# 커버리지 리포트 생성
./gradlew koverHtmlReport
```

테스트 프레임워크:
- **Kotest**: Kotlin 기반 테스트 (BehaviorSpec 스타일)
- **MockK**: Kotlin 모킹 라이브러리
- **Reactor Test**: 리액티브 스트림 테스트
- **Testcontainers**: 통합 테스트용 컨테이너

## E2E 테스트

Playwright 기반 E2E 테스트는 `e2e` 모듈에서 실행합니다.

```bash
# Playwright 브라우저 설치 (최초 1회)
npx playwright install chromium

# E2E 테스트 실행
./gradlew :e2e:test
```

## 유용한 명령어

```bash
# 전체 빌드 (테스트 포함)
./gradlew build

# 린트/포맷 확인
./gradlew check

# 의존성 트리 확인
./gradlew :assistant:dependencies
```
