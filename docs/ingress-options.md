# Handbook 외부 노출 방안 비교

> **2026-04-15 결정**: [옵션 3 (Kubernetes Gateway API)](#옵션-3-kubernetes-gateway-api-gatewaynetworkingk8sio) 로 진행. 셸 UI 정적 자산은 HTTPRoute URL rewrite 로 S3 에 직결하고, 나머지 경로는 catch-all HTTPRoute 가 Spring Cloud Gateway 로 포워딩하는 혼합 구성. 구현 리소스는 문서 끝 "구현 현황" 참조.

현재 상태 (결정 전 시점): handbook-dev/staging/prod 네임스페이스에 **외부에서 접근 가능한 인그레스가 없었다**.
- OpenShift Router 는 `*.apps.sayaya.cloud` 로 작동 중 (kiali / console 등은 이쪽으로 뚫려 있음)
- Istio 는 `istio` / `istio-waypoint` GatewayClass 가 설치돼 있음 (ambient 모드)
- 기존 `VirtualService/s3` 는 `gateway-https` Gateway CR 을 참조하는데 해당 Gateway 가 실제론 없어서 **무효 상태**
- 전통적 `istio-ingressgateway` deployment/service 없음
- `values.stages.<stage>.host` (예: `handbook.apps.sayaya.cloud`) 는 chart values 에만 존재, 실제 트래픽 경로 없음

셸 UI 의 정적 자산이 S3 에 배포돼도 브라우저가 가져갈 경로가 막혀있다.

---

## 옵션 1. OpenShift Route 단일

가장 단순. 한 개 리소스만 추가.

```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: handbook
  namespace: handbook-dev
spec:
  host: handbook.apps.sayaya.cloud
  to:
    kind: Service
    name: service-gateway         # Spring Cloud Gateway
  port:
    targetPort: 8080
  tls:
    termination: edge
```

### 장단점
- ✅ 리소스 1개, OpenShift 고유 idiom, 팀 친숙도 높음
- ✅ 백엔드 API 라우팅을 이미 gateway 가 담당하므로 통합적
- ❌ OpenShift Route 자체는 **URL rewrite 를 지원 안 한다**. `/` → `/handbook-dev/static/` 같은 prefix 변경 불가
- ⚠️ 따라서 gateway(Spring Cloud) 의 `spring.cloud.gateway.server.webflux.routes[*]` 에 `static` 엔트리를 S3 로 프록시하도록 추가해야 한다 (gateway 차트 ConfigMap 수정)

### 추가 작업
- `charts/handbook/gateway/templates/configmap.yaml` 의 `gateway.routes` 에 `static: http://rook-ceph-rgw-ocs-storagecluster-cephobjectstore.openshift-storage.svc/handbook-<env>` 추가
- jar 의 `application.yml` 에 `static` route 가 이미 있음 — URI 만 공급하면 됨
- Istio ServiceEntry `s3` 가 이미 있어서 mesh 안에서 외부로 나가는 건 OK
- `charts/handbook/gateway/templates/route.yaml` 신규 파일

---

## 옵션 2. Istio Gateway (legacy v1beta1) + 기존 VirtualService

기존 `s3` VirtualService 가 레거시 API 를 참조하므로 그쪽을 유효화.

### 현실
- 클러스터에 `istio-ingressgateway` pod 이 **없음** → 설령 `Gateway` CR 을 만들어도 데이터플레인 타겟이 없어 무효
- Istio ambient 모드는 전통적 ingress gateway 를 기본 배치 안 함
- **실질적으로 이 옵션은 ambient 환경에서 동작하지 않는다**

### 결론
⛔ 이 환경에선 버려야 함.

---

## 옵션 3. Kubernetes Gateway API (gateway.networking.k8s.io)

Istio ambient 에서 **북남 진입점의 표준 방식**. 클러스터에 `istio` GatewayClass 가 이미 설치돼 있어 `Gateway` CR 을 만들면 Istio 컨트롤러가 데이터플레인 pod 을 자동 프로비저닝한다.

### 필요 리소스

**1) `Gateway`** — 진입점 정의. apply 시 Istio 가 `handbook-istio` 같은 이름의 Deployment + Service(ClusterIP) 자동 생성.
```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: Gateway
metadata:
  name: handbook
  namespace: handbook-dev
spec:
  gatewayClassName: istio
  listeners:
    - name: http
      port: 80
      protocol: HTTP
      hostname: handbook.apps.sayaya.cloud
      allowedRoutes:
        namespaces:
          from: Same
```

**2) `HTTPRoute`** — 경로 매칭 + URL rewrite + 백엔드.
```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: shell-ui
  namespace: handbook-dev
spec:
  parentRefs:
    - name: handbook
  hostnames:
    - handbook.apps.sayaya.cloud
  rules:
    - matches:
        - path: { type: PathPrefix, value: / }
      filters:
        - type: URLRewrite
          urlRewrite:
            path:
              type: ReplacePrefixMatch
              replacePrefixMatch: /handbook-dev/static
      backendRefs:
        - name: rook-ceph-rgw-ocs-storagecluster-cephobjectstore
          namespace: openshift-storage
          port: 80
```
브라우저의 `GET /shell.html` → RGW 의 `GET /handbook-dev/static/shell.html` 로 rewrite → bucket=handbook-dev, key=static/shell.html 반환.

**3) `ReferenceGrant`** — cross-namespace backend 허용 (대상 ns 측 리소스).
```yaml
apiVersion: gateway.networking.k8s.io/v1beta1
kind: ReferenceGrant
metadata:
  name: handbook-dev-to-rgw
  namespace: openshift-storage      # ⚠️ openshift-storage ns
spec:
  from:
    - group: gateway.networking.k8s.io
      kind: HTTPRoute
      namespace: handbook-dev
  to:
    - group: ""
      kind: Service
      name: rook-ceph-rgw-ocs-storagecluster-cephobjectstore
```
운영 측 네임스페이스라 **apply 권한 문제** 가 있을 수 있음.

**4) `Route`** — 외부 DNS 연결 (옵션 2 없으면 필수).
Istio 가 만든 Gateway Service 는 기본 ClusterIP 라 외부 접근 불가. `apps.sayaya.cloud` wildcard DNS 를 쓰려면 OpenShift Route 로 forwarding.
```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: handbook
  namespace: handbook-dev
spec:
  host: handbook.apps.sayaya.cloud
  to:
    kind: Service
    name: handbook-istio           # Gateway 가 auto-provision 한 service
  port:
    targetPort: http
  tls:
    termination: edge
```

### 장단점
- ✅ Istio 표준, Kargo/Kiali/트레이싱과 자연스럽게 통합
- ✅ URL rewrite, 헤더 조작, traffic split 등 HTTPRoute 기능 전부 사용 가능
- ✅ 같은 패턴을 backend API 라우팅에 확장 가능 (나중에 /auth → login 등 rewrite)
- ✅ ambient mesh 와 궁합 좋음
- ❌ 리소스 4개, 디버깅 시 레이어가 많음 (DNS → Route → Gateway → HTTPRoute → ReferenceGrant → Service → RGW)
- ⚠️ `ReferenceGrant` 가 운영 ns 권한 필요
- ⚠️ 팀에 친숙도 낮을 수 있음

---

## 최종 권장

| 상황 | 추천 |
|------|------|
| 정적 자산만 빠르게 공개 | **옵션 3** (HTTPRoute URL rewrite 로 S3 prefix 숨김, 깔끔) |
| 백엔드 API 를 같은 호스트로 묶을 예정 + 이미 Spring Cloud Gateway 가 라우팅 중 | **옵션 1** (Route → Gateway Service, gateway 가 static route 도 담당) |
| Istio 표준에 수렴 + 확장성 우선 | **옵션 3** |
| 최소 변경 우선, 팀 친숙도 우선 | **옵션 1** |

옵션 2 는 이 클러스터에서는 **불가** (ambient 모드라 target 없음).

---

## 구현 현황 (2026-04-15)

옵션 3 기반으로 아래 리소스를 차트에 추가. `handbook-operator → handbook-shell-ui-<stage> / handbook-infra-<stage>` Application 체인으로 배포.

### 파일 카탈로그

| 파일 | 역할 |
|------|------|
| `charts/handbook/infrastructure/templates/gateway/gateway.yaml` | `gateway.networking.k8s.io/v1/Gateway` — Istio GatewayClass 로 진입점 프로비저닝 (`handbook-istio` Deployment + Service 자동 생성) |
| `charts/handbook/infrastructure/templates/gateway/route.yaml` | `route.openshift.io/v1/Route` — `handbook-istio` Service 를 OpenShift Router 로 TLS edge 노출 (`.Values.host` 로) |
| `charts/handbook/infrastructure/templates/gateway/http-route.yaml` | **catch-all** HTTPRoute (`name: gateway`) — 모든 `/` 요청을 Spring Cloud Gateway(`service-gateway:8080`) 로 포워딩 |
| `charts/handbook/infrastructure/templates/gateway/ceph-rgw-service.yaml` | `Service/ceph-rgw` (`type: ExternalName`) — Ceph RGW 를 같은 네임스페이스 이름으로 프록시. Gateway API cross-namespace backendRef 회피용. Istio gateway controller 가 ReferenceGrant 를 올바르게 인식 못 하는 이슈가 있어 이 패턴으로 우회 |
| `charts/handbook/shell-ui/templates/http-route.yaml` | 셸 UI 전용 HTTPRoute — **2 rule**: `/js/shell/**` PathPrefix + `ReplacePrefixMatch`, `/app.html` Exact + `ReplaceFullPath`. 두 rule 이 필요한 이유는 Gateway API 가 `ReplacePrefixMatch` 사용 시 오직 하나의 `PathPrefix` match 만 허용하기 때문. 두 rule 모두 `backendRefs: [ceph-rgw]` (ExternalName Service) |

### 요청 흐름

```
Browser ──TLS──▶ DNS (*.apps.sayaya.cloud → 192.168.1.9)
                    │
                    ▼
                 nginx LB (192.168.1.9, L4 stream proxy)
                 :443 → okd4_https_ingress_frontend (cluster nodes :443)
                 :80  → okd4_http_ingress_frontend  (cluster nodes :80)
                    │
                    ▼
                 OpenShift Router (cluster nodes)
                 - SNI/Host 매칭 → Route `handbook` (TLS edge termination, 기본 wildcard cert)
                    │
                    ▼
                 Service `handbook-istio` (Gateway API 자동 프로비저닝, MetalLB LB IP 192.168.1.203)
                    │
                    ▼
                 Istio Gateway (gateway.networking.k8s.io/Gateway "handbook")
                    │
          ┌─────────┴──────────┐
          ▼                    ▼
  HTTPRoute "shell-ui"   HTTPRoute "gateway" (catch-all)
  (/, /shell.html,        (/*)
   /js/shell/**)           │
          │                ▼
          │          service-gateway:8080 (Spring Cloud Gateway)
          │                │
          ▼                ▼
  Service `ceph-rgw`       백엔드 서비스 (login/search-*/persist-*/assistant/event-broadcaster)
  (ExternalName → openshift-storage 의 Ceph RGW)
  → bucket=handbook-<stage>
    key=static/<path>
```

**도메인 결정 (2026-04-15)**: dev = `handbook.apps.sayaya.cloud` 로 운영. `*.apps.sayaya.cloud` wildcard 의 OpenShift Router 기본 cert 가 적용되어 별도 cert 발급 불필요. staging(`handbook.sayaya.cloud`) / prod(`handbook.sayaya.dev`) 는 다른 도메인이라 cert 별도 작업 예정.

### 매칭 우선순위

Gateway API 의 [HTTPRoute 매칭 규칙](https://gateway-api.sigs.k8s.io/guides/http-routing/#conflict-resolution) 에 의해 **더 구체적인 path 가 먼저 매칭**된다. `shell-ui` HTTPRoute 의 `PathPrefix: /js/shell/` + `Exact: /shell.html, /` 이 `gateway` HTTPRoute 의 `/` PathPrefix 보다 우선하므로 catch-all 이 정적 자산을 가로채지 않는다.

### Ceph RGW Service (cross-namespace 우회)

Istio gateway 컨트롤러가 **`ReferenceGrant` 를 올바로 인식하지 못해** HTTPRoute 가 `openshift-storage` 의 Ceph RGW Service 를 직접 backend 로 참조하면 `RefNotPermitted` 가 떨어진다. 우회로 같은 네임스페이스에 `ExternalName` Service `ceph-rgw` 를 두고 HTTPRoute 가 이를 가리키게 했다 (`infrastructure/templates/gateway/ceph-rgw-service.yaml`).

### 기대 URL (dev)

- `https://handbook.apps.sayaya.cloud/` → Ceph RGW `GET /handbook-dev/static/app.html` (root rewrite)
- `https://handbook.apps.sayaya.cloud/app.html` → Ceph RGW `GET /handbook-dev/static/app.html`
- `https://handbook.apps.sayaya.cloud/js/shell/shell.nocache.js` → `GET /handbook-dev/static/js/shell/shell.nocache.js`
- `https://handbook.apps.sayaya.cloud/workspace/abc/types` → Spring Cloud Gateway → type-query
- `https://handbook.apps.sayaya.cloud/auth/login` → Spring Cloud Gateway → login
- `https://handbook.apps.sayaya.cloud/workspace/abc/messages` (SSE) → Spring Cloud Gateway → event-broadcaster

### 점진적 이전 여지

다음 단계에서 backend API 를 Spring Cloud Gateway 에서 HTTPRoute 로 이전 가능:
- `/auth/**` → login Service 직결
- `/workspace/*/types/**` GET → type-query, PUT/DELETE → type-command
- … 등
- CircuitBreaker 는 Istio `DestinationRule` (outlier detection) 으로 대체
- JWT 검증은 Istio `RequestAuthentication` + `AuthorizationPolicy` 로 대체
- `/menus` 집계는 BFF 로직이라 이전 어려움 — Spring Cloud Gateway 또는 별도 경량 merger 서비스로 유지

### 주의사항

- **ReferenceGrant** 는 `openshift-storage` 네임스페이스에 존재해야 하므로 `handbook-operator` 차트(운영자 권한) 에만 둔다. 일반 서비스 Application 체인으로는 생성 불가
- Istio Gateway 가 auto-provision 하는 `handbook-istio` Service 이름은 Gateway metadata.name 에 의존 — 변경 시 Route 의 `spec.to.name` 도 갱신 필요
- catch-all HTTPRoute 의 backend `service-gateway` 는 handbook-<stage> ns 의 Spring Cloud Gateway Service 이름 — 차트 상수로 박혀 있으니 서비스명 변경 시 동기화 필요
- HTTPRoute 의 `hostname` 은 `.Values.host` 에서 주입. ApplicationSet 이 `.Values.stages.<stage>.host` 를 helm parameter `host` 로 인프라/서비스 서브차트 양쪽에 넘김
