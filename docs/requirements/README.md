# 요구사항 도메인 인덱스

`docs/requirements.md` 의 섹션별 도메인 매핑. 각 에이전트는 자신의 스코프에 해당하는 섹션 범위를 읽어 작업한다.

**주의**: 현재는 `docs/requirements.md` 단일 파일이 원본이다. 이 인덱스는 도메인별 범위를 가리킨다.
본문 이동(migration) 은 docs-keeper 주도로 점진적으로 수행된다.

---

## 도메인 매핑

| 도메인 | 담당 에이전트 | requirements.md 섹션 | 관련 계약 |
|--------|-------------|--------------------|-----------|
| 인증 / 권한 | auth-expert | §3.3 RBAC, §3.8 인증 | [permissions](../contracts/permissions.md) |
| 워크스페이스 / 그룹 | workspace-expert | §3.1 워크스페이스, §3.2 사용자·그룹 | [permissions](../contracts/permissions.md) |
| 타입 / 스키마 | schema-expert | §3.4 타입, §3.5 시각화, §3.20 필드 권한 | [events](../contracts/events.md), [versioning](../contracts/versioning.md) |
| 문서 / 이력 | document-expert | §3.6 문서, §3.7 이력, §3.10 검증, §3.18 워크플로우 | [events](../contracts/events.md), [versioning](../contracts/versioning.md) |
| 이벤트 / 실시간 | events-expert | §3.9 이벤트 | [events](../contracts/events.md), [sse](../contracts/sse.md) |
| UI / 모바일 / 디자인 | ui-platform-expert | §3.11 웹 UI, §3.14 모바일, §3.15 대시보드, §3.21 차트, §6.6 접근성, §6.8 설정 | [design-tokens](../contracts/design-tokens.md), [menus](../contracts/menus.md) |
| AI 어시스턴트 | assistant-expert | §3.16 품질 감시, §3.17 자연어 변경 | [agent-commands](../contracts/agent-commands.md), [audit](../contracts/audit.md) |
| 랜딩 / 외부 AI | landing-expert | §3.22 랜딩 페이지, §3.23 외부 AI 통합 | [api](../contracts/api.md) |
| API 외부 연동 | assistant-expert (주) + landing-expert (외부 AI) | §3.12 API 접근성, §3.19 웹훅 | [api](../contracts/api.md), [audit](../contracts/audit.md) |
| 사용성 일반 | ui-platform-expert | §3.13 사용성 | — |
| 비기능 요구사항 | cluster-ops (운영) + 각 도메인 | §5 비기능, §7 품질 향상 | — |
| 추가 구현 | 각 도메인 | §6 추가 구현 | — |

---

## 메인 Gemini 사용법

- 사용자 질문 도메인 분석 → 위 테이블에서 담당 에이전트 확인
- 해당 에이전트 호출 시 스코프 파일 지정: `docs/requirements.md` + 섹션 범위 (예: "§3.22–§3.23")
- 계약 변경이 걸리면 관련 계약 문서 + 해당 매트릭스(`docs/contracts/README.md`) 의 OWNER/소비자 참조

## 후속 작업 (docs-keeper 대상)

- §3.x 섹션을 각 도메인 파일(`docs/requirements/<domain>.md`) 로 물리 이동
- 이동 완료 시 `docs/requirements.md` 는 이 인덱스로 대체
- 이동 순서는 변경 빈도 낮은 도메인부터 (위험 최소화)
