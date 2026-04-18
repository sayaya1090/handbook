# cluster-ops Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: login Google OAuth 외부호출 → ServiceEntry 추가 (ambient mesh Unknown 해소)
- 2026-04-18: 3건 커밋 dev 배포 상태 + persist-document CM + orphan DB 조사
- 2026-04-18: persist-workspace 500 로그 수집 → POST /workspace 예외 스택 추출

## 탐색 패턴

- **ArgoCD 앱 상태 빠른 확인**: `oc -n openshift-gitops get applications | grep handbook`
- **Kafka 클러스터 상태**: `oc -n handbook-dev get kafka,kafkatopic`
- **Istio ambient 등록 확인**: pod annotation `ambient.istio.io/redirection=enabled`
- **ImageStream 현황**: `oc get is -n handbook` — 백엔드 ImageStream 은 Kargo Project 네임스페이스(`handbook`) 에 공용으로 놓는다. `handbook-<stage>` 에는 생성하지 않음.

## 반복 함정

- **Istio ambient 서비스 포트 규칙**: `port=targetPort=8080`, `name=http-xxx`, `appProtocol:http` 필수.
  규칙 위반 시 Kiali 에 워크로드 노드 누락 + 다운스트림 화살표 누락. (2026-04 login/persist-workspace 사례)
- **dataplane-mode:none 워크로드 (Kafka/PostgreSQL)**: 메시 opt-out. 클라이언트는 ambient 유지.
- **sayaya.cloud 공용 인프라 혼동 금지**: ztunnel PodMonitor 등 클러스터 스코프는 handbook 레포가 아님.
- **jib `MainClassInferenceException`**: top-level `fun main` 패턴만 사용 (companion object 금지).
- **jib `Obtaining project build output files failed`**: 의존 모듈의 `tasks.jar { enabled = false }` 제거.
- **ImageStream 누락 아티팩트 오버사이트**: 새 백엔드 서비스(persist-*/search-*) 추가 시 서브차트(Deployment/Service/Warehouse) 는 만들면서 `charts/handbook/templates/image-stream.yaml` 갱신을 빠뜨리기 쉽다. jib push 가 레지스트리에 ImageStream 을 implicit 로 만들긴 하지만 ArgoCD 미관리 상태(tracking-id 누락)로 남아 OutOfSync 신호조차 안 뜬다. (2026-04-17 persist-workspace/search-workspace 사례)

## 내부 체크리스트

- [ ] 새 서비스 차트 추가 시 → Service 포트 규칙 준수 (http-xxx + appProtocol)
- [ ] 새 backend 서비스 추가 시 → `values.services` 에 `kind: backend` 등록 (top-level image-stream 템플릿이 자동 렌더)
- [ ] Kargo promotion 트리거 전 → Warehouse 가 이미지 감지했는지 확인
- [ ] 배포 후 routes/kafka localhost fallback 시 → ConfigMap 에 운영 설정 전부 있는지 검증 (subPath file overwrite 모델)
- [ ] Gateway 라우트 0개 로딩 시 → `spring.cloud.gateway.server.webflux.routes` 경로 사용 확인

## 현재 알려진 이슈

- **handbook 네임스페이스에 stale ImageStream**: `assistant`, `persist-document`, `persist-type`, `search-document`, `search-type` — 과거 작업 잔재. 현 `values.services` 에 없고, 현 Kargo Warehouse 도 없음. 차후 cleanup 대상 (jib push 가 implicit 생성한 것으로 추정).
- **handbook Application OutOfSync**: `oc get application handbook` — 세부는 ImageStream 외 다른 리소스 차이일 수 있음. 재sync 시 추가 확인 필요.

## 과거 실수

- (2026-04-17) 초기 `image-stream.yaml` 이 gateway/event-broadcaster/login 3개 하드코딩이었음. persist-workspace/search-workspace 서브차트 생성 시 이 파일 갱신 누락. 범용성을 위해 `values.services` range 로 재작성 (kind=backend 필터).

## 원칙 갱신 제안

- **services 단일 출처 원칙 강화**: 백엔드 관련 리소스(ImageStream / ApplicationSet / RBAC) 는 모두 `values.services` 를 range 하도록 통일. 신규 서비스 추가 시 values.yaml 수정만으로 모든 리소스가 따라오게.

## 아카이브 요약

(없음)

---

마지막 감사: 2026-04-17 (ImageStream 누락 수정)
