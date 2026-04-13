# Kargo 배포 전략 설계 (제안)

> 상태: **제안 / 검토 대기**. 현재 차트의 `stage.yaml`, `warehouse.yaml` 과 대비해 개선점을 정리한 문서. 합의 후 단계별로 적용.

## 1. 현재 구현 요약

`charts/handbook/<service>/templates/warehouse.yaml` + `stage.yaml` 이 서비스×스테이지 매트릭스로 Kargo 리소스를 찍어낸다.

- **Warehouse**: OpenShift ImageStream(`handbook/<service>`) 구독, `imageSelectionStrategy: NewestBuild`, `strictSemvers: true`, interval 1m.
- **Stage (dev)**: Warehouse 를 direct 로 구독 → `argocd-update` 스텝으로 ArgoCD Application 의 Helm image.tag 를 갱신.
- **Stage (staging)**: dev 를 upstream 으로 → 같은 `argocd-update` 스텝 + AnalysisTemplate `<svc>-staging` 검증.
- **Stage (prod)**: staging 을 upstream 으로 → `http` 스텝으로 GitHub `repository_dispatch` (event_type `release`) 호출.

## 2. 문제점

| # | 포인트 | 영향 |
|---|--------|------|
| 1 | `strictSemvers: true` + `NewestBuild` 조합 모순 — 전자는 semver 태그만 필터, 후자는 레지스트리 생성 시각 기준 정렬 | 태그 선택 규칙이 의도대로 안 먹고, 설계 의도가 불명확 |
| 2 | prod 단계에서 `http` → GitHub dispatch 호출 | 동일 Freight 를 그대로 전진해야 할 시점에 CI 재빌드가 끼어들면 **이미지 digest 가 바뀔 수 있어 Freight immutability 가 깨짐**. dev/staging 과 경로가 비대칭이라 감사·롤백이 어려움 |
| 3 | Warehouse 가 **image 만 구독** | chart/values 변경만 일어난 배포가 Freight 에 기록되지 않음 → "무엇이 프로모션되는가" 가 이미지 한 건에 한정 |
| 4 | verification 불균형 — dev 는 주석, staging 만 AnalysisTemplate, prod 는 없음 | 가장 위험한 prod 프로모션에 자동 SLO 게이트가 없음 |
| 5 | Promotion 트리거가 Stage spec 에 명시되지 않음 | auto/manual 경계가 암묵적. 누가 언제 promote 할 수 있는지 코드만 봐서 모름 |
| 6 | 멀티 서비스 release train 개념 없음 | 각 서비스가 독립 Warehouse → gateway+event-broadcaster 를 한 릴리즈로 묶어 동기 배포할 수단이 없음 |
| 7 | ArgoCD Application 이 Helm chart 경로를 직접 싱크 | Kargo 가 image.tag 를 pokes in-place. diff 가 "helm value 한 줄" 으로만 보여 사람이 배포 시점에 실제 매니페스트 변화를 못 본다 |

## 3. Kargo 베스트 프랙티스 원칙

Kargo 공식 docs / 샘플 리포 (`akuity/kargo`) 에서 반복적으로 강조되는 원칙:

1. **Freight Immutability** — Freight = `{image digest, git commit, chart version}` 스냅샷. 한번 만들어지면 수정 없이 stage 를 따라 그대로 승격. 롤백은 "오래된 Freight 를 다시 승격".
2. **Rendered Manifests Pattern** — 소스 브랜치(main)는 Helm/Kustomize 그대로 두고, 스테이지별 **렌더된 YAML 을 전용 브랜치 또는 폴더** (`env/dev`, `env/staging`, `env/prod`) 에 커밋. ArgoCD Application 은 렌더 결과만 싱크. 장점: 사람이 diff 로 변화를 직접 검토 가능, Kargo promotion 이 파일 수준의 `git-commit` 으로 완결.
3. **Promotion Steps 표준화** — `git-clone` → `kustomize-set-image` or `helm-template` or `yaml-update` → `git-commit` → `git-push` → `argocd-update`. 외부 CI HTTP 호출은 회피.
4. **Verification 은 모든 stage** — AnalysisTemplate(Argo Rollouts) 혹은 `verification.args` 로 Prometheus 쿼리 / smoke 엔드포인트 / 수동 approval. 실패 시 promotion 자동 취소.
5. **PromotionPolicy 명시** — `Project` 레벨에 AutoPromotionPolicy + approver 목록. Stage 는 실행 내용만, 권한은 Project 가 관리.
6. **Warehouse 는 image + git 동시 구독** — Freight 에 이미지 digest 뿐 아니라 commit SHA 를 묶어 "이 이미지 안의 코드가 정확히 어떤 상태였는지" 가 Freight 로 추적 가능.
7. **자격증명은 annotated Secret** — `kargo.akuity.io/cred-type: git|image|helm` 레이블. Kargo controller 가 자동 발견.
8. **Tag 선택 전략 일관성** — Git SHA 태그 → `NewestBuild`(레지스트리 시각) 또는 `Lexical`. Semver 태그(v1.2.3) → `SemVer` + 제약(`constraint: ^1.0.0`). 하나만 택일.

## 4. 제안 설계 (Target State)

### 4.1 아티팩트 태깅
- CI(GitHub Actions) 가 이미지를 `sha-<git-short-sha>` 로 푸시 (불변). `latest` 는 편의용으로만 사용.
- Git 리포는 main 브랜치가 소스.

### 4.2 Warehouse
하나의 `handbook` 프로젝트 네임스페이스에 서비스별 Warehouse. 각 Warehouse 는:

```yaml
apiVersion: kargo.akuity.io/v1alpha1
kind: Warehouse
metadata:
  name: gateway
  namespace: handbook
spec:
  interval: 1m
  freightCreationPolicy: Automatic
  subscriptions:
    - image:
        repoURL: image-registry.openshift-image-registry.svc:5000/handbook/gateway
        imageSelectionStrategy: NewestBuild   # sha-* 태그는 semver 아니므로 NewestBuild
        allowTags: ^sha-[a-f0-9]+$
        strictSemvers: false
        insecureSkipTLSVerify: true
        discoveryLimit: 10
    - git:
        repoURL: https://github.com/sayaya1090/handbook.git
        branch: main
        includePaths:
          - charts/handbook/gateway/**
          - gateway/**
```

핵심 변경:
- `strictSemvers: false` + `allowTags` 정규식으로 sha 태그만 통과.
- **Git subscription 추가**: 차트/소스가 바뀌어도 Freight 가 생성되어 "설정만 변경된" 릴리즈를 추적 가능. `includePaths` 로 무관 경로 제외.

### 4.3 Stage — 공통 Promotion Template (Rendered Manifests)

```yaml
apiVersion: kargo.akuity.io/v1alpha1
kind: Stage
metadata:
  name: gateway-dev
  namespace: handbook
spec:
  requestedFreight:
    - origin: { kind: Warehouse, name: gateway }
      sources: { direct: true }
  promotionTemplate:
    spec:
      vars:
        - name: targetBranch
          value: env/dev
        - name: servicePath
          value: env/dev/gateway
      steps:
        - uses: git-clone
          config:
            repoURL: https://github.com/sayaya1090/handbook.git
            checkout:
              - branch: main
                path: ./src
              - branch: ${{ vars.targetBranch }}
                create: true
                path: ./out
        - uses: helm-template
          config:
            path: ./src/charts/handbook/gateway
            outPath: ./out/${{ vars.servicePath }}
            releaseName: gateway
            namespace: handbook
            valuesFiles:
              - ./src/charts/handbook/gateway/values.yaml
              - ./src/charts/handbook/gateway/values-dev.yaml
        - uses: yaml-update
          config:
            path: ./out/${{ vars.servicePath }}/deployment.yaml
            updates:
              - key: spec.template.spec.containers.0.image
                value: ${{ imageFrom("image-registry.openshift-image-registry.svc:5000/handbook/gateway").tag }}@${{ imageFrom(...).digest }}
        - uses: git-commit
          as: commit
          config:
            path: ./out
            message: |
              chore(release): gateway-dev ${{ outputs.commit.commit }}
              Freight: ${{ ctx.freight.name }}
        - uses: git-push
          config: { path: ./out, targetBranch: ${{ vars.targetBranch }} }
        - uses: argocd-update
          config:
            apps:
              - name: handbook-gateway-dev
                sources:
                  - repoURL: https://github.com/sayaya1090/handbook.git
                    desiredRevision: ${{ outputs.commit.commit }}
  verification:
    analysisTemplates:
      - name: gateway-smoke
```

- **dev → staging → prod 동일한 promotionTemplate**. 차이는 `vars.targetBranch` / `vars.servicePath` + `verification` 뿐.
- prod 에서도 `argocd-update` 를 사용하고 **GitHub dispatch 제거**. 같은 Freight 의 digest 가 그대로 전진함.
- ArgoCD Application 3 개 (`handbook-gateway-{dev,staging,prod}`) 는 각각 `env/dev`, `env/staging`, `env/prod` 브랜치 경로를 참조.

### 4.4 PromotionPolicy & 승인

```yaml
apiVersion: kargo.akuity.io/v1alpha1
kind: Project
metadata: { name: handbook }
spec:
  promotionPolicies:
    - stageSelector: { name: gateway-dev }
      autoPromotionEnabled: true
    - stageSelector: { name: gateway-staging }
      autoPromotionEnabled: false   # 수동
    - stageSelector: { name: gateway-prod }
      autoPromotionEnabled: false
```

추가로 **Approval 필요**: Kargo UI 에서 staging/prod promotion 시 RBAC 의 `promote` 권한 보유자만 허용. Git 측에서 `env/prod` 브랜치를 Protected Branch 로 걸고 Kargo service account 만 push 가능하도록 제한.

### 4.5 Verification (AnalysisTemplate)

단계별로 난이도 상승:

- **dev (`gateway-smoke`)**: 롤링 완료 후 `/actuator/health/readiness` 200 확인 + 3분간 5xx 비율 < 5%.
- **staging (`gateway-staging`)**: p95 latency < 500ms, 5xx < 1%, Kafka consumer lag(event-broadcaster 쪽) < 100 을 10분 유지.
- **prod (`gateway-prod`)**: Canary 분할이 가능하면 Argo Rollouts 연동. 불가하면 staging 과 동일 쿼리 + 수동 사인오프.

Prometheus 기반 AnalysisTemplate 예시:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata: { name: gateway-smoke, namespace: handbook }
spec:
  metrics:
    - name: 5xx-ratio
      interval: 30s
      count: 6
      successCondition: result[0] < 0.05
      provider:
        prometheus:
          address: http://prometheus.observability:9090
          query: |
            sum(rate(http_server_requests_seconds_count{app="gateway",status=~"5.."}[2m]))
            /
            sum(rate(http_server_requests_seconds_count{app="gateway"}[2m]))
```

### 4.6 멀티 서비스 Release Train (선택)

개별 서비스 독립 프로모션이 기본. 그러나 "gateway 와 event-broadcaster 를 한 묶음으로" 배포할 필요가 생기면:

- 공통 Warehouse `handbook-train` 에 두 이미지 + git 을 동시 구독.
- 공통 Stage `train-dev` 가 이 Warehouse 를 구독 → 개별 서비스 Stage 는 `train-<stage>` 를 upstream 으로.
- Freight 하나에 두 이미지가 묶여 atomic 승격.

## 5. 현재 → Target 이행 순서

| Phase | 작업 | 리스크 |
|-------|------|--------|
| **P0** | Warehouse `strictSemvers` 오동작 수정 (`false` + `allowTags`), prod stage 의 http dispatch 를 argocd-update 로 교체, prod verification 추가 | 낮음. 동일 Freight 재사용으로 즉시 일관성 회복 |
| **P1** | Warehouse 에 git subscription 추가 | 낮음. 기존 Freight 와 호환 |
| **P2** | Rendered Manifests 전환 — `env/<stage>` 브랜치 생성, ArgoCD Application repoURL/path 변경, promotionTemplate 을 `git-clone + helm-template + yaml-update + git-commit + git-push + argocd-update` 로 재작성 | 중간. ArgoCD 리소스 경로 변경을 수반. 한 서비스 먼저 파일럿 후 확산 권장 |
| **P3** | AnalysisTemplate 표준화 (smoke / staging / prod) | 낮음. 실패해도 promotion 취소만 될 뿐 기존 파드 영향 없음 |
| **P4** | PromotionPolicy 를 Project CR 에 명시 + Kargo RBAC 설정 | 낮음 |
| **P5** | (선택) 멀티 서비스 release train 도입 | 중간. 서비스간 독립성 약화의 트레이드오프 검토 필요 |

각 Phase 는 단일 서비스(`gateway`) 로 먼저 검증한 뒤 `event-broadcaster` 와 후속 서비스에 복제.

## 6. 검토 체크리스트

- [ ] 이미지 태깅 컨벤션 합의 (`sha-<short>` vs semver `v*.*.*`)
- [ ] Rendered Manifests 브랜치 전략 승인 (`env/<stage>` 브랜치 vs main 의 `stages/<stage>/` 폴더)
- [ ] GitHub dispatch 를 제거해도 CI 가 해야 할 다른 일이 남아 있지 않은지 확인 (예: 외부 알림, 감사 로그). 남아 있으면 Kargo `http` 스텝으로 promote 후 notify 만 호출하도록 분리.
- [ ] AnalysisTemplate 이 참조할 Prometheus 메트릭 이름이 실제 Actuator export 이름과 일치하는지 (예: Spring Boot 4 의 `http_server_requests_seconds_*` 여부)
- [ ] `env/prod` 브랜치에 대한 Protected Branch + Kargo SA push 권한 설정 완료 여부
- [ ] Kargo Project CR 을 누가 소유할지 (chart 에 포함 vs 별도 `kargo-projects/` 리포)
- [ ] 롤백 플로우: Kargo UI 에서 "이전 Freight 재승격" 동작을 매뉴얼로 기록 (`docs/runbook/rollback.md`)

## 7. 오픈 질문

1. 이미지 태그가 현재 `latest` 만 쓰이는 것 같은데, CI 가 sha 태그를 동시에 푸시하고 있는가? 아니면 `latest` 를 ImageStream 트리거로 쓰고 있는가?
2. staging 환경은 dev 와 같은 클러스터인가 별도인가? 같은 클러스터면 namespace 만 분리. 다른 클러스터면 Kargo controller 에 각 ArgoCD instance 자격증명이 필요.
3. 현재 AnalysisTemplate `gateway-staging` 이 실제로 정의되어 있는가? 템플릿에 참조만 있고 정의 매니페스트는 아직 못 찾았음 — 존재 여부 확인 필요.
4. prod 만 Kargo 밖에서 dispatch 로 돌아가는 현재 방식에 "CI 재빌드" 같은 의도가 있었는지 — 있다면 그 의도를 Kargo 안으로 어떻게 옮길지.

---

이 문서는 제안이며, 4장과 5장을 먼저 검토해 주시고 합의된 Phase 부터 실제 차트에 반영하면 됩니다.
