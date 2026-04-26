# cluster-ops 실시간 노트

## 요청 로그
- 2026-04-26: handbook-infra-dev Diff 확인 → Synced 확인, 자동 동기화 미설정으로 인한 수동 싱크 필요성 파악

## Crystallized
### ## 탐색 패턴
- ArgoCD Application이 Synced임에도 사용자가 Diff를 언급할 경우: 1) 자동 동기화(`automated`) 설정 여부 확인, 2) ApplicationSet의 `status` 캐시 지연 확인, 3) OpenShift SCC에 의한 런타임 필드 변조(Ghost Diff) 확인.

### ## 과거 실수

### ## 원칙 갱신 제안
