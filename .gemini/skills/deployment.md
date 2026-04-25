# 배포 · Helm 차트 구조

OpenShift + ArgoCD + Kargo 기반의 2계층 GitOps. `charts/` 아래에 두 차트가 있으며, 계층과 역할이 명확히 분리된다.

## 2계층 구조

```
handbook-operator (루트, 수동/운영자 배포)
  ├── ArgoCD Application "handbook"  → charts/handbook 를 GitOps 로 싱크
  ├── GitHub Actions Runner ScaleSet (서브차트, gha-runner-scale-set dependency, alias ubi10)
  └── 운영자용 Job / RBAC

handbook (ArgoCD 가 싱크하는 런타임 차트)
  ├── ApplicationSet  → 스테이지(dev/staging/prod) × 서비스 매트릭스 전개
  ├── 서브차트 infrastructure/  (CloudNativePG, S3, Observability)
  ├── 서브차트 gateway/          (서비스 1)
  └── 서브차트 event-broadcaster/ (서비스 2)
```

**사람은 `handbook-operator` 만 배포**하고, 그 뒤부터는 ArgoCD 가 `handbook` 차트를 싱크하며 서비스 배포가 연쇄된다. 신규 서비스 차트가 생기면 ArgoCD 가 자동 배포한다.

## `charts/handbook-operator/`

| 파일 | 역할 |
|------|------|
| `Chart.yaml` | operator 차트 메타 |
| `values.yaml` | 비어 있음 — 서비스 목록은 handbook 차트가 소유 |
| `templates/application.yaml` | ArgoCD `Application` "handbook" 생성. `charts/handbook` 경로를 `targetRevision: HEAD` 로 싱크. sync-wave -10 |
| `templates/github-actions-runner-set.yaml` | `gha-runner-scale-set` 서브차트에 values 주입 (`ubi10:` 블록). runner pod 템플릿, dind 사이드카, init container, 캐시 PVC 마운트 정의 |
| `templates/job.yaml` | AutoscalingListener의 Role/RoleBinding 에 `IgnoreExtraneous` 애노테이션을 붙이는 PostSync Job — ArgoCD diff 노이즈 제거용 |
| `templates/rbac.yaml` | operator 서비스어카운트 권한 |

### 서브차트 `github-actions-runner-set/`
| 파일 | 역할 |
|------|------|
| `Chart.yaml` | `gha-runner-scale-set` 차트를 alias `ubi10` 으로 의존 (alias 는 runtime values 키와 일치) |
| `templates/actions-runner.yaml` | OpenShift `BuildConfig` + `ImageStream` — UBI10 기반 runner 이미지를 클러스터에서 직접 빌드 (`.gemini/skills/dev-environment.md` GitHub Actions Runner 섹션 참조) |
| `templates/cache.yaml` | `.Values.ubi10.template.spec.volumes` 에서 cache PVC 이름을 읽어 10Gi PVC 생성. `_work`, `.gradle`, `gradle-installations/installs` 캐시 공유 |
| `templates/docker-daemon-config.yaml` | dind 사이드카용 `daemon.json` ConfigMap — `storage-driver: fuse-overlayfs`, `mtu: 1350` |
| `templates/job.yaml`, `rbac.yaml` | Runner 네임스페이스 초기화 Job · RBAC |

### 외부 의존
- **`github-secret`**: GitHub App 자격증명 (`github_app_id`, `github_app_installation_id`, `github_app_private_key`). 생성 예시는 `charts/handbook-operator/github-actions-runner-set/README.md`.

## `charts/handbook/`

### 값 스키마 (`values.yaml`)
- **`stages`**: `dev | staging | prod` 각각에 `database`(PG IP), `host`(FQDN), `color`, `backup.schedule`(cron; 빈 문자열이면 백업 비활성), `bucket.maxSize`(S3 PVC 크기)
- **`services`**: `[{name, kind, color, stages: [...]}]` — 어떤 서비스를 어떤 스테이지에 배포할지 매트릭스. `kind` 는 `backend`(Spring Boot JVM 이미지 기반 promotion) 또는 `frontend`(GWT 정적 자산 git-tag 기반 promotion) 로 release-staging / release-prod bundle 템플릿에서 promotion 패턴을 분기한다. `color` 는 Kargo UI 에서 Warehouse 카드 구분용(ApplicationSet 이 각 서비스 Application 의 helm parameter `color` 로 주입 → 서브차트 `templates/warehouse.yaml` 이 `kargo.akuity.io/color` 애노테이션에 적용).

### 루트 템플릿
| 파일 | 역할 |
|------|------|
| `templates/application-set.yaml` | ArgoCD `ApplicationSet` — `stages × services` 곱을 matrix generator 로 전개해 Application 을 자동 생성 |
| `templates/image-stream.yaml` | 서비스별 `ImageStream` (OpenShift 내부 레지스트리 태그 추적 대상) |
| `templates/project-config.yaml` | ArgoCD `AppProject` 설정 |
| `templates/job.yaml`, `rbac.yaml` | 초기화 Job · RBAC |

### 서비스 서브차트 (`gateway/`, `event-broadcaster/`, `login/`, `persist-workspace/`)
동일 패턴. 신규 JVM 백엔드 추가 시 이 구조를 복사한다. Deployment 는 `handbook-lib` 라이브러리 차트의 named template `handbook.jvm-backend.deployment` 로 통합되어 있어 서브차트 `templates/deployment.yaml` 은 한 줄 include (`{{ include "handbook.jvm-backend.deployment" . }}`) 로 끝난다. 운영 설정에 DB/Kafka 가 필요하면 `configmap.yaml` 의 `spring.config.import` 에 해당 fragment classpath 를 추가하고 `values.yaml` 의 `jvmBackend.fragments` 리스트에도 이름(postgresql / kafka) 을 추가한다.

| 파일 | 역할 |
|------|------|
| `Chart.yaml` | handbook-lib (type: library) 를 `file://../../handbook-lib` 로 dependency 등록. `helm dependency build` 가 Chart.lock + charts/handbook-lib-*.tgz 생성 |
| `templates/configmap.yaml` | **jar 의 `application.yml` 을 파일 단위로 대체하는 운영 설정**. Deployment 가 이 ConfigMap 의 `application.yml` 키를 `/app/resources/application.yml` 에 subPath 로 마운트하여 jar 의 동명 파일을 덮어쓴다. **머지(SPRING_CONFIG_ADDITIONAL_LOCATION)는 사용하지 않는다** — 운영 환경에서 필요한 모든 설정(application.name, routes, cloud.stream bindings, kafka producer, server.port, cors 등)을 jar 와 중복되더라도 이 파일 안에 다 넣는다. jar 의 application.yml 은 로컬 IDE 실행용 default 로만 사용. 공통 단편(observability, postgresql, kafka, authentication)은 `spring.config.import: [classpath:observability.yaml, ...]` 로 fragment ConfigMap 에서 가져온다 |
| `templates/deployment.yaml` | **한 줄 include** — `handbook-lib` 의 `handbook.jvm-backend.deployment` named template 호출. 서비스별 차이는 `values.yaml` 의 `jvmBackend` 섹션으로 흡수: `deploymentName` (기본 `.Chart.Name`, gateway 만 `service-gateway`), `fragments: [observability, authentication, postgresql, kafka]` 필요한 것만, `extraEnv` (tpl 지원 → `https://{{ .Values.host }}/` 같은 표현식 사용 가능). 템플릿이 fragment 이름에서 ConfigMap 이름·mount path·`configmap.reloader.stakater.com/reload` 명단을 자동 생성하고, `postgresql` fragment 있으면 CNPG `postgresql-app` secret 에서 DB_HOST/PORT/NAME/USERNAME/PASSWORD 자동 주입, `kafka` fragment 있으면 `KAFKA_BROKERS` 자동 주입 |
| `templates/service.yaml` | ClusterIP Service (포트 8080) |
| `templates/stage.yaml` | **Kargo `Stage`** — 서비스 × 스테이지 promotion 파이프라인 |
| `templates/warehouse.yaml` | **Kargo `Warehouse`** — dev 스테이지의 첫 서비스에만 생성, ImageStream 을 1분 간격으로 감시해 새 빌드 Freight 생성. `kargo.akuity.io/color` 애노테이션을 `.Values.color` 에서 받아 Kargo UI 에서 서비스별로 구분 |

### 외부 진입점 (Ingress)
- **Kubernetes Gateway API** + OpenShift Route 로 `handbook.apps.sayaya.cloud` (dev) 호스트 노출. 사용자 nginx LB(192.168.1.9, L4 stream) 가 cluster nodes :443 으로 forward → OpenShift Router → `handbook-istio` Service → Istio Gateway
- `infrastructure/templates/gateway/` 의 `Gateway` CR → Istio(istio GatewayClass) 가 `handbook-istio` Service 자동 프로비저닝 → OpenShift `Route` 가 TLS edge 로 외부 노출 (`*.apps.sayaya.cloud` 기본 wildcard cert 사용)
- 트래픽 분기: 서비스별 HTTPRoute 가 구체 path 매칭으로 우선하고, `infrastructure/templates/gateway/http-route.yaml` 의 catch-all 이 나머지를 Spring Cloud Gateway(`service-gateway:8080`) 로 포워딩
- **Cross-namespace backend 우회**: Istio gateway 컨트롤러가 `ReferenceGrant` 를 올바로 인식 못 해서 HTTPRoute 가 `openshift-storage` 의 Ceph RGW 를 직접 참조하면 `RefNotPermitted` 가 난다. 같은 ns 에 `ExternalName` Service `ceph-rgw` 를 두고 (`infrastructure/templates/gateway/ceph-rgw-service.yaml`) HTTPRoute 가 이를 가리키게 우회
- 상세 비교 + 매칭 우선순위 + 점진적 이전 계획은 `docs/ingress-options.md` 참조

### Spring 설정 주입 모델 (요약)
- **운영 설정의 단일 진리는 ConfigMap.** jar 의 `application.yml` 은 로컬 dev fallback.
- **머지 안 함, 파일 단위 overwrite.** `/app/resources/application.yml` 위치에 subPath 마운트해 jar 의 동명 파일을 통째로 대체. SPRING_CONFIG_ADDITIONAL_LOCATION 금지.
- **공통 fragment** 는 `infrastructure/` 서브차트가 소유하고, `spring.config.import: classpath:<name>.yaml` 로 끌어온다. fragment 는 `/app/resources/<name>.yaml` 에 subPath 마운트.
- **fragment 카탈로그**: `observability` (management/metrics/logging), `handbook-postgresql` (r2dbc), `handbook-kafka` (spring.kafka.bootstrap-servers), `handbook-authentication` (security.authentication).

### Kargo Promotion 파이프라인 — Release Train (v2)

**dev 만 서비스별 독립 stage, staging/prod 는 모든 서비스가 한 묶음(번들)**:

```
[gateway-dev]           ┐
[event-broadcaster-dev] ┤
[login-dev]             ┤
[persist-workspace-dev] ┼──→ release-staging ──→ release-prod
[shell-ui-dev]          ┤   (모든 서비스 동시       (release-staging 통과 번들을
[login-ui-dev]          ┤    atomic 배포)            그대로 prod 로 전진)
[workspace-ui-dev]      ┘
```

- **`<svc>-dev` Stage** (서비스 subchart `templates/stage.yaml`, dev 일 때만 렌더): 자기 Warehouse 만 구독 + autoPromotion=true. 새 Freight 가 발행되면 즉시 dev 환경에 자동 배포
- **`release-staging` Stage** (`templates/release-staging.yaml`): 모든 서비스의 `<svc>-dev` 를 sources 로 받아 한 Stage 에 multi-Warehouse 구독. autoPromotion=false 라 사람이 "이번 릴리즈 후보를 묶자" 는 시점에 Kargo UI 에서 수동 승격. promotion 시 모든 서비스의 staging Application 을 동시 update
- **`release-prod` Stage** (`templates/release-prod.yaml`): release-staging 을 단일 upstream 으로 구독. release-staging 을 통과한 동일 digest/commit 번들이 그대로 prod 로 전진. autoPromotion=false (사람이 출시 결정)
- **ProjectConfig** (`templates/project-config.yaml`): `<svc>-dev` 에만 autoPromotion=true, release-staging/release-prod 는 explicit autoPromotion=false

**서비스 종류별 promotion key 처리**:
- **JVM 백엔드** (image 기반): `compose-output` 으로 imageFrom 결과 캡처 → `argocd-update.helm.images[key=image.tag, value=tag@digest]` 로 Application 갱신
- **GWT 프론트엔드** (`services[].kind == "frontend"` — app, shell-ui, login-ui, workspace-ui, …): `argocd-update.helm.images[key=freight.commit, value=${{ commitFrom(...).ID }}]` 직접 주입 (compose-output 불필요)
- 두 패턴이 release-staging/release-prod 의 promotionTemplate 안에 `eq $service.kind "frontend"` 분기로 공존

**기존 (v1) 잔재**:
- 서비스 subchart 의 `templates/stage.yaml` 은 `if eq .Values.stage.name "dev"` 가드로 dev 만 렌더. staging/prod 인스턴스가 helm parameter 로 들어와도 빈 manifest 를 만들어 Kargo CR 충돌 방지
- ApplicationSet 은 여전히 (service × stage) 매트릭스로 Application 을 생성 — staging/prod 는 deployment manifest 만 들고 Kargo Stage 가 직접 update

**(B) GWT 프론트엔드 정적 자산 — GitHub Release + ArgoCD Hook Job 기반** (shell-ui, login-ui, workspace-ui, …)
- **Build**: GHA(`<module>-deploy.yaml`) 가 `:<module>:build` → WAR 에서 정적 자산 추출 → tar 묶음 → `gh release create <module>-<sha> --prerelease` 로 GitHub prerelease + asset 업로드. GHA 는 빌드 + publish 까지만, deploy 액션 0번
- **Warehouse**: git 구독, `commitSelectionStrategy: Lexical` + `allowTags: ^<module>-` + `strictSemvers: false` → 새 prerelease tag 마다 Freight 발행. **필터 필드 이름은 `allowTags` 다 — `includeTags` 는 CRD 스키마가 unknown fields 를 허용해 오탈자가 조용히 무시되고, 모듈을 구분 못 해 다른 모듈 tag 까지 긁어오게 된다.** **`NewestTag` 는 Kargo 의 유효한 enum 이 아니어서** 설정 시 기본 `NewestFromBranch` 로 fallback 되고 tag 가 무시되는 함정이 있으니 `Lexical` 을 쓴다. ⚠️ **Lexical 은 문자열 사전순 정렬이라 sha 기반 tag(`shell-ui-<7자 sha>`) 에선 "알파벳 상 가장 큰 것" = 반드시 최신이 아님**. 워크플로가 publish 시 이전 release/tag 를 정리하거나, 정렬 가능한 prefix(타임스탬프 등)를 tag 에 붙이는 형태로 운영해야 한다
- **dev Stage** (서비스 subchart `templates/stage.yaml`): JVM 백엔드와 마찬가지로 `<svc>-dev` Stage 가 자기 Warehouse 만 구독, autoPromotion=true. helm parameter `freight.commit` 으로 chart 의 sync-job 에 commit SHA 주입
- **staging/prod Stage**: 서비스 subchart 가 만들지 않고 **release-staging/release-prod 번들** Stage 가 multi-Warehouse 구독으로 묶어서 처리 (위 Release Train 섹션 참조)
- ⚠️ **Kargo `argocd-update` step 의 `helm` 블록은 스키마상 `parameters` 를 허용하지 않고 `images` 배열만 받는다** — 이름은 image 용이지만 `key` 에 임의 helm parameter path 를 쓸 수 있어 `freight.commit` / `bucket` 같은 값 주입에 재활용한다. 표현식으로 commit SHA 를 얻을 때는 `${{ commitFrom("...").ID }}` — Kargo 표현식 엔진이 Go struct 필드명(대문자 포함)을 그대로 노출하므로 `.id`/`.tag` 는 `has no field` 에러를 낸다
- **Sync Job (chart 내장, 라이브러리 템플릿)**: 각 프론트엔드 서브차트의 `templates/sync-job.yaml` 은 `{{ include "handbook.frontend-sync-job" . }}` 한 줄이다. 실제 Job 정의는 `handbook-lib` 라이브러리 차트의 `_frontend-sync-job.yaml` named template 이 소유하며, tag prefix / job 이름 / label 은 `.Chart.Name` 에서 자동 유도. ArgoCD reconcile 시 새 freight commit 마다 새 Job 을 만든다 (`argocd.argoproj.io/hook: Sync` + `hook-delete-policy: BeforeHookCreation`). Job 컨테이너(`amazon/aws-cli`)가 GitHub release asset 을 **unauthenticated** (public repo) 로 `curl -fsSL "https://github.com/.../releases/download/<tag>/<asset>"` 로 받아 `aws s3 sync s3://${bucket}/static/` 수행 후 종료. `ttlSecondsAfterFinished` 로 자동 정리
- 백엔드 `argocd-update` 패턴과 100% 동일 — Kargo 가 Application 갱신, ArgoCD 가 reconcile, K8s 가 실행

즉, dev 는 자동 전진, staging/prod 는 수동 (또는 승인) promotion. 승격 단위는 Freight — JVM 은 image digest, 정적 자산은 git tag.

### 서브차트 `infrastructure/`
| 파일 | 역할 |
|------|------|
| `cloudnative-pg/cluster.yaml` | CloudNativePG `Cluster` — 3 인스턴스 PostgreSQL. `values.stages.<stage>.database` 가 비어 있지 않으면 외부 PG IP 로 프록시, 비어 있으면 자체 운영. Barman S3 백업 설정 (스케줄 조건부). 복구 시 bootstrap 모드 전환 |
| `cloudnative-pg/backup.yaml` | `ScheduledBackup` — `backup.schedule` 이 빈 문자열이면 생성 안 함 |
| `cloudnative-pg/configmap.yaml` · `service.yaml` | PG 접속 정보 노출 |
| `cloudnative-pg/postgresql.yaml` | **`handbook-postgresql` ConfigMap** — `classpath:postgresql.yaml` fragment. r2dbc URL/credential 을 env(`DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USERNAME`/`DB_PASSWORD`) placeholder 로 노출 |
| `kafka/cluster.yaml` · `node-pool.yaml` · `topics.yaml` | **Strimzi KRaft Kafka**. `Kafka` + `KafkaNodePool` (combined controller+broker, ambient mesh 제외 라벨) + `KafkaTopic` (handbook-events / handbook-events-dlq) |
| `kafka/kafka.yaml` | **`handbook-kafka` ConfigMap** — `classpath:kafka.yaml` fragment. `spring.kafka.bootstrap-servers: ${KAFKA_BROKERS}` |
| `authentication/authentication.yaml` | **`handbook-authentication` ConfigMap** — `classpath:authentication.yaml` fragment. JWT 검증 설정. `JWT_SECRET` env 와 짝 |
| `s3/bucket.yaml` | Ceph `ObjectBucketClaim` — `bucket.maxSize` 만큼 할당 |
| `s3/service-entry.yaml` + `virtual-service.yaml` | Istio `ServiceEntry` (외부 RGW) + `VirtualService` (메시 내부 라우팅) — S3 호출을 Istio 게이트웨이로 투명 프록시 |
| `gateway/gateway.yaml` | **`gateway.networking.k8s.io/v1/Gateway`** — `gatewayClassName: istio`, listener hostname 은 `.Values.host`. Istio 컨트롤러가 감지해 `handbook-istio` Deployment + Service(ClusterIP) 를 자동 프로비저닝 |
| `gateway/route.yaml` | OpenShift `Route` — 자동 생성된 `handbook-istio` Service 를 `.Values.host` 로 TLS edge 노출 (`insecureEdgeTerminationPolicy: Redirect`) |
| `gateway/http-route.yaml` | **catch-all** HTTPRoute (`name: gateway`) — 모든 `/` 요청을 `service-gateway:8080` (Spring Cloud Gateway) 로 포워딩. 서비스별 HTTPRoute 가 구체 매칭으로 먼저 가로챈 나머지 경로를 처리 |
| `observability/configmap.yaml` | **`observability` ConfigMap** — `classpath:observability.yaml` fragment. Actuator/Prometheus exposure, health probes, metrics tags, console 로깅 패턴(correlationId 포함) 등 모든 서비스 공통 management/logging 설정 |
| `observability/pod-monitor.yaml` | `PodMonitor` — Istio 사이드카 메트릭 스크래이프 |

## 외부 시크릿 일람 (Git 에 저장하지 않음)
| 이름 | 네임스페이스 | 키 | 용도 | 생성 예시 |
|------|--------------|-----|------|-----------|
| `handbook-jwt` | 각 서비스 네임스페이스 | `jwt-secret` | JWT 서명·검증 PEM 키 | `charts/handbook/README.md` |
| `github-secret` | `github-actions-runner` | `github_app_*` | Actions Runner GitHub App 인증 | `charts/handbook-operator/github-actions-runner-set/README.md` |
| S3/백업 자격증명 | 각 서비스 네임스페이스 | 차트별 | bucket, PG 백업 | infrastructure 차트가 참조 |

## 공통 라이브러리 차트 (`charts/handbook-lib/`)

`type: library` Helm 차트로, 서비스 서브차트들이 공통으로 쓰는 named template 을 보관한다. 각 서브차트 `Chart.yaml` 이 `file://../../handbook-lib` 로 dependency 등록하고 `helm dependency build` 로 `Chart.lock` + `charts/handbook-lib-*.tgz` 를 생성한다 (lock/tgz 모두 git 에 커밋되어 ArgoCD 가 그대로 사용).

| named template | 소비 차트 | 역할 |
|----------------|-----------|------|
| `handbook.jvm-backend.deployment` | gateway, event-broadcaster, login, persist-workspace | Spring Boot JVM Deployment 공통 템플릿. `values.jvmBackend.{deploymentName, fragments, extraEnv}` 로 서비스 차이 흡수 |
| `handbook.frontend-sync-job` | app, shell-ui, login-ui, workspace-ui | 정적 자산 ArgoCD Sync Hook Job. tag prefix / job 이름 / label 은 `.Chart.Name` 에서 자동 유도 |

서브차트 `templates/deployment.yaml` 또는 `templates/sync-job.yaml` 은 한 줄 include 로 끝나고, 실제 manifest 는 모두 라이브러리가 소유. 추가 공통 패턴이 생기면 `handbook-lib/templates/_*.yaml` 에 named template 으로 추가.

## GitHub Actions — reusable workflow

`.github/workflows/_jvm-deploy.yaml` 과 `_frontend-deploy.yaml` 두 개의 reusable workflow (workflow_call) 가 실제 빌드/배포 로직을 소유한다. 각 모듈별 `<module>-deploy.yaml` 은 `paths` 트리거와 `uses:` + `submodule:` 입력만 남긴 ~12줄의 얇은 래퍼:

```yaml
name: 게이트웨이 모듈 배포
on:
  push:
    paths:
      - gateway/**
  workflow_dispatch:
jobs:
  deploy:
    uses: ./.github/workflows/_jvm-deploy.yaml
    with:
      submodule: gateway
    secrets: inherit
```

JDK 버전 / BASE_IMAGE / 러너 라벨 변경은 `_jvm-deploy.yaml` 또는 `_frontend-deploy.yaml` 한 곳만 수정하면 전체 모듈에 반영. `secrets: inherit` 로 `_GITHUB_TOKEN` / `GITHUB_TOKEN` 이 reusable 쪽에 자동 전달.

## 새 정적 자산 모듈을 추가할 때 (GWT 프론트엔드)
1. `charts/handbook/<module>/` 디렉토리 생성. `Chart.yaml` 에 `handbook-lib` dependency 등록. `values.yaml` 과 `templates/{warehouse,stage,http-route}.yaml` 만 작성. `templates/sync-job.yaml` 은 `{{ include "handbook.frontend-sync-job" . }}` 한 줄로 끝.
2. Warehouse `allowTags` 패턴(`^<module>-`), Stage 의 `argocd-update` 가 가리키는 Application 이름 등을 새 모듈 이름으로 치환.
3. `helm dependency build` 로 `Chart.lock` + `charts/handbook-lib-*.tgz` 생성 후 커밋.
4. `.github/workflows/<module>-deploy.yaml` 은 `_frontend-deploy.yaml` 을 `uses:` 하는 12줄 래퍼로 작성.
5. `charts/handbook/values.yaml` 의 `services:` 배열에 `{name, kind: frontend, color, stages: [dev, staging, prod]}` 추가. ApplicationSet 이 자동으로 Application 을 만든다.
6. **promote 워크플로는 필요 없다** — Kargo 가 `argocd-update` 로 chart 의 helm parameter(`freight.commit`, `bucket`)를 갱신하면 sync-job 이 ArgoCD Sync Hook 으로 매 freight 마다 새 Job 으로 실행되어 release 다운로드 + S3 sync 를 수행한다.
7. **정적 자산 모듈은 ConfigMap fragment / 서비스 ConfigMap 패턴이 적용되지 않는다** — Spring Boot 가 아니므로 `application.yml` 자체가 없음.

## 새 JVM 서비스를 추가할 때
1. `charts/handbook/<service>/` 디렉토리 생성. `Chart.yaml` 에 `handbook-lib` dependency 등록. gateway/login 템플릿을 참고해 `templates/{configmap,service,stage,warehouse}.yaml` 과 `values.yaml` 만 작성. `templates/deployment.yaml` 은 `{{ include "handbook.jvm-backend.deployment" . }}` 한 줄로 끝.
2. `values.yaml` 의 `jvmBackend` 섹션을 채운다:
   - `deploymentName`: metadata.name 오버라이드 (기본 `.Chart.Name`)
   - `fragments`: 필요한 fragment 이름 리스트 — `observability`, `authentication`, `postgresql`, `kafka` 중 선택
   - `extraEnv`: 서비스별 추가 env (tpl 평가되므로 `https://{{ .Values.host }}/` 같은 표현식 사용 가능)
3. **ConfigMap 의 `application.yml` 키에 운영 설정을 다 적는다**. jar 의 `src/main/resources/application.yml` 에 있는 값(application.name, routes, cloud.stream bindings, kafka producer, server.port, cors 등)을 그대로 옮겨 적는다. **머지가 아닌 파일 단위 overwrite** 이므로 jar 와 중복돼도 OK. 공통 단편은 `spring.config.import: [classpath:observability.yaml, classpath:authentication.yaml, ...]` 로 fragments 리스트와 일치하게 가져온다.
4. `charts/handbook/values.yaml` 의 `services:` 배열에 `{name, kind: backend, color, stages:[...]}` 추가.
5. `helm dependency build` 로 Chart.lock/tgz 생성 후 커밋.
6. `.github/workflows/<service>-deploy.yaml` 은 `_jvm-deploy.yaml` 을 `uses:` 하는 12줄 래퍼로 작성.
7. 로컬 IDE 에서 jar 를 직접 실행할 때는 jar 의 `application.yml` 이 default 로 로드된다 — 별도 profile 활성화 불필요.
8. 새 서비스의 gradle 모듈이 다른 서비스 모듈을 `implementation(project(":x"))` 로 참조하지 않는지 확인. 참조 시 그쪽의 `@Configuration` 이 딸려 올라와 Bean 충돌이 난다 (GEMINI.md 디버깅 표 참조).

## 운영 명령 치트시트
```bash
# operator 재배포 (= GitOps 체인 재개)
helm upgrade --install handbook-operator charts/handbook-operator -n handbook-operator

# ArgoCD 싱크 상태
argocd app list -l app.kubernetes.io/part-of=handbook

# Kargo freight / stage 확인
kubectl get freight,stage,warehouse -A

# CloudNativePG 상태
kubectl cnpg status <cluster-name> -n <ns>

# 시크릿 생성 (JWT)
# charts/handbook/README.md 참조
```
