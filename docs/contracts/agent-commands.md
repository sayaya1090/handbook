# Agent Command 프로토콜 계약

에이전트(assistant) 가 프론트엔드 UI 를 원격 조작하는 명령 프로토콜.
Kafka `AGENT_COMMAND` 이벤트로 발행되어 SSE 로 전달된다.

## 공급자 (Providers)

- **assistant** — 자연어 요청 해석 후 실행 계획의 각 단계를 `AGENT_COMMAND` 이벤트로 발행
  - `usecase/GroupedPlanExecutor.kt`
  - `interfaces/event/KafkaAgentCommandEventPublisher.kt`
- **(후속) mcp-server** — MCP 클라이언트 요청을 동일 프로토콜로 변환 (§external-ai)

## 소비자 (Consumers)

- **agent-ui** — `AgentSseClient` 가 SSE 로 수신 → `AgentCommandHandler` 에 위임
  - `client/interfaces/AgentCommandHandler.java`
  - `client/interfaces/NavigateHandler.java`, `HighlightHandler.java` 등 10종
- **shell-ui / 각 UI activity** — `agent-bridge` 의 `CustomEvent` 를 구독해 domain-specific mutate 수행
- **dashboard-ui** — 에이전트 활동 로그 표시

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 신규 커맨드 타입 추가 | `agent-protocol/` JsType + 모든 핸들러 구현 + 시나리오 테스트 |
| 페이로드 필드 추가 | 공급자(assistant) + 소비자(agent-ui 핸들러) 양쪽 JSON 필드 |
| 실행 순서/그룹 변경 | `GroupedPlanExecutor` + PROGRESS 페이로드 포맷 |

---

## 커맨드 타입 (10종)

| Type | 설명 | 페이로드 필수 필드 |
|------|------|-------------------|
| `navigate` | 특정 화면으로 이동 (메뉴/도구 선택, URL 변경) | `target: {menu, tool}` |
| `highlight` | 요소 강조 (반복 펄스). **단순 시선 유도 전용** — 라벨/메시지/오버레이가 필요하면 `attention` 사용 | `target.selector` |
| `scroll` | 스크롤/포커스 이동 | `target.selector` |
| `preview` | 변경 전후 diff 인라인 표시 | `changes: [{path, op, value}]` |
| `mutate` | 실제 값 변경 (필드 입력, 행 추가/삭제) | `changes: [{path, op, value}]` |
| `notify` | 토스트/배너 | `level: info|warning|error`, `message` |
| `progress` | 진행률 표시 | `{currentGroup, totalGroups, parallel, stepCount}` |
| `attention` | 코치마크/스포트라이트 안내 (설명 텍스트·오버레이·화살표·뱃지) | `target.selector`, `style`, `message`, `position` |

**highlight vs attention 결정 트리:** "여기요" 정도의 단순 pulse → `highlight`. 설명 텍스트·화살표·뱃지 필요 → `attention`. shell-ui MenuRail 같은 UI 는 `.ui-highlight` class 변화를 자체 감지해 `TooltipCard` 를 COLLAPSE 상태에서 동반 표시한다 — agent-protocol 확장(과거 문서에 있던 `highlight.style` 필드)이 아니라 UI 측 책임.
| `await_confirm` | 사용자 확인 대기 | `options: ["confirm", "cancel", "edit"]` |
| `complete` | 작업 완료 (요약) | `summary`, `artifactId?`, `affectedResources?` |

## 메시지 공통 구조

```json
{
  "seq": 1,
  "type": "navigate",
  "target": { "menu": "type-editor", "tool": "customer" },
  "description": "고객 타입 편집기로 이동합니다",
  "executionId": "uuid-v4"
}
```

- `seq` — 실행 계획 내 단계 순서
- `type` — 10종 중 하나
- `description` — 사용자에게 보여줄 사유 (감사 로그에도 기록)
- `executionId` — 다중 실행 식별 (`ExecutionContext` 매핑)

## Canonical Selector (shell-ui DOM 앵커)

agent 가 `target.selector` 로 참조할 때 쓰는 표준 CSS 선택자. LLM 프롬프트와 외부
MCP 클라이언트 모두 이 표를 기준으로 selector 를 생성해야 한다. shell-ui 내부 구조가
바뀌면 여기와 함께 갱신한다.

| 대상 | Canonical selector | 설명 |
|------|--------------------|------|
| 데스크톱 MenuRail 아이템 | `.rail:first-child .item` (+ `[data-menu-title="<title>"]`) | `MenuRailItemElement`. `.ui-highlight` 감지로 `TooltipCard` 동반 표시 |
| 데스크톱 ToolRail 아이템 | `.rail:nth-of-type(2) .item` | `ToolRailItemElement` |
| 모바일 상단 탭 | `.menu-tabs md-primary-tab.menu-tab` (+ `[data-menu-title]`) | `MobileTabsElement`. overflow 분리 시 일부만 노출 |
| 모바일 overflow 메뉴 항목 | `.menu-tabs md-menu md-menu-item.menu-tab-menu-item` (+ `[data-menu-title]`) | overflow 팝업 내. 팝업 닫힘 상태에서는 타겟팅 전 `.menu-tabs-overflow-btn` 클릭 필요 |
| 모바일 overflow 버튼 | `.menu-tabs-overflow-btn` | 조건부 표시 (`[hidden]`) |
| AppBar 햄버거 | `#menu-toggle-button` (`.shell-app-bar-leading` 내부) | `MenuToggleButton` |
| AppBar 워크스페이스 셀렉터 | `.shell-app-bar-center .workspace` | 이전 `.drawer-header .workspace` 에서 이동 (2026-04) |
| AppBar 테마 토글 | `.shell-app-bar-trailing .item.rail-bottom` | `ThemeToggle`. 이전 `.rail .item.rail-bottom` 에서 이동 (2026-04) |
| AppBar 세션 액션 (Sign In/Out 등) | `.shell-app-bar-trailing .shell-app-bar-action` (+ `[data-menu-title]`) | `appBarSlot="trailing"` Menu 가 승격됨 (`menus.md` 참조) |
| Drawer overlay (모바일 햄버거) | `nav.drawer[overlay]` | 사용자 햄버거 열기 전까지는 `[hide]` |

**규칙**
- `[data-menu-title="<Menu.title()>"]` 속성으로 특정 엔트리 지정 권장. `title()` 은 i18n key 원본값 (번역 전 문자열).
- **Highlight 동반 tooltip 강조** 는 현재 `MenuRailItemElement` 만 구현. `.menu-tab` / `.shell-app-bar-action` 타겟팅 시 MD3 built-in 시각만 반응하므로 리팩토링 단계에서 `HighlightEffect` 공통화 예정 (`notes`: 호환성 검토 2026-04).
- **Deprecated**: `.drawer-header ...` — 2026-04 AppBar 도입 후 제거됨. LLM 은 이 경로를 생성하지 말 것.

## `attention` 스타일

| 스타일 | 설명 |
|--------|------|
| `coachmark` | 대상 주변 말풍선 + 반투명 오버레이 |
| `spotlight` | 대상만 밝게, 나머지 어둡게 |
| `pulse` | 대상 테두리 반복 강조 |
| `arrow` | 대상을 가리키는 화살표 |
| `badge` | 대상 옆 숫자/아이콘 뱃지 |

## 실행 그룹 (병렬 실행)

```kotlin
data class ExecutionStep(
    val seq: Int,
    val group: Int,  // 같은 group 은 병렬, 다른 group 간 순차
    val command: AgentCommand
)
```

- 같은 `group` 단계는 `Flux.merge` 로 동시 실행
- 다른 `group` 간에는 순차
- 그룹 내 한 단계 실패 시 나머지 취소

## 전송 경로

```
assistant 실행 계획
    → GroupedPlanExecutor (단계별 발행)
    → Kafka AGENT_COMMAND event
    → event-broadcaster
    → SSE /workspace/{id}/messages
    → shell-ui EventSource
    → AgentSseClient
    → AgentCommandHandler
    → (타입별 10종 Handler)
```

## 사용자 확인 흐름

1. assistant 가 `await_confirm` 발행 → 스트림 일시 정지
2. 사용자가 프론트엔드에서 선택 → `POST /assistant/respond` (executionId + 응답)
3. assistant 가 다음 단계 발행 → 재개
