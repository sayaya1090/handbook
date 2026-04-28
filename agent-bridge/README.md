# Agent Bridge 모듈

GWT 클라이언트 모듈 간 에이전트 커맨드 및 관련 런타임 상태를 공유하기 위한 브릿지 라이브러리.

## 핵심 역할
- **에이전트 통신(Command)**: `agent-ui`가 수신한 명령을 실제 편집 모듈(`type-ui`, `document-ui` 등)로 전달.
- **네이티브 프로젝션(Native Projection)**: [agent-protocol](../agent-protocol/README.md)에 정의된 Java 클래스들을 GWT 런타임에서 복사 없이(`Js.cast`) 사용하기 위한 네이티브 인터페이스 제공.
- **상태 공유(State/Search)**: 에이전트가 각 모듈의 내부 데이터에 접근할 수 있도록 공급자(Provider)를 연결.

## 주요 구성 요소

### 1. 네이티브 모델 (Domain)
`agent-protocol`의 서브클래스들에 대응하며, `@JsType(isNative = true, name = "Object")`로 정의되어 JSON 파싱 결과와 100% 호환됨.
- `NavigateCommand`, `MutateCommand`, `HighlightCommand`, `AttentionCommand` 등

### 2. 브릿지 (Usecase)
| 클래스 | 설명 |
|--------|------|
| **AgentMutation** | 에이전트의 데이터 변경 명령(`mutate`)을 발행 및 수신. |
| **AgentSearch** | 에이전트가 모듈 내 데이터를 검색할 수 있도록 검색 공급자(`SearchProvider`) 연결. |
| **AgentState** | 에이전트가 현재 UI 상태를 스냅샷으로 찍을 수 있도록 상태 공급자(`StateProvider`) 연결. |
| **WorkspaceEvent** | 워크스페이스 컨텍스트(ID) 및 실시간 SSE 이벤트를 모든 모듈이 공유. |

## 에이전트 연동
**에이전트 연동: 핵심 기반 모듈.**
모든 프론트엔드 에이전트 기능은 이 브릿지를 통해 상호작용함.

## 의존성
- `agent-protocol`: 명령 명세 참조 (개념적 의존)
- `sayaya-rx`: 이벤트 스트림 처리를 위한 BehaviorSubject 활용
- `elemental2-dom`: 브라우저 CustomEvent 연동

## 개발 및 테스트
- **테스트 전략**: 
    - `Application.java`(GWT)에서 에이전트 브릿지 시나리오를 자동 실행하고 로그를 출력.
    - `AgentBridgeTest.kt`(Kotlin/Playwright)에서 브라우저 로그를 검증하는 **정석적인 런타임 테스트** 수행.

