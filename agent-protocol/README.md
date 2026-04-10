# Agent-Protocol 모듈

에이전트(AI 비서)와 프론트엔드 간 통신 프로토콜을 정의하는 도메인 모듈.
백엔드(assistant)와 프론트엔드(agent-ui) 양쪽에서 참조한다.

## 커맨드 체계

`AgentCommand`를 추상 베이스로, 10종의 서브클래스가 Jackson `@JsonTypeInfo` 다형성 직렬화로 JSON `type` 필드에 매핑된다.

```
AgentCommand (abstract)
├── NavigateCommand     — 화면 이동 (메뉴/도구/URL)
├── HighlightCommand    — 요소 하이라이트 (CSS 셀렉터)
├── AttentionCommand    — 주의 오버레이 (스타일 + 메시지 + 위치)
├── ScrollCommand       — 요소로 스크롤
├── PreviewCommand      — 변경 미리보기 (diff 형태)
├── MutateCommand       — 실제 데이터 변경 (changes 배열)
├── NotifyCommand       — 알림 토스트 (level + message)
├── ProgressCommand     — 진행률 표시 (value / max)
├── AwaitConfirmCommand — 사용자 확인 대기 (options 배열)
└── CompleteCommand     — 작업 완료 (summary)
```

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.

## 커맨드 상세

| 커맨드 | 필드 | 용도 |
|--------|------|------|
| `NavigateCommand` | menu?, tool?, url? | Shell 화면 이동 |
| `HighlightCommand` | target (CSS 셀렉터) | 요소 하이라이트 |
| `AttentionCommand` | target, style, message, position, dismissable | 안내 오버레이 |
| `ScrollCommand` | target | 스크롤 이동 |
| `PreviewCommand` | changes[] | 변경 미리보기 |
| `MutateCommand` | changes[] | 데이터 변경 |
| `NotifyCommand` | level (info/success/warning/error), message | 토스트 알림 |
| `ProgressCommand` | value, max | 진행률 |
| `AwaitConfirmCommand` | options[] | 확인 대화상자 |
| `CompleteCommand` | summary | 작업 완료 |

## AttentionStyle

| 스타일 | 설명 |
|--------|------|
| `COACHMARK` | 대상 주위에 설명 말풍선 |
| `SPOTLIGHT` | 주변 어둡게 + 대상 강조 |
| `PULSE` | 대상 테두리 펄스 애니메이션 |
| `ARROW` | 대상을 가리키는 화살표 |
| `BADGE` | 대상에 뱃지 부착 |

## 프로젝트 구조

```
agent-protocol/
├── build.gradle.kts
└── src/main/java/dev/sayaya/handbook/
    ├── AgentProtocol.gwt.xml
    └── domain/
        ├── AgentCommand.java          # 추상 베이스 (@JsonTypeInfo)
        ├── CommandType.java           # 10종 enum
        ├── AttentionStyle.java        # 5종 enum
        ├── NavigateCommand.java
        ├── HighlightCommand.java
        ├── AttentionCommand.java
        ├── ScrollCommand.java
        ├── PreviewCommand.java
        ├── MutateCommand.java
        ├── NotifyCommand.java
        ├── ProgressCommand.java
        ├── AwaitConfirmCommand.java
        └── CompleteCommand.java
```

## 의존성

- **jackson-annotations** — `@JsonTypeInfo`, `@JsonSubTypes` 다형성 직렬화
- **jsinterop-annotations** — GWT 환경에서도 사용 가능하도록
