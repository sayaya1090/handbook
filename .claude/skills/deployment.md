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
| `templates/configmap.yaml` | **jar 내부 `src/main/resources/application.yml` 에 merge 되는 override 파일**. routes/kafka 같은 "코드가 요구하는 구성"은 jar 에 둔 채, 이 ConfigMap 에는 클러스터에 종속적인 값(`spring.security.authentication.header/jwt-secret`, `spring.config.import`, `management.endpoint.health.probes.enabled`, gateway 의 `services:` 리스트 등)만 넣는다. `application.name` / `server.port` 같이 jar 에 이미 정의된 값은 중복하지 않는다 |
| `templates/deployment.yaml` | Deployment. **공통 환경변수**: `TZ=Asia/Seoul`, `JWT_SECRET` (secret `handbook-jwt` 의 `jwt-secret`), `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/config/application.yaml` (jar 기본값 위에 ConfigMap override 를 merge). 서비스별 추가 env: gateway 는 `LOGIN_URI`/`EVENT_BROADCASTER_URI`/… + `CORS_ALLOWED_ORIGINS`, event-broadcaster 는 `KAFKA_BROKERS`. **볼륨 마운트**: ConfigMap 을 `/app/config/application.yaml` (NOT `/app/resources`) 에 subPath 마운트 — jar classpath 의 application.yml 을 덮지 않게 분리. observability ConfigMap 은 `/app/resources/observability.yaml` 에 마운트해 `classpath:observability.yaml` 로 import 된다. **Reloader**: `configmap.reloader.stakater.com/reload: "<service>,observability"`. **프로브**: `/actuator/health/{liveness,readiness}` |
| `templates/service.yaml` | ClusterIP Service (포트 8080) |
| `templates/stage.yaml` | **Kargo `Stage`** — 서비스 × 스테이지 promotion 파이프라인 |
| `templates/warehouse.yaml` | **Kargo `Warehouse`** — dev 스테이지의 첫 서비스에만 생성, ImageStream 을 1분 간격으로 감시해 새 빌드 Freight 생성 |

### Kargo Promotion 파이프라인
- **Warehouse**: OpenShift ImageStream 변화를 감지 → Freight 발행
- **Stage (dev)**: Warehouse 구독 → ArgoCD Update task 로 `image.tag` 업데이트해 dev 환경에 자동 배포
- **Stage (staging/prod)**: 이전 스테이지를 upstream 으로 두고, promotion 시 GitHub repository dispatch API 호출(`release` 이벤트)로 CI/CD 파이프라인 트리거
- 즉, dev 는 자동 전진, staging/prod 는 수동 (또는 승인) promotion. 승격 단위는 Freight = 이미지 태그 스냅샷.

### 서브차트 `infrastructure/`
| 파일 | 역할 |
|------|------|
| `cloudnative-pg/cluster.yaml` | CloudNativePG `Cluster` — 3 인스턴스 PostgreSQL. `values.stages.<stage>.database` 가 비어 있지 않으면 외부 PG IP 로 프록시, 비어 있으면 자체 운영. Barman S3 백업 설정 (스케줄 조건부). 복구 시 bootstrap 모드 전환 |
| `cloudnative-pg/backup.yaml` | `ScheduledBackup` — `backup.schedule` 이 빈 문자열이면 생성 안 함 |
| `cloudnative-pg/configmap.yaml` · `service.yaml` | PG 접속 정보 노출 (`postgresql-ro.yaml` 등 — 서비스 configmap 의 `spring.config.import` 타깃) |
| `s3/bucket.yaml` | Ceph `ObjectBucketClaim` — `bucket.maxSize` 만큼 할당 |
| `s3/service-entry.yaml` + `virtual-service.yaml` | Istio `ServiceEntry` (외부 RGW) + `VirtualService` (메시 내부 라우팅) — S3 호출을 Istio 게이트웨이로 투명 프록시 |
| `observability/configmap.yaml` | Spring Boot Actuator / Prometheus exposure 공통 설정 (`spring.config.import: classpath:observability.yaml` 로 각 서비스가 참조) |
| `observability/pod-monitor.yaml` | `PodMonitor` — Istio 사이드카 메트릭 스크래이프 |

## 외부 시크릿 일람 (Git 에 저장하지 않음)
| 이름 | 네임스페이스 | 키 | 용도 | 생성 예시 |
|------|--------------|-----|------|-----------|
| `handbook-jwt` | 각 서비스 네임스페이스 | `jwt-secret` | JWT 서명·검증 PEM 키 | `charts/handbook/README.md` |
| `github-secret` | `github-actions-runner` | `github_app_*` | Actions Runner GitHub App 인증 | `charts/handbook-operator/github-actions-runner-set/README.md` |
| S3/백업 자격증명 | 각 서비스 네임스페이스 | 차트별 | bucket, PG 백업 | infrastructure 차트가 참조 |

## 새 서비스를 추가할 때
1. `charts/handbook/<service>/` 디렉토리 생성. gateway 또는 event-broadcaster 템플릿을 복사 → `configmap`, `deployment`, `service`, `stage`, `warehouse`, `Chart.yaml`, `values.yaml` 수정.
2. `charts/handbook/values.yaml` 의 `services:` 배열에 `{name, stages:[...]}` 추가. ApplicationSet 이 자동으로 Application 을 생성한다.
3. **ConfigMap 은 override 전용**으로 유지. `configmap.yaml` 의 `application.yaml` 에는 `spring.security.authentication.jwt-secret: ${JWT_SECRET}` + `spring.config.import: [classpath:observability.yaml]` + `management.endpoint.health.probes.enabled: true` + 서비스 고유 override 만 넣는다. **jar 가 이미 가지고 있는 값(routes, kafka brokers, application.name, server.port 등)을 복사하지 말 것** — additional-location 으로 자동 merge 된다.
4. `deployment.yaml` 의 env 에 `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/config/application.yaml` 와 `JWT_SECRET` 을 넣고, 서비스가 요구하는 외부 URL/브로커(예: `KAFKA_BROKERS`, `*_URI`, `CORS_ALLOWED_ORIGINS`) 를 **env 로만** 주입한다. `KAFKA_BROKERS` 같은 값을 ConfigMap 에 하드코딩하지 말 것.
5. **볼륨 마운트 경로는 `/app/config/application.yaml`** (NOT `/app/resources/application.yaml`). 후자는 jar classpath 와 겹쳐 jar 내부 application.yml 을 덮어버리므로 routes/kafka 설정이 사라진다. 과거 해당 실수로 gateway routes 전체가 사라졌던 이력 있음.
6. reloader 애노테이션에 새 ConfigMap 이름 + `observability` 를 포함시킨다. `postgresql-ro` 같이 실제로 import 하지 않는 이름은 넣지 말 것.
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
