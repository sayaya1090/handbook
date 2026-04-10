# UI-Components 모듈

GWT 기반 공유 UI 컴포넌트 라이브러리. 에이전트 커맨드 처리, 토스트 알림, 오버레이 안내, 확인 다이얼로그 등 프론트엔드 모듈에서 공통으로 사용하는 위젯을 제공한다.

## 구조

```
├── domain/
│   ├── ToastLevel.java       토스트 심각도 (INFO, SUCCESS, WARNING, ERROR)
│   └── OverlayStyle.java     오버레이 스타일 (COACHMARK, SPOTLIGHT, PULSE, ARROW, BADGE)
└── client/
    └── components/
        ├── ConfirmDialog.java     범용 확인 다이얼로그 (headline + 옵션 버튼)
        ├── DiffPanel.java         변경사항 Diff 미리보기 패널 (before → after)
        ├── HighlightEffect.java   DOM 요소 강조 효과 (pulse 애니메이션)
        ├── OverlayContainer.java  오버레이 안내 컨테이너 (코치마크, 스포트라이트 등)
        ├── ScrollEffect.java      스크롤 + 도착 강조 효과
        └── ToastContainer.java    토스트 메시지 컨테이너
```

## 주요 컴포넌트

| 컴포넌트 | 설명 |
|----------|------|
| ToastContainer | 레벨별(INFO/SUCCESS/WARNING/ERROR) 토스트 메시지 표시 |
| ConfirmDialog | 사용자 확인이 필요한 작업에 대한 모달 다이얼로그 |
| DiffPanel | 변경 전/후 비교를 MD3 카드로 시각화 |
| HighlightEffect | CSS 선택자로 요소를 찾아 pulse 강조 |
| ScrollEffect | 대상 요소로 부드럽게 스크롤 후 강조 |
| OverlayContainer | 코치마크, 스포트라이트 등 안내 오버레이 |

## 의존성

- GWT 2.13.0
- Elemento, Sayaya UI/RX, Dagger

## 빌드

```bash
./gradlew :ui-components:build
./gradlew :ui-components:test
```
