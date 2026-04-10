# Dashboard-UI 클래스 다이어그램

## Domain 계층

```mermaid
classDiagram
    class WorkspaceStats {
        <<@JsType native>>
        +int typeCount
        +int documentCount
        +int userCount
    }
    class QualityIssue {
        <<@JsType native>>
        +String type
        +String serial
        +String field
        +String severity
        +String message
    }
    class AgentActivity {
        <<@JsType native>>
        +double timestamp
        +String intent
        +int commandCount
        +String status
    }
```

## Usecase 계층

```mermaid
classDiagram
    class StatsProvider {
        -BehaviorSubject~WorkspaceStats~ subject
        +next(stats: WorkspaceStats)
        +getValue(): WorkspaceStats
        +asObservable(): Observable~WorkspaceStats~
        +subscribe(consumer: Consumer~WorkspaceStats~)
    }
    class QualityIssueList {
        -BehaviorSubject~List~QualityIssue~~ subject
        +next(issues: List~QualityIssue~)
        +getValue(): List~QualityIssue~
        +asObservable(): Observable~List~QualityIssue~~
        +subscribe(consumer: Consumer~List~QualityIssue~~)
    }
    class AgentActivityList {
        -BehaviorSubject~List~AgentActivity~~ subject
        +next(activities: List~AgentActivity~)
        +getValue(): List~AgentActivity~
        +asObservable(): Observable~List~AgentActivity~~
        +subscribe(consumer: Consumer~List~AgentActivity~~)
    }
    class DashboardRepository {
        <<interface>>
        +fetchStats(): Observable~WorkspaceStats~
        +fetchQualityIssues(): Observable~QualityIssue[]~
        +fetchAgentActivity(): Observable~AgentActivity[]~
    }

    StatsProvider --> WorkspaceStats
    QualityIssueList --> QualityIssue
    AgentActivityList --> AgentActivity
```

## Interfaces 계층

```mermaid
classDiagram
    class DashboardApi {
        -FetchApi fetchApi
        +fetchStats(): Observable~WorkspaceStats~
        +fetchQualityIssues(): Observable~QualityIssue[]~
        +fetchAgentActivity(): Observable~AgentActivity[]~
    }
    class StatsCardElement {
        -HTMLDivElement element
        -HTMLElement typeCountValue
        -HTMLElement docCountValue
        -HTMLElement userCountValue
        +element(): HTMLElement
    }
    class QualityPanelElement {
        -HTMLDivElement element
        -HTMLDivElement listContainer
        -renderIssues(issues: List~QualityIssue~)
        +element(): HTMLElement
    }
    class ActivityLogElement {
        -HTMLDivElement element
        -HTMLDivElement listContainer
        -renderActivities(activities: List~AgentActivity~)
        -formatTimestamp(ts: double): String «JSNI»
        +element(): HTMLElement
    }
    class DashboardElement {
        -HTMLDivElement element
        +element(): HTMLElement
    }
    class ApiModule {
        <<@Module>>
        +fetch(): FetchApi$
        +bindDashboardRepository(impl: DashboardApi): DashboardRepository
    }
    class Application {
        +onModuleLoad()
        -injectCss(href: String)$ «JSNI»
    }
    class Component {
        <<@Component>>
        +dashboard(): DashboardElement
        +statsProvider(): StatsProvider
        +qualityIssueList(): QualityIssueList
        +agentActivityList(): AgentActivityList
        +dashboardRepository(): DashboardRepository
    }

    DashboardApi ..|> DashboardRepository
    DashboardElement --> StatsCardElement
    DashboardElement --> QualityPanelElement
    DashboardElement --> ActivityLogElement
    StatsCardElement --> StatsProvider
    QualityPanelElement --> QualityIssueList
    ActivityLogElement --> AgentActivityList
    Application --> Component
    Component ..> ApiModule : modules
```

## 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **Port & Adapter** | DashboardRepository / DashboardApi | usecase의 포트 인터페이스를 interfaces에서 FetchApi로 구현 |
| **BehaviorSubject 상태 관리** | StatsProvider, QualityIssueList, AgentActivityList | 최신 값 보존 + 새 구독자에게 즉시 전달 |
| **Composite** | DashboardElement | StatsCardElement, QualityPanelElement, ActivityLogElement를 조합 |
