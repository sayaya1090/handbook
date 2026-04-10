# Dashboard-UI 모듈

워크스페이스 현황 대시보드 (GWT). 통계 카드, 데이터 품질 이슈, 에이전트 활동 로그를 실시간으로 표시한다.
Shell이 `ModuleScriptManager`로 `js/dashboard.nocache.js`를 동적 로딩하여 실행한다.

---

## 통계 카드 (StatsCardElement)

워크스페이스의 핵심 지표를 카드 형태로 표시한다.

| 지표 | 소스 |
|------|------|
| 타입 수 | `WorkspaceStats.typeCount` |
| 문서 수 | `WorkspaceStats.documentCount` |
| 사용자 수 | `WorkspaceStats.userCount` |

---

## 품질 패널 (QualityPanelElement)

데이터 품질 이슈를 심각도별 뱃지와 함께 목록으로 표시한다.

| 심각도 | CSS 클래스 | 색상 토큰 |
|--------|-----------|----------|
| ERROR | `.dash-severity-error` | `error-container` / `on-error-container` |
| WARNING | `.dash-severity-warning` | `tertiary-container` / `on-tertiary-container` |
| INFO | `.dash-severity-info` | `secondary-container` / `on-secondary-container` |

---

## 활동 로그 (ActivityLogElement)

에이전트 활동을 타임라인으로 표시한다.

| 상태 | CSS 클래스 | 색상 |
|------|-----------|------|
| COMPLETE | `.dash-status-complete` | `primary` |
| RUNNING | `.dash-status-running` | `tertiary` |
| ERROR | `.dash-status-error` | `error` |

---

## 상태 관리

| 클래스 | 역할 |
|--------|------|
| `StatsProvider` | 워크스페이스 통계 (BehaviorSubject) |
| `QualityIssueList` | 품질 이슈 목록 (BehaviorSubject) |
| `AgentActivityList` | 에이전트 활동 목록 (BehaviorSubject) |

> 공통 패턴은 [설계 패턴](../docs/design-patterns.md#반응형-상태-관리-behaviorsubject) 참조.

## API 연동

| 포트 메서드 | HTTP | 설명 |
|------------|------|------|
| `DashboardRepository.stats()` | `GET /dashboard/stats` | 워크스페이스 통계 |
| `DashboardRepository.qualityIssues()` | `GET /dashboard/quality-issues` | 품질 이슈 목록 |
| `DashboardRepository.agentActivity()` | `GET /dashboard/agent-activity` | 에이전트 활동 로그 |

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
