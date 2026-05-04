# Dashboard-UI 모듈

워크스페이스 현황 대시보드 (GWT). 통계 카드, 데이터 품질 이슈, 에이전트 활동 로그를 실시간으로 표시한다.
Shell이 `ModuleScriptManager`로 `js/dashboard/dashboard.nocache.js`를 동적 로딩하여 실행한다.

## Mount 패턴

`Application.onModuleLoad()` 은 `WindowRenderBridge.next(render)` 로 shell
`FrameUpdater` 에 Render 위임. body 직접 append 금지 —
[`docs/contracts/frame.md`](../docs/contracts/frame.md). `:agent-bridge` 의존
필요 (build.gradle.kts + `Dashboard.gwt.xml` 의 `AgentBridge` inherits).

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

## 감사 로그 (AuditLogWidget)

사용자 변경 이력과 에이전트 활동 이력을 통합 타임라인으로 표시한다. 날짜 범위 필터와 사용자/키워드 필터를 제공한다.

| 필터 | 설명 |
|------|------|
| 날짜 범위 | HTML5 date input으로 from/to 기간 필터링 |
| 사용자 필터 | intent 필드 대소문자 무시 부분 일치 검색 |

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

| `DashboardRepository.timeline()` | `GET /workspaces/{workspaceId}/stats/timeline?from=&to=&interval=` | 시계열 통계 |
| `DashboardRepository.distribution()` | `GET /workspaces/{workspaceId}/stats/distribution` | 타입별 문서 분포 |

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.

---

## 차트 컴포넌트

대시보드에 시계열 차트 및 분포 차트를 표시한다. Canvas 2D API로 렌더링한다.

### TimelineChartElement

기간별 추이를 라인 차트로 표시하는 컴포넌트.

| 속성 | 설명 |
|------|------|
| `data` | 시계열 데이터 배열 `{date, documentCreations, validationFailures, agentUsage}[]` |
| `interval` | 집계 간격 (`day` / `week`) |
| `from` / `to` | 조회 기간 |

표시 항목:

| 시리즈 | 색상 토큰 | 설명 |
|--------|----------|------|
| 문서 생성 추이 | `primary` | 일별/주별 문서 생성 건수 |
| 검증 실패율 추이 | `error` | 기간별 검증 실패 비율 |
| 에이전트 사용량 추이 | `tertiary` | 기간별 에이전트 요청 건수 |

### DistributionChartElement

타입별 문서 분포를 파이 차트로 표시하는 컴포넌트.

| 속성 | 설명 |
|------|------|
| `data` | 분포 데이터 배열 `{typeName, count}[]` |

색상: `primary`, `secondary`, `tertiary`, `error` 순서로 순환 할당.

### 상태 관리

| 클래스 | 역할 |
|--------|------|
| `TimelineProvider` | 시계열 통계 (BehaviorSubject) |
| `DistributionProvider` | 분포 통계 (BehaviorSubject) |

## 에이전트 연동

### 내부 assistant
- 시나리오: "워크스페이스 건강 상태 보여줘" -> assistant 가 `dashboard` 화면으로 `navigate`.
- 데이터 품질 안내: 품질 이슈 발견 시 해당 이슈 카드를 `attention` (spotlight)으로 강조.

### Agent Command 타겟
- navigate: `dashboard`
- highlight selector 패턴: `.stats-card`, `.quality-item`, `.agent-activity-row`

### 구현 현황

| 기능 | 상태 | 구현체 |
|------|------|--------|
| 문서 생성 추이 라인 차트 | 미구현 | `TimelineChartElement` |
| 검증 실패율 추이 라인 차트 | 미구현 | `TimelineChartElement` |
| 에이전트 사용량 추이 라인 차트 | 미구현 | `TimelineChartElement` |
| 타입별 문서 분포 파이 차트 | 미구현 | `DistributionChartElement` |
