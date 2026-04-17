# cluster-ops Operational Notes

---

## 탐색 패턴

- **ArgoCD 앱 상태 빠른 확인**: `oc -n openshift-gitops get applications | grep handbook`
- **Kafka 클러스터 상태**: `oc -n handbook-dev get kafka,kafkatopic`
- **Istio ambient 등록 확인**: pod annotation `ambient.istio.io/redirection=enabled`

## 반복 함정

- **Istio ambient 서비스 포트 규칙**: `port=targetPort=8080`, `name=http-xxx`, `appProtocol:http` 필수.
  규칙 위반 시 Kiali 에 워크로드 노드 누락 + 다운스트림 화살표 누락. (2026-04 login/persist-workspace 사례)
- **dataplane-mode:none 워크로드 (Kafka/PostgreSQL)**: 메시 opt-out. 클라이언트는 ambient 유지.
- **sayaya.cloud 공용 인프라 혼동 금지**: ztunnel PodMonitor 등 클러스터 스코프는 handbook 레포가 아님.
- **jib `MainClassInferenceException`**: top-level `fun main` 패턴만 사용 (companion object 금지).
- **jib `Obtaining project build output files failed`**: 의존 모듈의 `tasks.jar { enabled = false }` 제거.

## 내부 체크리스트

- [ ] 새 서비스 차트 추가 시 → Service 포트 규칙 준수 (http-xxx + appProtocol)
- [ ] Kargo promotion 트리거 전 → Warehouse 가 이미지 감지했는지 확인
- [ ] 배포 후 routes/kafka localhost fallback 시 → ConfigMap 에 운영 설정 전부 있는지 검증 (subPath file overwrite 모델)
- [ ] Gateway 라우트 0개 로딩 시 → `spring.cloud.gateway.server.webflux.routes` 경로 사용 확인

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

---

마지막 감사: — (신규)
