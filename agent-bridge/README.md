# Agent-Bridge 모듈

에이전트(agent-ui)와 편집 모듈(type-ui, workspace-ui) 간 GWT 모듈 경계를 넘는 통신 브릿지.
별도 GWT 모듈로 컴파일되는 프론트엔드 모듈들이 `window` CustomEvent/속성을 통해 데이터를 교환한다.

## 배경

shell-ui와 agent-ui는 각각 독립된 GWT 컴파일 결과물(nocache.js)을 가지며, type-ui/workspace-ui도
`ModuleScriptManager`에 의해 별도 GWT 모듈로 동적 로딩된다. 서로 다른 JavaScript 컨텍스트에서 실행되므로
Java 레벨의 인터페이스만으로는 런타임 연결이 불가능하다. 이 모듈이 `window` 객체를 통한 브릿지를 제공한다.

## 인터페이스

| 인터페이스 | 방향 | 설명 |
|-----------|------|------|
| `MutationReceiver` | agent-ui → type-ui/workspace-ui | 에이전트 mutation 이벤트 수신 포트 |
| `StateProvider` | type-ui → agent-ui | 현재 편집 상태를 JSON으로 제공 |
| `SearchProvider` | type-ui → agent-ui | 검색 쿼리 실행 및 결과 JSON 반환 |

## 브릿지 구현

| 클래스 | 메커니즘 | 발행 측 | 구독 측 |
|--------|----------|---------|---------|
| `WindowMutationBridge` | `CustomEvent('handbook-mutate')` | agent-ui: `publish(changes)` | type-ui/workspace-ui: `receiver()` |
| `WindowStateProviderBridge` | `window.__handbook_stateProvider` | type-ui: `register(provider)` | agent-ui: `snapshot()` |
| `WindowSearchProviderBridge` | `window.__handbook_searchProvider` | type-ui: `register(callback)` | agent-ui: `search(query)` |
| `WindowProgressBridge` | `window.__handbook_progress` | shell-ui: `register(observer)` | agent-ui: `next(progress)` |
| `WindowUriBridge` | `window.__handbook_uri` | shell-ui: `register(observer)` | agent-ui: `next(uri)` |
| `WindowLabelBridge` | `window.__handbook_labels` | shell-ui: `publish(labels)` | agent-ui: `subscribe(callback)` |

## 사용법

### 발행 측 (agent-ui)

```java
// MutateCommand 수신 시
WindowMutationBridge.publish(changes);

// 상태 조회
String json = WindowStateProviderBridge.snapshot();

// 검색
Observable<String> result = WindowSearchProviderBridge.search("customer");
```

### 구독 측 (type-ui)

```java
// Dagger Module에서 MutationReceiver 제공
@Provides @Singleton static MutationReceiver mutationReceiver() {
    return WindowMutationBridge.receiver();
}

// Application에서 StateProvider/SearchProvider 등록
WindowStateProviderBridge.register(typeStateProvider);
WindowSearchProviderBridge.register(query -> searchProvider.search(query));
```

## 프로젝트 구조

```
agent-bridge/
├── build.gradle.kts
└── src/main/java/dev/sayaya/handbook/
    ├── AgentBridge.gwt.xml
    └── usecase/
        ├── MutationReceiver.java          # 인터페이스
        ├── StateProvider.java             # 인터페이스
        ├── SearchProvider.java            # 인터페이스
        ├── WindowMutationBridge.java      # CustomEvent 기반 브릿지
        ├── WindowStateProviderBridge.java # window 속성 기반 브릿지
        ├── WindowSearchProviderBridge.java # window 속성 기반 브릿지
        ├── WindowProgressBridge.java     # shell→agent Progress 브릿지
        ├── WindowUriBridge.java          # shell→agent URI 브릿지
        └── WindowLabelBridge.java        # shell→agent Labels 브릿지
```

## 의존성

- **sayaya-rx** — Observable, BehaviorSubject
- **Elemento** — DomGlobal, CustomEvent
