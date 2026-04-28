---
name: dev-environment
description: 로컬 개발 환경 및 테스트 가이드
---

# 개발 환경 설정

## 인프라
```bash
docker-compose up -d  # PostgreSQL + Kafka
```

## 환경변수 (기본값 사용 가능)
| 변수 | 기본값 | 설명 |
|------|--------|------|
| DB_HOST | localhost | PostgreSQL 호스트 |
| DB_PORT | 5432 | PostgreSQL 포트 |
| DB_NAME | handbook | 데이터베이스 이름 |
| DB_USER | handbook | DB 사용자 |
| DB_PASSWORD | handbook | DB 비밀번호 |
| KAFKA_BROKERS | localhost:9092 | Kafka 브로커 |
| JWT_SECRET | default-dev-secret... | JWT 서명 키 (PEM) |
| GOOGLE_CLIENT_ID | - | OAuth2 클라이언트 ID |
| GOOGLE_CLIENT_SECRET | - | OAuth2 클라이언트 시크릿 |

## 서비스 포트
| 서비스 | 포트 | 설명 |
|--------|------|------|
| gateway | 8080 | API 게이트웨이 |
| login | 8081 | OAuth2 인증 |
| type-query | 8082 | 타입 조회 (CQRS) |
| type-command | 8083 | 타입 저장 |
| document-query | 8084 | 문서 조회 (CQRS) |
| document-command | 8085 | 문서 저장 |
| workspace-command | 8086 | 워크스페이스 관리 |
| assistant | 8087 | AI 에이전트 |
| event-broadcaster | 8088 | Kafka → SSE |

## 빌드 & 테스트

### 전체 테스트
```bash
./gradlew test                    # 백엔드 + GWT Playwright 테스트 (E2E 제외)
E2E=true ./gradlew :e2e:test      # E2E (서버 실행 필요)
```

### 모듈별
```bash
./gradlew :모듈:compileJava       # 컴파일
./gradlew :모듈:compileKotlin     # Kotlin 컴파일
./gradlew :모듈:test              # 테스트
./gradlew :모듈:gwtDev            # GWT DevMode
./gradlew :gateway:bootRun        # 백엔드 실행
```

### 주의사항
- 통합 테스트(R2dbc*IntegrationTest)는 Testcontainers로 PostgreSQL 자동 실행
- GWT 테스트는 모듈별 고유 포트 할당 (병렬 실행 가능, `--parallel`)
- E2E 테스트는 게이트웨이 + 모든 백엔드 서비스 실행 필요

## Prometheus / Grafana 로컬 설정 (7.4 관측성)

### docker-compose 추가 서비스

```yaml
# docker-compose.yml에 추가
prometheus:
  image: prom/prometheus:latest
  ports:
    - "9090:9090"
  volumes:
    - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana:latest
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin
```

### prometheus.yml 예시

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
  - job_name: 'document-command'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8085']
  - job_name: 'document-query'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8084']
  - job_name: 'event-broadcaster'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8088']
  - job_name: 'assistant'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8087']
```

### Spring Boot Actuator 설정 (각 서비스)

```yaml
# application.yml에 추가
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,metrics
  metrics:
    tags:
      application: ${spring.application.name}
```

### 주요 모니터링 메트릭

| 메트릭 | 설명 |
|--------|------|
| `http_server_requests_seconds` | HTTP 요청 지연 시간 (히스토그램) |
| `r2dbc_pool_acquired` | R2DBC 풀 사용 중 커넥션 수 |
| `r2dbc_pool_pending` | R2DBC 풀 대기 중 요청 수 |
| `kafka_consumer_records_lag` | Kafka 컨슈머 랙 |
| `dlq_events_total` | DLQ에 저장된 이벤트 수 (커스텀) |

### 접속 URL
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## GitHub Actions Runner 이미지

`charts/handbook-operator/github-actions-runner-set/templates/actions-runner.yaml`는 OpenShift BuildConfig로 러너 이미지를 빌드한다 (UBI10 기반). 업데이트는 항상 **로컬 podman 검증 → yaml 반영** 순서로 한다.

### 업데이트가 필요할 때
- **러너 버전 올리기**: `RUNNER_VERSION`, `RUNNER_CONTAINER_HOOKS_VERSION`, `HELM_VERSION` `ARG`를 해당 프로젝트의 GitHub 릴리즈 최신 태그로 맞춘다.
  ```bash
  curl -sL https://api.github.com/repos/actions/runner/releases/latest | grep tag_name
  curl -sL https://api.github.com/repos/actions/runner-container-hooks/releases/latest | grep tag_name
  curl -sL https://api.github.com/repos/helm/helm/releases/latest | grep tag_name
  ```
- **UBI 베이스 올리기** (ubi10 → ubi11 등): `FROM registry.access.redhat.com/ubi<N>/ubi-init:latest`. el 메이저가 바뀌면 패키지가 사라지거나 이름이 바뀔 수 있으므로 아래 "패키지 해결 순서"를 따른다.
- **도구 추가/제거**: dnf 설치 목록을 수정하기 전에 아래 "도구별 주의점" 확인.

### 로컬 검증 (필수, Apple Silicon 기준)
```bash
# 1) BuildConfig 안의 dockerfile 블록을 /tmp/runner-build/Dockerfile 로 복사
# 2) arm64로 빌드 (amd64 에뮬레이션 금지)
mkdir -p /tmp/runner-build && $EDITOR /tmp/runner-build/Dockerfile
podman build --arch=arm64 -t handbook-actions-runner:test /tmp/runner-build

# 3) 한국어 로케일 + 주요 도구 스모크 체크
podman run --rm --user root --arch=arm64 handbook-actions-runner:test bash -c '
  LANG=ko_KR.UTF-8 date &&
  helm version --short &&
  for c in docker gh oc kubectl aws zsh openssl git git-lfs jq; do command -v $c; done &&
  ls /home/runner/run.sh /home/runner/k8s/index.js'
```
- **반드시 `--arch=arm64`**: Apple Silicon에서 `--arch=amd64`로 UBI를 빌드하면 qemu 환경의 OpenSSL provider 서명 검증이 깨져 `cdn-ubi.redhat.com` TLS 실패가 난다.
- `oc`는 mirror.openshift.com에 amd64 바이너리만 있어 arm64 실행 시 `rosetta error`가 나지만, 실제 OpenShift(amd64) 빌드에선 정상이므로 무시한다.
- 로컬 검증 통과 후 yaml의 dockerfile 블록에 변경사항을 그대로 복사한다 (들여쓰기가 `|` 블록이라 rendering 주의).

### 빌드 실패 시 패키지 해결 순서
`dnf install ... No match for argument: <pkg>`가 뜨면 다음 순서로 조치한다.
1. **대체 이름 검색**: 베이스 이미지에서 `dnf --enablerepo='*' list --available '<pattern>*'`. 예: `liberation-sans-fonts` 제거 → `dejavu-sans-fonts`.
2. **리포 추가**: UBI/CRB에 없으면 EPEL10(`epel-release-latest-10.noarch.rpm`). CRB는 `/usr/bin/crb enable`로 활성화 (이미 스크립트에 포함).
3. **제거 가능성 판단**: 러너 구동에 진짜 필요한 라이브러리는 `libicu openssl krb5-libs zlib`뿐. 나머지는 워크플로 요구사항에 따라 유지/삭제.

### 도구별 주의점
- **docker**: runner-container-hooks가 job 컨테이너를 k8s API로 스폰하므로 **daemon 불필요**. `docker-ce-cli`만 쓴다. daemon을 다시 넣으려면 UBI에 없는 `iptables-nft`를 별도로 해결해야 한다.
- **Chromium / Playwright**: GWT 모듈의 Kotest + Playwright 테스트가 러너에서 돌려면 **(a) chromium 런타임 OS 라이브러리**, **(b) chromium 바이너리 자체**, **(c) runtime 자동 install 차단** 세 가지가 필요하다.
  - **OS 라이브러리**: `nss atk at-spi2-atk at-spi2-core cups-libs libdrm libXcomposite libXdamage libXext libXfixes libXrandr libXcursor mesa-libgbm pango alsa-lib libxkbcommon libxshmfence gtk3 cairo-gobject gdk-pixbuf2` + 폰트(`dejavu-sans-fonts dejavu-serif-fonts dejavu-sans-mono-fonts`). UBI10 에 `liberation-sans-fonts` 는 없고 `dejavu-*-fonts` 만 있다. `gtk3/cairo-gobject/gdk-pixbuf2/libXcursor` 누락 시 Playwright 가 "Host system is missing dependencies" 경고를 찍고 browser launch 시 crash.
  - **Node.js**: `nodejs` (playwright 드라이버가 Node 기반). 별도 버전 고정 안 함 — UBI10 기본 stream 사용.
  - **Chromium 바이너리 pre-install (chromium 만)**: `PLAYWRIGHT_BROWSERS_PATH=/opt/playwright npx -y playwright@<ver> install chromium` 을 빌드 시 root 로 실행 후 `chown -R runner:0 /opt/playwright`. **`install` (인자 없음)을 쓰면 firefox + webkit 까지 받으려고 해서 Azure fallback CDN 에서 ~500MB 를 다운로드** — 빌드가 수 십 분 걸리고 이미지 크기 폭증. 테스트가 chromium 만 쓰므로 인자를 명시한다.
  - **Runtime 자동 install 차단**: playwright-java 는 `Playwright.create()` 시점에 `node cli.js install` 을 자동 호출하고 기본적으로 **전체 브라우저를 설치**하려 한다. 이미지 ENV 에 `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1` 을 박아서 runtime install 을 no-op 으로 만든다. 기존 pre-installed chromium 은 그대로 사용.
  - **버전 동기화**: 이미지에 pre-install 하는 playwright 버전은 `libs.versions.toml` 의 `version("playwright", ...)` 와 맞춘다. 불일치 시 playwright-java 가 경고 후 재다운로드.
  - **dbus 경고는 무시**: 러너 파드엔 dbus 가 없어 chromium 이 `Failed to connect to the bus` 를 찍지만 기능에 영향 없음.
- **한국어 로케일**: UBI10부터는 `glibc-langpack-ko`가 개별 패키지로 없다. `glibc-all-langpacks`(~200MB) 대신 `glibc-langpack-en` + `glibc-locale-source`를 설치한 뒤 `localedef -c -i ko_KR -f UTF-8 ko_KR.UTF-8`로 ko_KR만 컴파일한다 (로케일 디렉토리 ~8MB로 감축).
- **`./bin/installdependencies.sh` 호출 금지**: 러너 tarball에 딸려오는 이 스크립트는 `lttng-ust` 등 el10에 없는 패키지를 강제로 깔려다 실패한다. 위의 필수 라이브러리 4개만 우리가 직접 설치하면 runner는 정상 기동한다.
- **helm 설치**: `get-helm-3` 스크립트는 `get.helm.sh`에 대한 실패 시 재시도 로직이 없어 빌드 네트워크 플레이키에 취약하다. **GitHub/CDN에서 pinned tarball을 curl `--retry`로 직접 받는다** (`HELM_VERSION` ARG로 고정).
- **pip**: el10+ Python은 PEP 668 환경이라 `pip3 install` 에 `--break-system-packages` 필수.
- **GitHub 릴리즈 CDN**: `runner-container-hooks` zip 등에 간헐적 504가 뜨므로 tarball/zip 다운로드 curl에는 `--retry 8 --retry-delay 10 --retry-all-errors` 유지.
