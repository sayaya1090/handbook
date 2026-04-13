# 배포 전략 v2 — Preview + Kargo + Canary (검토용)

> 상태: **제안 / 검토 대기**. v1 (과설계) 대체. 사용자 확정 사항: 단일 클러스터, 3 환경(dev/staging/prod), 태그 전략 TBD, AnalysisTemplate 신규 작성 필요, 옵션 D(Preview + Canary) 방향.
>
> **주의**: 옵션 D 원본은 staging 삭제 포함이었으나, 이 문서는 "3 환경 유지 + Preview 추가 + Prod Canary" 로 각색했다. staging 삭제 버전이 맞으면 §2, §4, §7.2 를 제거하면 됨.

## 1. 큰 그림

세 겹의 도구가 각자의 역할만 담당:

| 층위 | 도구 | 역할 |
|------|------|------|
| Stage 승격 | **Kargo** | dev → staging → prod 간 Freight 이동. 승격 기록·롤백 관리 |
| Preview | **ArgoCD ApplicationSet (PR generator)** | PR 별 ephemeral 환경 자동 생성/삭제 |
| Prod 안전장치 | **Argo Rollouts + Istio** | prod 배포를 canary 로 점진 확장, SLO 위반 시 자동 롤백 |

Kargo 는 stage 승격에만 집중 — preview 와 canary 는 Kargo 바깥. 각 도구가 잘하는 일만 시키는 게 핵심.

## 2. 네임스페이스 레이아웃 (단일 클러스터)

| 네임스페이스 | 용도 | 비고 |
|--------------|------|------|
| `handbook-dev` | dev 환경 | Kargo 관리 |
| `handbook-staging` | staging 환경 | Kargo 관리 |
| `handbook-prod` | prod 환경 | Kargo + Argo Rollouts |
| `handbook-preview-pr-<N>` | PR 별 ephemeral | ApplicationSet 자동 생성·삭제 |
| `kargo` | Kargo controller | 신규 설치 |
| `argocd` | ArgoCD | 기존 |
| `argo-rollouts` | Argo Rollouts controller | 신규 설치 |
| `github-actions-runner` | CI runner | 기존 |

**격리:**
- **NetworkPolicy**: 각 env 네임스페이스는 기본 `deny-all` ingress. `istio-system`, `kargo`, `argocd` 에서만 ingress 허용. env 간 cross-namespace 트래픽 차단.
- **ResourceQuota**: dev(`cpu=4, memory=8Gi`), staging(`cpu=8, memory=16Gi`), prod(`cpu=16, memory=32Gi`), preview(`cpu=2, memory=4Gi`). 실제 사용량 측정 후 조정.
- **LimitRange**: 각 env 에 기본 컨테이너 request/limit 고정.
- **Observability**: Prometheus relabel 로 `namespace` → `env` 레이블 변환. 대시보드/알람이 env 축으로 필터 가능.

## 3. 태그 전략 (제안)

**원칙**: 운영 배포 아티팩트는 **불변 태그만** 소비. mutable 태그 (`latest`, `main`) 는 디버그 편의용일 뿐 어떤 배포 경로도 참조하지 않는다.

| 용도 | 태그 형식 | 생성 주체 | 소비 주체 |
|------|-----------|-----------|-----------|
| 주 배포 아티팩트 | `sha-<8char>` | CI (main push) | Kargo Warehouse |
| PR preview | `pr-<num>-<8char>` | CI (PR open/sync) | ApplicationSet |
| 릴리즈 스냅샷(선택) | `v<major>.<minor>.<patch>` | main 태그 push | 수동 승격·보존 |
| 디버그 | `main` (mutable) | CI main push 마다 덮어씀 | 로컬 실험만, 배포 금지 |

Warehouse 구독 조건:
```yaml
subscriptions:
  - image:
      repoURL: image-registry.openshift-image-registry.svc:5000/handbook/gateway
      allowTags: ^sha-[a-f0-9]{8}$
      imageSelectionStrategy: NewestBuild
      strictSemvers: false
      insecureSkipTLSVerify: true
      discoveryLimit: 5
```

**Retention**: OpenShift ImageStream 은 `spec.tags[].referencePolicy` 기본값으로 20 태그 보존. 초과 시 오래된 sha 폐기. 릴리즈 스냅샷(`v*`) 은 별도 ImageStream 또는 보존 CronJob 으로 분리 관리.

**CI 변경점** (GitHub Actions deploy 워크플로):
```yaml
# 기존: jib 에 latest 태그
# 변경: jib 에 sha + latest (latest 는 편의, 소비되지 않음)
- name: Deploy
  run: |
    SHA_TAG=sha-${GITHUB_SHA:0:8}
    ./gradlew $SUBMODULE:jib \
      -Djib.to.tags=$SHA_TAG,latest \
      -Djib.to.image=$IMAGE
```

PR 워크플로는 별도 추가:
```yaml
- name: Build PR preview image
  if: github.event_name == 'pull_request'
  run: |
    PR_TAG=pr-${{ github.event.pull_request.number }}-${GITHUB_SHA:0:8}
    ./gradlew $SUBMODULE:jib -Djib.to.tags=$PR_TAG -Djib.to.image=$IMAGE
```

## 4. Kargo Stage 승격 파이프라인

### 4.1 Warehouse (서비스당 1개)
§3 의 태그 필터 그대로. `freightCreationPolicy: Automatic`, `interval: 1m`.

### 4.2 Stage: dev (auto)
```yaml
apiVersion: kargo.akuity.io/v1alpha1
kind: Stage
metadata: {name: gateway-dev, namespace: kargo}
spec:
  requestedFreight:
    - origin: {kind: Warehouse, name: gateway}
      sources: {direct: true}
  promotionTemplate:
    spec:
      steps:
        - uses: argocd-update
          config:
            apps:
              - name: handbook-gateway-dev
                sources:
                  - repoURL: https://github.com/sayaya1090/handbook.git
                    helm:
                      images:
                        - key: image.tag
                          value: ${{ imageFrom("image-registry.openshift-image-registry.svc:5000/handbook/gateway").tag }}
  verification:
    analysisTemplates:
      - name: gateway-smoke
```

Project spec 에 AutoPromotionPolicy:
```yaml
promotionPolicies:
  - stageSelector: {name: gateway-dev}
    autoPromotionEnabled: true
```

### 4.3 Stage: staging (auto 검증 통과 시)
- `requestedFreight.sources.stages: [gateway-dev]` 로 dev 를 upstream.
- 동일 `argocd-update` 스텝, target = `handbook-gateway-staging`.
- `verification.analysisTemplates: [gateway-staging]`.
- PromotionPolicy 에서 `autoPromotionEnabled: true`. 단 verification 실패 시 Kargo 가 자동으로 승격 차단.

### 4.4 Stage: prod (manual + canary)
- upstream: `gateway-staging`.
- `argocd-update` 가 가리키는 Application 은 prod 용이며 리소스가 **Rollout**. image 태그를 갱신하면 Argo Rollouts 가 canary 진행.
- `verification.analysisTemplates: [gateway-canary]` — Rollouts 내부 AnalysisRun 과 별개로 Kargo 가 "승격 직후 15분 안정성" 을 한 번 더 체크.
- PromotionPolicy `autoPromotionEnabled: false`. Kargo UI 에서 승인자가 버튼 클릭.

## 5. Preview Environments — ApplicationSet PR Generator

```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata: {name: handbook-previews, namespace: argocd}
spec:
  generators:
    - pullRequest:
        github:
          owner: sayaya1090
          repo: handbook
          tokenRef: {secretName: github-token, key: token}
          labels: ["preview"]
        requeueAfterSeconds: 180
  template:
    metadata:
      name: 'preview-pr-{{.number}}'
      finalizers: ["resources-finalizer.argocd.argoproj.io"]
    spec:
      project: handbook
      source:
        repoURL: https://github.com/sayaya1090/handbook.git
        targetRevision: '{{.head_sha}}'
        path: charts/handbook/gateway
        helm:
          values: |
            image:
              tag: pr-{{.number}}-{{.head_sha_short}}
            host: pr-{{.number}}.preview.handbook.sayaya.cloud
            replicaCount: 1
            resources:
              limits: {cpu: 500m, memory: 512Mi}
              requests: {cpu: 100m, memory: 256Mi}
      destination:
        server: https://kubernetes.default.svc
        namespace: 'handbook-preview-pr-{{.number}}'
      syncPolicy:
        automated: {prune: true, selfHeal: true}
        syncOptions: [CreateNamespace=true]
```

**라이프사이클:**
1. PR 에 `preview` 레이블 부착 → ApplicationSet 이 감지 → Application + namespace 생성.
2. PR 에 커밋 푸시 → CI 가 `pr-<num>-<sha>` 태그 이미지 빌드 → ApplicationSet 이 `targetRevision`, `image.tag` 갱신 → ArgoCD 가 rolling update.
3. PR 닫힘/머지 → 레이블 조건 이탈 → Application 제거 → finalizer 가 namespace 자원 정리 → namespace 삭제.

**주의점:**
- Preview 는 infrastructure(PG, Kafka) 공유가 곤란하므로 **별도 SQLite/embedded Kafka** 또는 **dev 네임스페이스 리소스 공유** 중 하나를 선택해야 함. 기본 권장: dev 네임스페이스 PG 의 PR 별 schema 분리 (`pr_<num>`). Kafka 는 topic 접두어 (`pr-<num>-`).
- Preview 도메인 wildcard 인증서 (`*.preview.handbook.sayaya.cloud`) 필요.
- Preview 는 Kargo 와 **무관**. Kargo Warehouse 의 태그 필터가 `sha-*` 로 고정되어 있어 `pr-*` 태그는 수집되지 않음. 의도된 분리.

## 6. Prod Canary — Argo Rollouts + Istio

### 6.1 Deployment → Rollout 교체
`charts/handbook/gateway/templates/deployment.yaml` 를 prod 에서만 `Rollout` 으로 렌더:

```yaml
{{- if eq .Values.stage.name "prod" }}
apiVersion: argoproj.io/v1alpha1
kind: Rollout
{{- else }}
kind: Deployment
apiVersion: apps/v1
{{- end }}
metadata: {...}
spec:
  replicas: 3
  selector: {matchLabels: {app: gateway}}
  template:
    # 기존 podTemplate 그대로
  {{- if eq .Values.stage.name "prod" }}
  strategy:
    canary:
      canaryService: service-gateway-canary
      stableService: service-gateway
      trafficRouting:
        istio:
          virtualService:
            name: gateway
            routes: [primary]
      steps:
        - setWeight: 10
        - pause: {duration: 2m}
        - analysis:
            templates: [{templateName: gateway-canary}]
        - setWeight: 30
        - pause: {duration: 3m}
        - analysis:
            templates: [{templateName: gateway-canary}]
        - setWeight: 60
        - pause: {duration: 3m}
        - setWeight: 100
  {{- else }}
  strategy:
    type: RollingUpdate
    rollingUpdate: {maxSurge: 25%, maxUnavailable: 25%}
  {{- end }}
```

### 6.2 Service 분리
`service-gateway` (stable) 와 `service-gateway-canary` (canary) 두 개 생성. 셀렉터는 Rollouts 이 주입하는 `rollouts-pod-template-hash` 로 자동 분리.

### 6.3 Istio VirtualService
기존 infrastructure 차트의 VirtualService 에 subset 추가:
```yaml
spec:
  http:
    - name: primary
      route:
        - destination:
            host: service-gateway
            subset: stable
          weight: 100
        - destination:
            host: service-gateway
            subset: canary
          weight: 0
```
Rollouts 가 weight 를 동적 변경. 실패 → `setWeight: 0` 자동 롤백.

## 7. AnalysisTemplate 라이브러리

`charts/handbook/infrastructure/templates/analysis/` 아래에 세 템플릿. 서비스당 복제 (이름에 service 프리픽스). 메트릭 이름은 Spring Boot Actuator + Micrometer 기본값 가정.

> **확인 필요**: Spring Boot 4 는 Micrometer Observation 기반이라 메트릭 이름이 `http_server_requests_*` 에서 바뀔 수 있음. 배포 전 실제 `/actuator/prometheus` 출력 확인.

### 7.1 `<svc>-smoke` (dev)
목적: 배포가 "살아 있고 에러 폭발은 없는지" 만 확인. 2 분.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata: {name: gateway-smoke, namespace: handbook-dev}
spec:
  args:
    - name: service
      value: gateway
  metrics:
    - name: readiness-up
      interval: 15s
      count: 8
      successCondition: result[0] == 1
      failureLimit: 2
      provider:
        prometheus:
          address: http://prometheus-k8s.openshift-monitoring:9090
          query: |
            min(up{job=~".*{{args.service}}.*"})
    - name: error-rate
      interval: 30s
      count: 4
      successCondition: result[0] < 0.05
      failureLimit: 1
      provider:
        prometheus:
          address: http://prometheus-k8s.openshift-monitoring:9090
          query: |
            sum(rate(http_server_requests_seconds_count{application="{{args.service}}",status=~"5.."}[2m]))
            /
            clamp_min(sum(rate(http_server_requests_seconds_count{application="{{args.service}}"}[2m])), 0.001)
```

### 7.2 `<svc>-staging` (staging)
목적: 소크 테스트. p95 레이턴시·에러율·서비스별 특화 메트릭을 10 분 관측.

```yaml
spec:
  args:
    - {name: service, value: gateway}
  metrics:
    - name: p95-latency
      interval: 1m
      count: 10
      successCondition: result[0] < 0.5
      provider:
        prometheus:
          query: |
            histogram_quantile(0.95,
              sum(rate(http_server_requests_seconds_bucket{application="{{args.service}}"}[2m])) by (le)
            )
    - name: error-rate
      interval: 1m
      count: 10
      successCondition: result[0] < 0.01
      provider:
        prometheus:
          query: |
            sum(rate(http_server_requests_seconds_count{application="{{args.service}}",status=~"5.."}[2m]))
            /
            clamp_min(sum(rate(http_server_requests_seconds_count{application="{{args.service}}"}[2m])), 0.001)
```

**서비스별 확장:**
- event-broadcaster: Kafka consumer lag 메트릭 추가 — `max(kafka_consumer_records_lag{application="event-broadcaster"}) < 100`.
- persist-*: DB 풀 고갈 감지 — `max(r2dbc_pool_acquired{application="..."} / r2dbc_pool_max) < 0.9`.

### 7.3 `<svc>-canary` (prod canary)
목적: Istio subset 기준 canary vs stable 비교. 2 분 × 3 회 = 6 분.

```yaml
spec:
  args:
    - {name: service, value: gateway}
  metrics:
    - name: canary-error-rate
      interval: 1m
      count: 3
      successCondition: result[0] < 0.01
      failureLimit: 1
      provider:
        prometheus:
          query: |
            sum(rate(istio_requests_total{
              destination_service_name="service-gateway",
              destination_version="canary",
              response_code=~"5.."
            }[2m]))
            /
            clamp_min(sum(rate(istio_requests_total{
              destination_service_name="service-gateway",
              destination_version="canary"
            }[2m])), 0.001)
    - name: canary-vs-stable-latency
      interval: 1m
      count: 3
      successCondition: result[0] < 1.2    # canary p95 가 stable p95 의 120% 미만
      failureLimit: 1
      provider:
        prometheus:
          query: |
            histogram_quantile(0.95,
              sum(rate(istio_request_duration_milliseconds_bucket{destination_service_name="service-gateway",destination_version="canary"}[2m])) by (le)
            )
            /
            clamp_min(
              histogram_quantile(0.95,
                sum(rate(istio_request_duration_milliseconds_bucket{destination_service_name="service-gateway",destination_version="stable"}[2m])) by (le)
              ), 1)
```

**AnalysisTemplate 은 Helm 서브차트 `infrastructure/templates/analysis/` 로 배치**, 서비스별 `service` 인자를 주입. 공용 템플릿 1벌 + env 별 override 가 아니라 **env 별 1벌씩 복제** — 네임스페이스 격리 때문에 AnalysisTemplate 도 env 별 존재해야 한다.

## 8. 이행 로드맵

| Phase | 작업 | 예상 소요 | 의존성 |
|-------|------|-----------|--------|
| P0 | 태그 전략 CI 적용 (jib `sha-*` 태그 push) | 2h | 없음 |
| P1 | Kargo Project/Warehouse/Stage 리팩토링 (v1 버그 fix + 새 태그 필터) | 4h | P0 |
| P2 | AnalysisTemplate 3 벌 작성 + staging 실제 동작 검증 | 1d | P1, Prometheus 메트릭 이름 확인 |
| P3 | Argo Rollouts 설치 + gateway prod 를 Rollout 으로 교체 + Istio subset wiring | 1d | P2 |
| P4 | ApplicationSet PR Generator + preview CI job + wildcard 인증서 | 1d | P0 |
| P5 | NetworkPolicy / ResourceQuota / LimitRange 적용 | 0.5d | 네임스페이스 분리 완료 |
| P6 | event-broadcaster 및 후속 서비스에 동일 패턴 복제 | 서비스당 1h | P3 |

**P0 → P1 → P2 순서는 엄격**. P3/P4/P5 는 병렬 가능.

## 9. 검토 체크리스트
- [ ] **staging 유지**: 이 문서는 staging 유지로 작성. 실제로 3 환경 운영 원하시면 OK. 옵션 D 원본(삭제) 원하시면 §2 의 `handbook-staging` 제거 + §4.3 삭제 + §7.2 삭제
- [ ] **태그 포맷**: `sha-<8char>` vs full 40 자 vs `<8char>-<short-timestamp>` 어느 게 나은지
- [ ] **태그 push 위치**: 현재 CI 가 jib 로 `latest` 만 푸시 중. `sha-*` 로 전환 OK?
- [ ] **Prometheus 엔드포인트**: `prometheus-k8s.openshift-monitoring:9090` 이 실제 주소인지, 아니면 별도 `prometheus-stack` 인지
- [ ] **Spring Boot 4 메트릭 이름**: `http_server_requests_seconds_*` 가 실제 Micrometer Observation 에서도 동일하게 나오는지 (`/actuator/prometheus` 출력 확인)
- [ ] **Istio VirtualService subset**: 현재 infrastructure 차트의 VirtualService 가 single destination 인지, subset 구조로 되어 있는지 확인 필요. 단순 destination 이면 canary 전 재구성 필요
- [ ] **Preview infrastructure 공유**: PG schema 분리, Kafka topic prefix 전략 합의 필요
- [ ] **Wildcard 인증서**: `*.preview.handbook.sayaya.cloud` OpenShift Route + cert-manager 발급 가능한지
- [ ] **ResourceQuota 값**: 현재 가정치. 실제 사용량 측정 후 조정
- [ ] **Kargo 설치 주체**: ArgoCD 로 Kargo 자체를 싱크 (operator-of-operator 패턴) vs helm 으로 직접 설치
- [ ] **Kargo ↔ ArgoCD 인증**: `argocd-update` 스텝이 사용할 ServiceAccount 토큰의 생성/관리 방법
- [ ] **롤백 런북**: Rollouts abort, Kargo 이전 Freight 재승격, git revert 각각의 경계 문서화 필요

## 10. 현재 차트와의 델타 요약

| 파일 | 변경 방향 |
|------|-----------|
| `.github/workflows/*-deploy.yaml` | jib 에 `sha-<8char>` 태그 푸시 추가 |
| `.github/workflows/*-preview.yaml` (신규) | PR 이벤트로 `pr-<num>-<sha>` 태그 푸시 |
| `charts/handbook/<svc>/templates/warehouse.yaml` | `allowTags`, `strictSemvers: false` 적용, git subscription 제거 (v1 제안 철회) |
| `charts/handbook/<svc>/templates/stage.yaml` | prod 단계 `http` 스텝 제거, `argocd-update` 로 통일 |
| `charts/handbook/<svc>/templates/deployment.yaml` | prod 전용 `Rollout` 렌더 분기 |
| `charts/handbook/<svc>/templates/service.yaml` | canary service 추가 |
| `charts/handbook/infrastructure/templates/analysis/*.yaml` (신규) | AnalysisTemplate 3벌 × env |
| `charts/handbook/templates/application-set.yaml` | preview ApplicationSet 추가 |
| `charts/handbook/infrastructure/templates/s3/virtual-service.yaml` | subset 기반 canary routing 재구성 |
| `charts/handbook/templates/namespace.yaml` (신규) | env 별 namespace + NetworkPolicy + ResourceQuota + LimitRange |
| `.claude/skills/deployment.md` | 전략 확정 시 내용 갱신 |

## 11. 오픈 질문
1. staging 유지/삭제 최종 결정 (§9 첫 번째 항목)
2. Preview 에서 PG/Kafka 를 dev 와 공유할지, 완전 독립 in-memory 로 갈지
3. canary 판정 지표를 Istio 메트릭으로 할지 Spring Actuator 메트릭으로 할지 (Istio 쪽이 trafficRouting 과 정합성 좋지만, JVM 내부 상태 반영은 Actuator 가 더 정확)
4. Kargo 를 이미 운영 중인지, 이번에 처음 설치하는지
5. Argo Rollouts 이 이미 설치되어 있는지 (OpenShift GitOps operator 에 포함되는 경우 있음)

---

검토 후 §9 체크리스트 답변 + §11 오픈 질문 답을 주시면 P0 부터 실행 가능한 PR 단위로 쪼개 드리겠습니다.
