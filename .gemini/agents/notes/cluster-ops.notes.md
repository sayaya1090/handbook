## 요청 로그

- 2026-05-15: Gateway Fallback -> Accept 헤더 기반 Clean URL 설정 제안
- 2026-05-12: Elasticsearch 9.3.3 추가 -> 인프라 확장 및 검색 동기화 설정
- 2026-04-23: ArgoCD 동기화 및 메모리 제한 수정 -> handbook-operator 및 runner-set 검증 완료 및 GRADLE_OPTS 적용

---

# cluster-ops Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

(미확보)

## 반복 함정

(미확보)

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
