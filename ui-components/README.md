# UI-Components 모듈

**에이전트 연동: 핵심 기반 모듈.** 에이전트 커맨드(`highlight`, `attention`, `scroll`, `notify`, `preview`, `confirm`)의 실제 UI 렌더링을 담당하는 컴포넌트들을 제공한다.

GWT 기반 공유 UI 컴포넌트 라이브러리. 에이전트 커맨드 처리, 토스트 알림, 오버레이 안내, 확인 다이얼로그 등 프론트엔드 모듈에서 공통으로 사용하는 위젯을 제공한다.

## 구조

```
├── domain/
│   ├── Action.java           실행/롤백 액션 인터페이스 (Undo/Redo 스택 관리 단위)
│   ├── ToastLevel.java       토스트 심각도 (INFO, SUCCESS, WARNING, ERROR)
│   └── OverlayStyle.java     오버레이 스타일 (COACHMARK, SPOTLIGHT, PULSE, ARROW, BADGE)
└── client/
    └── components/
        ├── ActionManager.java     Undo/Redo 스택 관리 Command 패턴 매니저
        ├── ChangeTracker.java     키 기반 변경 상태 추적 (NOT_CHANGED/CHANGED/DELETED)
        ├── ConfirmDialog.java     범용 확인 다이얼로그 (headline + 옵션 버튼)
        ├── DiffPanel.java         변경사항 Diff 미리보기 패널 (before → after)
        ├── HighlightEffect.java   DOM 요소 강조 효과 (pulse 애니메이션)
        ├── OverlayContainer.java  오버레이 안내 컨테이너 (코치마크, 스포트라이트 등)
        ├── RbacGuard.java         RBAC 권한 검증 유틸리티 (읽기 전용 판단)
        ├── ScrollEffect.java      스크롤 + 도착 강조 효과
        └── ToastContainer.java    토스트 메시지 컨테이너
```

## 주요 컴포넌트

| 컴포넌트 | 설명 |
|----------|------|
| Action | 실행/롤백 인터페이스. 모든 편집 작업의 기본 단위 |
| ActionManager | Undo/Redo 스택 관리 (최대 100개, BehaviorSubject 상태 전파) |
| ChangeTracker | 키 기반 변경 상태 추적. document-ui(serial), type-ui(typeKey) 공용 |
| ToastContainer | 레벨별(INFO/SUCCESS/WARNING/ERROR) 토스트 메시지 표시 |
| ConfirmDialog | 사용자 확인이 필요한 작업에 대한 모달 다이얼로그 |
| DiffPanel | 변경 전/후 비교를 MD3 카드로 시각화 |
| HighlightEffect | CSS 선택자로 요소를 찾아 pulse 강조 |
| ScrollEffect | 대상 요소로 부드럽게 스크롤 후 강조 |
| OverlayContainer | 코치마크, 스포트라이트 등 안내 오버레이 |
| RbacGuard | RBAC 권한 검증 유틸리티. 사용자 역할 집합과 요구 역할을 비교하여 읽기 전용 여부 판단 |

## 의존성

- GWT 2.13.0
- Elemento, Sayaya UI/RX, Dagger

## 빌드

```bash
./gradlew :ui-components:build
./gradlew :ui-components:test
```
