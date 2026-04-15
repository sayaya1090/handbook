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
| `templates/actions-runner.yaml` | OpenShift `BuildConfig` + `ImageStream` — UBI10 기반 runner 이미지를 클러스터에서 직접 빌드 (`.claude/skills/dev-environment.md` GitHub Actions Runner 섹션 참조) |
| `templates/cache.yaml` | `.Values.ubi10.template.spec.volumes` 에서 cache PVC 이름을 읽어 10Gi PVC 생성. `_work`, `.gradle`, `gradle-installations/installs` 캐시 공유 |
| `templates/docker-daemon-config.yaml` | dind 사이드카용 `daemon.json` ConfigMap — `storage-driver: fuse-overlayfs`, `mtu: 1350` |
| `templates/job.yaml`, `rbac.yaml` | Runner 네임스페이스 초기화 Job · RBAC |

### 외부 의존
- **`github-secret`**: GitHub App 자격증명 (`github_app_id`, `github_app_installation_id`, `github_app_private_key`). 생성 예시는 `charts/handbook-operator/github-actions-runner-set/README.md`.

## `charts/handbook/`

### 값 스키마 (`values.yaml`)
- **`stages`**: `dev | staging | prod` 각각에 `database`(PG IP), `host`(FQDN), `color`, `backup.schedule`(cron; 빈 문자열이면 백업 비활성), `bucket.maxSize`(S3 PVC 크기)
- **`services`**: `[{name, stages: [...]}]` — 어떤 서비스를 어떤 스테이지에 배포할지 매트릭스. 현재 `gateway` 는 3 스테이지, `event-broadcaster` 는 dev/prod 만.

### 루트 템플릿
| 파일 | 역할 |
|------|------|
| `templates/application-set.yaml` | ArgoCD `ApplicationSet` — `stages × services` 곱을 matrix generator 로 전개해 Application 을 자동 생성 |
| `templates/image-stream.yaml` | 서비스별 `ImageStream` (OpenShift 내부 레지스트리 태그 추적 대상) |
| `templates/project-config.yaml` | ArgoCD `AppProject` 설정 |
| `templates/job.yaml`, `rbac.yaml` | 초기화 Job · RBAC |

### 서비스 서브차트 (`gateway/`, `event-broadcaster/`)
동일 패턴. 신규 서비스 추가 시 이 구조를 복사한다.

| 파일 | 역할 |
|------|------|
| `templates/configmap.yaml` | **jar 의 `application.yml` 을 파일 단위로 대체하는 운영 설정**. Deployment 가 이 ConfigMap 의 `application.yml` 키를 `/app/resources/application.yml` 에 subPath 로 마운트하여 jar 의 동명 파일을 덮어쓴다. **머지(SPRING_CONFIG_ADDITIONAL_LOCATION)는 사용하지 않는다** — 운영 환경에서 필요한 모든 설정(application.name, routes, cloud.stream bindings, kafka producer, server.port, cors 등)을 jar 와 중복되더라도 이 파일 안에 다 넣는다. jar 의 application.yml 은 로컬 IDE 실행용 default 로만 사용. 공통 단편(observability, postgresql, kafka, authentication)은 `spring.config.import: [classpath:observability.yaml, ...]` 로 fragment ConfigMap 에서 가져온다 |
| `templates/deployment.yaml` | Deployment. **공통 환경변수**: `TZ=Asia/Seoul`, `JWT_SECRET` (secret `handbook-jwt`). `SPRING_CONFIG_ADDITIONAL_LOCATION` 을 절대 사용하지 않는다 — 머지 우선순위가 모호해진다. 서비스별 추가 env 는 placeholder(`${KAFKA_BROKERS}` 등)를 채우는 값만. **볼륨 마운트**: 서비스 ConfigMap 을 `/app/resources/application.yml` 에 subPath 마운트(jar classpath 파일 덮어쓰기). 공통 fragment ConfigMap(observability / handbook-authentication / handbook-kafka 등)은 `/app/resources/<name>.yaml` 에 subPath 마운트하여 `classpath:<name>.yaml` 로 import 되게 한다. **Reloader**: 마운트한 모든 ConfigMap 이름을 `configmap.reloader.stakater.com/reload` 에 나열. **프로브**: `/actuator/health/{liveness,readiness}` |
| `templates/service.yaml` | ClusterIP Service (포트 8080) |
| `templates/stage.yaml` | **Kargo `Stage`** — 서비스 × 스테이지 promotion 파이프라인 |
| `templates/warehouse.yaml` | **Kargo `Warehouse`** — dev 스테이지의 첫 서비스에만 생성, ImageStream 을 1분 간격으로 감시해 새 빌드 Freight 생성 |

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

### Kargo Promotion 파이프라인
서비스 종류에 따라 두 가지 형태가 공존한다.

**(A) JVM 백엔드 — 컨테이너 이미지 기반** (gateway, event-broadcaster, persist-\*, search-\*, login, assistant)
- **Warehouse**: OpenShift ImageStream 변화를 감지 → Freight 발행 (image digest)
- **Stage (dev/staging)**: Warehouse 구독 → ArgoCD Update task 로 `image.tag` 업데이트해 환경에 자동 배포
- **Stage (prod)**: 이전 스테이지를 upstream 으로 두고, promotion 시 GitHub `repository_dispatch`(`release` 이벤트) 호출로 CI/CD 파이프라인 트리거

**(B) GWT 프론트엔드 정적 자산 — GitHub Release + ArgoCD Hook Job 기반** (shell-ui, …)
- **Build**: GHA(`<module>-deploy.yaml`) 가 `:<module>:build` → WAR 에서 정적 자산 추출 → tar 묶음 → `gh release create <module>-<sha> --prerelease` 로 GitHub prerelease + asset 업로드. GHA 는 빌드 + publish 까지만, deploy 액션 0번
- **Warehouse**: git 구독, `commitSelectionStrategy: Lexical` + `includeTags: ^<module>-` + `strictSemvers: false` → 새 prerelease tag 마다 Freight 발행. **`NewestTag` 는 Kargo 의 유효한 enum 이 아니어서** 설정 시 기본 `NewestFromBranch` 로 fallback 되고 tag 가 무시되는 함정이 있으니 `Lexical` 을 쓴다. ⚠️ **Lexical 은 문자열 사전순 정렬이라 sha 기반 tag(`shell-ui-<7자 sha>`) 에선 "알파벳 상 가장 큰 것" = 반드시 최신이 아님**. 워크플로가 publish 시 이전 release/tag 를 정리하거나, 정렬 가능한 prefix(타임스탬프 등)를 tag 에 붙이는 형태로 운영해야 한다
- **Stage**: `vars.bucket: handbook-{dev,staging,prod}` 선언. promotion step 은 `argocd-update` 로 ArgoCD Application 의 helm parameter 를 갱신한다. ⚠️ **Kargo `argocd-update` step 의 `helm` 블록은 스키마상 `parameters` 를 허용하지 않고 `images` 배열만 받는다** — 이름은 image 용이지만 `key` 에 임의 helm parameter path 를 쓸 수 있어 `freight.commit` / `bucket` 같은 값 주입에 재활용한다. 표현식으로 commit SHA 를 얻을 때는 `${{ commitFrom("...").ID }}` — Kargo 표현식 엔진이 Go struct 필드명(대문자 포함)을 그대로 노출하므로 `.id`/`.tag` 는 `has no field` 에러를 낸다
- **Sync Job (chart 내장)**: ArgoCD reconcile 시 `templates/sync-job.yaml` 이 새 freight commit 마다 새 Job 을 만든다 (`argocd.argoproj.io/hook: Sync` + `hook-delete-policy: BeforeHookCreation`). Job 컨테이너(`amazon/aws-cli`)가 GitHub release asset 을 **unauthenticated** (public repo) 로 `curl -fsSL "https://github.com/.../releases/download/<tag>/<asset>"` 로 받아 `aws s3 sync s3://${bucket}/static/` 수행 후 종료. `ttlSecondsAfterFinished` 로 자동 정리
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

## 새 정적 자산 모듈을 추가할 때 (GWT 프론트엔드)
1. `charts/handbook/<module>/` 디렉토리 생성. shell-ui 템플릿(`Chart.yaml`, `values.yaml`, `templates/{warehouse,stage,sync-job}.yaml`)을 복사. Deployment/Service/configmap 은 없다.
2. Warehouse `includeTags` 패턴(`^<module>-`), Stage 의 `argocd-update` 가 가리키는 Application 이름, Job 이름 prefix 를 새 모듈 이름으로 치환.
3. `.github/workflows/<module>-deploy.yaml`: `:<module>:build` → 정적 자산 unpack/sed/tar → `gh release create <module>-<short-sha> --prerelease`.
4. `charts/handbook/values.yaml` 의 `services:` 배열에 `{name, stages: [dev, staging, prod]}` 추가. ApplicationSet 이 자동으로 Application 을 만든다.
5. **promote 워크플로는 필요 없다** — Kargo 가 `argocd-update` 로 chart 의 helm parameter(`freight.tag`, `bucket`)를 갱신하면 chart 안 `sync-job.yaml` 이 ArgoCD Sync Hook 으로 매 freight 마다 새 Job 으로 실행되어 release 다운로드 + S3 sync 를 수행한다.
6. **정적 자산 모듈은 ConfigMap fragment / 서비스 ConfigMap 패턴이 적용되지 않는다** — Spring Boot 가 아니므로 `application.yml` 자체가 없음.

## 새 JVM 서비스를 추가할 때
1. `charts/handbook/<service>/` 디렉토리 생성. gateway 또는 event-broadcaster 템플릿을 복사 → `configmap`, `deployment`, `service`, `stage`, `warehouse`, `Chart.yaml`, `values.yaml` 수정.
2. `charts/handbook/values.yaml` 의 `services:` 배열에 `{name, stages:[...]}` 추가. ApplicationSet 이 자동으로 Application 을 생성한다.
3. **ConfigMap 의 `application.yml` 키에 운영 설정을 다 적는다**. jar 의 `src/main/resources/application.yml` 에 있는 값(application.name, routes, cloud.stream bindings, kafka producer, server.port, cors 등)을 그대로 옮겨 적는다. **머지가 아닌 파일 단위 overwrite** 이므로 jar 와 중복돼도 OK. 공통 단편은 `spring.config.import: [classpath:observability.yaml, classpath:authentication.yaml, classpath:kafka.yaml, classpath:postgresql.yaml]` 로 필요한 것만 가져온다.
4. `deployment.yaml`:
   - **env 는 placeholder 채우는 값만**. `JWT_SECRET` (Secret), `KAFKA_BROKERS` 등. **`SPRING_CONFIG_ADDITIONAL_LOCATION` 절대 금지** — 머지 우선순위가 모호해진다.
   - 서비스 ConfigMap 을 `/app/resources/application.yml` 에 subPath 마운트 (jar 의 동명 파일을 file-level overwrite).
   - 사용하는 fragment ConfigMap(observability + 필요한 것만)을 각각 `/app/resources/<name>.yaml` 에 subPath 마운트.
5. reloader 애노테이션 `configmap.reloader.stakater.com/reload` 에 마운트한 모든 ConfigMap 이름을 나열 (`<service>,observability,handbook-authentication,handbook-kafka` 식).
6. 로컬 IDE 에서 jar 를 직접 실행할 때는 jar 의 `application.yml` 이 default 로 로드된다 — 별도 profile 활성화 불필요.
7. 새 서비스의 gradle 모듈이 다른 서비스 모듈을 `implementation(project(":x"))` 로 참조하지 않는지 확인. 참조 시 그쪽의 `@Configuration` 이 딸려 올라와 Bean 충돌이 난다 (CLAUDE.md 디버깅 표 참조).

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
