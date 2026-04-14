# 배포 전략 v2 — Preview + Kargo + Canary (검토용)

> 상태: **제안 / 검토 대기**. v1 (과설계) 대체. 사용자 확정 사항: 단일 클러스터, 3 환경(dev/staging/prod), 태그 전략 TBD, AnalysisTemplate 신규 작성 필요, 옵션 D(Preview + Canary) 방향.
>
> **환경 용도 (확정)**:
> - **dev** — **기능 테스트 환경**. Kargo 가 배포 후 Argo Workflows 로 Playwright E2E job 을 실행하고, 통과해야 staging 으로 승격.
> - **staging** — **부하 테스트 환경**. k6-operator 로 TestRun 을 실행하고 부하 중 Prometheus 메트릭 + k6 threshold 를 모두 만족해야 prod 승격 가능.
> - **prod** — 실사용자 트래픽. Argo Rollouts canary 로 점진 확장.
>
> **주의**: 옵션 D 원본은 staging 삭제 포함이었으나, dev=기능/staging=부하로 용도가 분리되므로 staging 은 유지해야 의미가 있다.

## 1. 큰 그림

다섯 개의 도구가 역할별로 담당:

| 층위 | 도구 | 역할 |
|------|------|------|
| Stage 승격 | **Kargo** | dev → staging → prod 간 Freight 이동. 승격 기록·롤백 관리 |
| Preview | **ArgoCD ApplicationSet (PR generator)** | PR 별 ephemeral 환경 자동 생성/삭제 |
| 기능 테스트 (dev 검증) | **Argo Workflows + Playwright** | dev 배포 직후 E2E 시나리오 실행, 성공해야 staging 승격 |
| 부하 테스트 (staging 검증) | **k6-operator** | staging 배포 직후 `TestRun` CR 로 부하 발생, k6 threshold + Prometheus 메트릭 이중 판정 |
| Prod 안전장치 | **Argo Rollouts + Istio** | prod 배포를 canary 로 점진 확장, SLO 위반 시 자동 롤백 |

**역할 분리 원칙**: Kargo 는 Freight 를 "언제 다음 단계로 넘길지" 만 결정. "무엇이 통과 조건인지" 는 각 환경 특화 도구가 정답한다.
- dev: Argo Workflows 가 E2E 시나리오를 실제로 돌려본 결과
- staging: k6 가 부하 중 관측한 threshold + AnalysisTemplate 이 Prometheus 로 재확인한 SLO
- prod: Rollouts 가 canary 구간 Istio 메트릭으로 판단

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
| `argo-workflows` | Argo Workflows controller (dev 기능 테스트 오케스트레이션) | 신규 설치 |
| `k6-operator` | k6-operator (staging 부하 테스트 CRD) | 신규 설치 |
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

### 4.2 Stage: dev (auto) — 기능 테스트 환경
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
        # 1) 이미지 태그 갱신 → ArgoCD 가 handbook-dev 네임스페이스로 sync
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
        # 2) Argo Workflows 의 Playwright E2E WorkflowTemplate 실행 요청
        - uses: http
          config:
            url: https://argo-workflows.argo-workflows.svc.cluster.local:2746/api/v1/workflows/argo-workflows/submit
            method: POST
            headers:
              - {name: Authorization, value: "Bearer {{`${{ secrets.argo.token }}`}}"}
              - {name: Content-Type, value: application/json}
            body: |
              {{`${{ quote({
                "resourceKind": "WorkflowTemplate",
                "resourceName": "gateway-e2e",
                "submitOptions": {
                  "parameters": [
                    "targetUrl=https://handbook-dev.sayaya.cloud",
                    "imageTag=" + imageFrom("image-registry.openshift-image-registry.svc:5000/handbook/gateway").tag,
                    "freight=" + ctx.freight.name
                  ]
                }
              }) }}`}}
            successExpression: response.status == 200
  verification:
    # Workflow 는 비동기 실행이지만, AnalysisTemplate 의 web provider 로 완료 상태를 폴링
    analysisTemplates:
      - name: gateway-functional
```

**dev 의 검증 = "기능 테스트 성공"**. Playwright WorkflowTemplate `gateway-e2e` 가 `handbook-dev.sayaya.cloud` 를 대상으로 시나리오 실행. AnalysisTemplate `gateway-functional` 이 Argo Workflows API 를 polling 해 최종 `Succeeded` 확인.

Project PromotionPolicy:
```yaml
promotionPolicies:
  - stageSelector: {name: gateway-dev}
    autoPromotionEnabled: true   # Warehouse → dev 자동
```

### 4.3 Stage: staging (auto 검증 통과 시) — 부하 테스트 환경
```yaml
apiVersion: kargo.akuity.io/v1alpha1
kind: Stage
metadata: {name: gateway-staging, namespace: kargo}
spec:
  requestedFreight:
    - origin: {kind: Warehouse, name: gateway}
      sources: {stages: [gateway-dev]}
  promotionTemplate:
    spec:
      steps:
        # 1) staging 배포
        - uses: argocd-update
          config:
            apps:
              - name: handbook-gateway-staging
                sources:
                  - repoURL: https://github.com/sayaya1090/handbook.git
                    helm:
                      images:
                        - key: image.tag
                          value: ${{ imageFrom(...).tag }}
        # 2) k6-operator 에 TestRun CR 생성 요청 (argocd-update 를 통해 매니페스트 commit/sync)
        - uses: argocd-update
          config:
            apps:
              - name: handbook-gateway-staging-loadtest
                # 별도 ArgoCD Application 이 k6 TestRun 매니페스트를 watch. image tag 변경으로 trigger
                sources:
                  - repoURL: https://github.com/sayaya1090/handbook.git
                    helm:
                      parameters:
                        - {name: testRunId, value: '${{ ctx.freight.name }}'}
                        - {name: targetImage, value: '${{ imageFrom(...).tag }}'}
  verification:
    analysisTemplates:
      - name: gateway-loadtest
```

**staging 의 검증 = "부하 중 SLO 유지"**. k6-operator 가 parallel runner pod 를 spin up 해 `staging.handbook.sayaya.cloud` 로 부하 발생. AnalysisTemplate `gateway-loadtest` 가 부하 중 Prometheus 메트릭 (RPS, p95, 5xx, CPU/mem saturation) 을 관측 + k6 threshold 결과 CR status 도 cross-check.

PromotionPolicy: `autoPromotionEnabled: true` (dev 통과 즉시 staging 자동 시작). verification 실패 시 Kargo 가 자동 차단.

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

## 7. 검증 자동화 — Test Job + AnalysisTemplate

각 환경은 **테스트 실행 주체**와 **판정 주체**가 분리된다.

| 환경 | 실행 주체 | 판정 주체 (AnalysisTemplate) |
|------|-----------|------------------------------|
| dev | Argo Workflows `gateway-e2e` WorkflowTemplate (Playwright) | `gateway-functional` — Workflow 완료 상태 polling |
| staging | k6-operator `gateway-load` TestRun CR | `gateway-loadtest` — 부하 중 Prometheus SLO + k6 threshold cross-check |
| prod canary | 실 트래픽 (Rollouts 가 subset 라우팅) | `gateway-canary` — Istio 메트릭 |

`charts/handbook/infrastructure/templates/analysis/` 아래에 env 별로 배치. Spring Boot 4 Actuator 메트릭 이름(`http_server_requests_seconds_*`) 가정 — 실제 `/actuator/prometheus` 출력 확인 필요.

### 7.1 dev 기능 테스트 — Argo Workflows WorkflowTemplate

```yaml
apiVersion: argoproj.io/v1alpha1
kind: WorkflowTemplate
metadata: {name: gateway-e2e, namespace: argo-workflows}
spec:
  entrypoint: run
  arguments:
    parameters:
      - {name: targetUrl}
      - {name: imageTag}
      - {name: freight}
  templates:
    - name: run
      inputs:
        parameters:
          - {name: targetUrl}
          - {name: imageTag}
          - {name: freight}
      container:
        image: mcr.microsoft.com/playwright:v1.52.0-jammy
        command: [sh, -c]
        args:
          - |
            git clone --depth=1 https://github.com/sayaya1090/handbook.git /work
            cd /work/e2e
            npm ci
            BASE_URL={{inputs.parameters.targetUrl}} npx playwright test --reporter=junit
        env:
          - {name: IMAGE_TAG, value: '{{inputs.parameters.imageTag}}'}
          - {name: FREIGHT_ID, value: '{{inputs.parameters.freight}}'}
      activeDeadlineSeconds: 900   # 15분 타임아웃
      retryStrategy:
        limit: 1
        retryPolicy: OnError
```

**`gateway-functional` AnalysisTemplate** — Argo Workflows API 를 polling:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata: {name: gateway-functional, namespace: handbook-dev}
spec:
  args:
    - {name: service, value: gateway}
    - {name: freight}
  metrics:
    - name: workflow-succeeded
      interval: 30s
      count: 30                        # 최대 15분 폴링
      successCondition: "result == 'Succeeded'"
      failureCondition: "result == 'Failed' || result == 'Error'"
      provider:
        web:
          url: "https://argo-workflows.argo-workflows.svc.cluster.local:2746/api/v1/workflows/argo-workflows?listOptions.labelSelector=freight={{args.freight}}"
          headers:
            - {key: Authorization, value: "Bearer $ARGO_TOKEN"}
          jsonPath: "{$.items[0].status.phase}"
```

**핵심**: Kargo 가 Workflow 를 submit 한 뒤 AnalysisTemplate 이 "완료" 를 기다린다. 실패 시 Kargo 가 자동으로 staging 승격을 차단.

### 7.2 staging 부하 테스트 — k6-operator TestRun

```yaml
apiVersion: k6.io/v1alpha1
kind: TestRun
metadata: {name: gateway-load, namespace: handbook-staging}
spec:
  parallelism: 4                       # runner pod 4개 병렬
  script:
    configMap: {name: k6-gateway-script, file: test.js}
  runner:
    env:
      - {name: BASE_URL, value: https://staging.handbook.sayaya.cloud}
      - {name: TEST_DURATION, value: 10m}
      - {name: TARGET_RPS, value: '200'}
    resources:
      limits: {cpu: 1, memory: 1Gi}
  arguments: --out experimental-prometheus-rw
  # Prometheus remote_write 로 k6 메트릭을 집계 → Prometheus 쿼리로 cross-check 가능
```

k6 스크립트 (`test.js`, ConfigMap 으로 배포):
```javascript
import http from 'k6/http'
import { check } from 'k6'
export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-arrival-rate',
      startRate: 10, timeUnit: '1s',
      preAllocatedVUs: 50, maxVUs: 200,
      stages: [
        { target: 200, duration: '2m' },
        { target: 200, duration: '6m' },
        { target: 0, duration: '2m' },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],           // 5xx < 1%
    http_req_duration: ['p(95)<500'],          // p95 < 500ms
  },
}
export default function () {
  const r = http.get(`${__ENV.BASE_URL}/actuator/health`)
  check(r, { 'status 200': (r) => r.status === 200 })
}
```

**`gateway-loadtest` AnalysisTemplate** — TestRun 상태 + Prometheus 메트릭 이중 판정:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata: {name: gateway-loadtest, namespace: handbook-staging}
spec:
  args:
    - {name: service, value: gateway}
  metrics:
    # 1차: k6 TestRun CR 상태가 finished 상태일 때 통과
    - name: k6-finished
      interval: 30s
      count: 40                        # 최대 20분 폴링
      successCondition: "result == 'finished'"
      failureCondition: "result == 'error' || result == 'stopped'"
      provider:
        web:
          url: "https://kubernetes.default.svc/apis/k6.io/v1alpha1/namespaces/handbook-staging/testruns/{{args.service}}-load"
          headers:
            - {key: Authorization, value: "Bearer $SA_TOKEN"}
          jsonPath: "{$.status.stage}"
    # 2차: 부하 중 서버 측 5xx 비율이 k6 threshold 와 일치하는지 cross-check
    - name: server-error-rate
      interval: 1m
      count: 10
      successCondition: result[0] < 0.01
      provider:
        prometheus:
          address: http://prometheus-k8s.openshift-monitoring:9090
          query: |
            sum(rate(http_server_requests_seconds_count{application="{{args.service}}",status=~"5..",namespace="handbook-staging"}[2m]))
            /
            clamp_min(sum(rate(http_server_requests_seconds_count{application="{{args.service}}",namespace="handbook-staging"}[2m])), 0.001)
    # 3차: JVM 리소스 saturation — 부하 중 GC 과다 / 힙 포화 감지
    - name: heap-saturation
      interval: 1m
      count: 10
      successCondition: result[0] < 0.85
      provider:
        prometheus:
          query: |
            max(jvm_memory_used_bytes{area="heap",application="{{args.service}}",namespace="handbook-staging"})
            /
            max(jvm_memory_max_bytes{area="heap",application="{{args.service}}",namespace="handbook-staging"})
```

**서비스별 확장:**
- event-broadcaster: `kafka_consumer_records_lag < 100` 추가 (부하 중에도 consumer 가 뒤쳐지지 않는지)
- persist-*: `r2dbc_pool_acquired / r2dbc_pool_max < 0.9` 추가 (풀 고갈 방지)

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
| P0 | 태그 전략 CI 적용 (jib `sha-<8char>` 태그 push) | 2h | 없음 |
| P1 | Kargo Project/Warehouse/Stage 리팩토링 (v1 버그 fix + 새 태그 필터) | 4h | P0 |
| P2a | **Argo Workflows 설치** + Playwright `gateway-e2e` WorkflowTemplate 작성 + `gateway-functional` AnalysisTemplate 작성 + dev 실제 동작 검증 | 1~2d | P1, E2E 시나리오 스크립트 준비 |
| P2b | **k6-operator 설치** + `gateway-load` k6 스크립트/TestRun 작성 + `gateway-loadtest` AnalysisTemplate 작성 + staging 실제 동작 검증 | 1~2d | P1, Prometheus remote_write 설정 |
| P3 | Argo Rollouts 설치 + gateway prod 를 Rollout 으로 교체 + Istio subset wiring + `gateway-canary` AnalysisTemplate | 1d | P2b |
| P4 | ApplicationSet PR Generator + preview CI job + wildcard 인증서 | 1d | P0 |
| P5 | NetworkPolicy / ResourceQuota / LimitRange 적용 | 0.5d | 네임스페이스 분리 완료 |
| P6 | event-broadcaster 및 후속 서비스에 동일 패턴 복제 (E2E/k6 스크립트는 서비스별 작성) | 서비스당 0.5~1d | P2a, P2b, P3 |

**P0 → P1 → (P2a, P2b 병렬) → P3 순서는 엄격**. P4/P5 는 P1 이후 병렬 가능.

**핵심 산출물 4개**:
1. `e2e/` 디렉토리 — Playwright 시나리오 (이미 프로젝트에 있으면 재활용 검토)
2. `charts/handbook/infrastructure/templates/workflows/<svc>-e2e.yaml` — WorkflowTemplate
3. `charts/handbook/infrastructure/templates/loadtests/<svc>-k6.*` — k6 스크립트 ConfigMap + TestRun
4. `charts/handbook/infrastructure/templates/analysis/<svc>-{functional,loadtest,canary}.yaml` — AnalysisTemplate 3벌 × 서비스 × env

## 9. 검토 체크리스트
- [ ] **dev = 기능 테스트 / staging = 부하 테스트 확정** (이 문서가 쓰는 전제)
- [ ] **기능 테스트 프레임워크**: Playwright E2E 권장. 현재 `e2e/` 디렉토리 시나리오가 dev 대상으로 재사용 가능한지, 전용 smoke 세트를 새로 쓸지
- [ ] **부하 테스트 도구**: k6-operator 권장. Gatling/JMeter/Locust 선호 있으면 표시
- [ ] **부하 시나리오 설계 초안**: 타깃 RPS(예: 200), 지속 시간(10m), 램프업/다운 곡선, 엔드포인트 조합. 최소 gateway `/menu` 집계 + persist-document 쓰기 정도는 포함해야 병목 노출 가능
- [ ] **Argo Workflows 설치 상태**: OpenShift GitOps / argocd operator 에 포함되어 있는 경우 있음. 이미 있으면 P2a 축소
- [ ] **k6-operator 설치 주체**: helm chart 또는 manifest 로 별도 설치 예정
- [ ] **Prometheus remote_write**: k6 부하 메트릭을 remote_write 로 수집할 수 있는 엔드포인트가 클러스터에 있는지 (또는 OTLP collector)
- [ ] **태그 포맷**: `sha-<8char>` vs full 40 자 vs `<8char>-<short-timestamp>` 어느 게 나은지
- [ ] **태그 push 위치**: 현재 CI 가 jib 로 `latest` 만 푸시 중. `sha-*` 로 전환 OK?
- [ ] **Prometheus 엔드포인트**: `prometheus-k8s.openshift-monitoring:9090` 이 실제 주소인지, 아니면 별도 `prometheus-stack` 인지
- [ ] **Spring Boot 4 메트릭 이름**: `http_server_requests_seconds_*` 가 실제 Micrometer Observation 에서도 동일한지 (`/actuator/prometheus` 출력 확인)
- [ ] **Istio VirtualService subset**: 현재 infrastructure 차트의 VirtualService 가 single destination 인지 subset 구조인지 확인 필요
- [ ] **Preview infrastructure 공유**: PG schema 분리, Kafka topic prefix 전략 합의 필요
- [ ] **Wildcard 인증서**: `*.preview.handbook.sayaya.cloud` OpenShift Route + cert-manager 발급 가능한지
- [ ] **ResourceQuota 값**: 현재 가정치. 실제 사용량 기반으로 조정. **staging 은 부하 runner 까지 수용해야 하므로 dev 대비 2~3배 필요**
- [ ] **Kargo 설치 주체**: ArgoCD 로 Kargo 자체를 싱크 (operator-of-operator) vs helm 직접 설치
- [ ] **Kargo ↔ ArgoCD 인증**: `argocd-update` 스텝용 ServiceAccount 토큰의 생성/관리
- [ ] **Kargo ↔ Argo Workflows 인증**: `http` 스텝에서 Workflows API 호출용 토큰 어떻게 발급
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
| `charts/handbook/infrastructure/templates/analysis/*.yaml` (신규) | AnalysisTemplate — functional/loadtest/canary × env |
| `charts/handbook/infrastructure/templates/workflows/<svc>-e2e.yaml` (신규) | Argo Workflows WorkflowTemplate — Playwright E2E 실행 |
| `charts/handbook/infrastructure/templates/loadtests/<svc>-k6.yaml` (신규) | k6 ConfigMap + TestRun 매니페스트 |
| `e2e/` (기존 또는 신규) | Playwright 시나리오 — dev 기능 테스트의 실체 |
| `charts/handbook/templates/application-set.yaml` | preview ApplicationSet 추가 |
| `charts/handbook/infrastructure/templates/s3/virtual-service.yaml` | subset 기반 canary routing 재구성 |
| `charts/handbook/templates/namespace.yaml` (신규) | env 별 namespace + NetworkPolicy + ResourceQuota + LimitRange |
| `.claude/skills/deployment.md` | 전략 확정 시 내용 갱신 |

## 11. 오픈 질문
1. `e2e/` 디렉토리 시나리오가 실제로 있는지, dev 대상(`https://handbook-dev.sayaya.cloud`) 으로 실행 가능한 상태인지
2. 부하 테스트의 "합격 기준" — RPS/지연/에러율의 구체적 수치를 어떻게 정할지 (현재 prod 트래픽 베이스라인이 있는지)
3. staging 부하 중에 DB/Kafka 가 prod 와 격리되어 있는지 — staging 부하가 prod 에 영향을 주지 않아야 함
4. Preview 에서 PG/Kafka 를 dev 와 공유할지, 완전 독립 in-memory 로 갈지
5. canary 판정 지표를 Istio 메트릭으로 할지 Spring Actuator 메트릭으로 할지
6. Kargo 를 이미 운영 중인지, 이번에 처음 설치하는지
7. Argo Rollouts / Argo Workflows / k6-operator 각각의 설치 현황

---

검토 후 §9 체크리스트 답변 + §11 오픈 질문 답을 주시면 P0 부터 실행 가능한 PR 단위로 쪼개 드리겠습니다.
