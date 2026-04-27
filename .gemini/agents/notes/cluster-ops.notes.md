## 요청 로그

- 2026-04-27: 스마트 라우팅 반영 → ArgoCD Sync 및 검증 완료. curl -I는 Method=GET 제한으로 404 반환됨을 확인.
- 2026-04-27: 4915cae 반영 → Sync 및 재시작 완료, 200 OK 확인
- 2026-04-27: Clean URL 404 진단 → Helm ConfigMap 동기화 누락 확인
- 2026-04-26: shell-ui S3 점검 → JS 누락 확인 및 빌드 설정 결함 발견
- 2026-04-23: S3 shell-ui 파일 목록 조회 → .cache.js 파일 리스트 확보
- 2026-04-26: handbook-infra-dev Diff 확인 → 수동 싱크 필요성 및 ArgoCD Ghost Diff 가능성 파악
- 2026-04-26: handbook-infra-dev Sync → elasticsearch node lock 에러 재발 및 설정 불일치 발견
- 2026-04-26: ES 파드 장애 분석 -> PVC 권한 문제(UID 1000) 확인
- 2026-05-15: Gateway Fallback -> Accept 헤더 기반 Clean URL 설정 제안
- 2026-05-12: Elasticsearch 9.3.3 추가 -> 인프라 확장 및 검색 동기화 설정
- 2026-04-23: ArgoCD 동기화 및 메모리 제한 수정 -> handbook-operator 및 runner-set 검증 완료 및 GRADLE_OPTS 적용

---

# cluster-ops Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

- **Elasticsearch/Kafka 등 상태 저장 파드의 AccessDeniedException**:
  - 원인: 컨테이너의 UID(예: ES 1000)와 마운트된 볼륨(Ceph RBD/NFS)의 소유권(root) 불일치.
  - 진단: `oc debug` 파드로 마운트 포인트(`ls -ld /data`) 권한 확인.
  - 해결: `podSecurityContext.fsGroup` 설정 또는 `initContainer`를 통한 `chown`.

## 반복 함정

- **GWT war 태스크 의존성**: `build.gradle.kts` 수정 시 `war { dependsOn("gwtCompile") }` 섹션이 누락되면 정적 자산(CSS/HTML)만 배포되고 JS 가 빠지는 현상이 발생함. `shell-ui` 가 최근 커밋(`d9d52702`)에서 이 설정이 제거되어 결함이 발생함.

## 내부 체크리스트

- [ ] K8s 리소스 변경 시 → Helm 차트 템플릿 변수화 여부 확인
- [ ] 이미지 태그 업데이트 시 → Kargo Warehouse 수동 승인 필요 여부 확인

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
## 요청 로그
- 2026-04-26: elasticsearch 재시작 분석 → node lock 실패 및 권한 문제 확인

## 탐색 패턴
- Elasticsearch CrashLoopBackOff 진단 시 `oc logs --previous`의 `failed to obtain node locks` 패턴은 주로 PVC 권한(UID/GID) 불일치에서 기인함.
