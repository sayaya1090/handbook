# docs-keeper Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-23: Global Kover exclusion for *Application, *Config, and *Properties implemented to improve test quality.
- 2026-04-18: §3.24.1 F 과금/구독 축 추가 → 표 1행 + 이월 기록 3항목 · 주의 박스(가시성 ≠ 실행)
- 2026-04-18: §3.24 재작성 (scope→allowedSessionStates 집합) → requirements.md + contracts/menus.md 전면 교체, 계층 추론 제거
- 2026-04-18: SessionState+MenuScope 초안 → requirements §3.26, contracts/menus.md scope 추가, 공급자 6개 매핑

- 2026-04-21: 레이아웃 수정(Workspace 선택창 찌그러짐 해결) 및 문서 정합성 점검 수행.

## 탐색 패턴

- **UC 추출**: `grep -nE "^### UC-[0-9]+" docs/usecases.md`
- **테스트에서 UC 참조**: `grep -rE "UC-[0-9]+" **/src/test/ */src/test/`
- **계약 소비자 검증**: `grep -rl "<ClassName>" **/src/main/`
- **MenuSupplier 공급자 전수 조사**: `grep -l "MenuSupplier" **/*.kt` (gateway 집계자 + 각 서비스 MenuController)
- **Menu 도메인 필드 정의 위치**: `activity/src/main/java/dev/sayaya/handbook/domain/Menu.java` (모든 공급자 응답 스키마와 shell-ui 역직렬화 대상. scope 같은 필드 추가 시 이 파일이 touch 대상)
- **requirements.md 섹션 번호**: 현재 §3.23 다음 §5 로 점프. §3.24 이후 자리가 비어 있으므로 새 "사용자 상태" 류 요구사항 삽입 지점으로 자연스러움

## 반복 함정

- **매트릭스만 보고 판단하면 위험**: 실제 테스트 파일 제목·주석까지 열어봐야 함 (약한 매칭은 "추정됨" 으로 표시)
- **사용자 관점 + Claude 관점 혼동 금지**: 이 파일은 Claude 내부용. 사용자 읽을 문서에는 일반 독자 관점 유지

## 내부 체크리스트

- [ ] 감사 요청 수신 시 → doc-structure.md 크로스체크 매트릭스 먼저 참조
- [ ] 계약 변경 감지 시 → contracts/README.md 매트릭스의 OWNER/소비자 전원 확인
- [ ] 에이전트 노트 감사 시 → 모든 `*.notes.md` 전수, 각자 "원칙 갱신 제안" 섹션 우선
- [ ] 테스트 커버리지 보고 시 → 매트릭스 vs 실제 존재 3-way 비교 (매트릭스 / 명시 UC 참조 / 명명 추정)
- [ ] 승격 실행 금지 — 후보만 반환

## 과거 실수

- **초안 시 계층 포함 `satisfiedBy` 모델 채택** — 사용자가 "집합 멤버십 + AND 축 독립" 으로 재설계 지시. 앞으로 상태 기반 필터는 기본 "명시 열거" 를 제안하고 계층 암묵 포함은 사용자 확인 후에만.

## 원칙 갱신 제안

- **AppBar 센터 슬롯 보호**: `flex: 1`인 슬롯 내 핵심 요소는 뷰포트 축소 시 찌그러짐 방지를 위해 `min-width`를 확보하고, `justify-content: center`로 시각적 균형을 유지해야 한다. (2026-04-21)

## 아카이브 요약

(없음)

---

마지막 감사: — (신규, 아직 감사 수행 전)
- 2026-04-23: Commit 4244003 audit → requirements.md (§3.24, §6.5) and menus.md contract updated to reflect auto-onboarding and space-in-name support.
