# Workspace Domain 모듈

GWT 기반의 워크스페이스 도메인 모델 및 API 인터페이스를 제공하는 라이브러리.

## 역할 및 책임
- **도메인 모델**: `Workspace`, `User`, `Group` 등 워크스페이스 구조 관련 JsInterop 객체 정의.
- **API 포트**: `WorkspaceApi` 인터페이스 정의.

## 에이전트 연동
**에이전트 연동: 없음 (내부 전용).**
프론트엔드 모듈(`shell-ui`, `onboarding-ui`, `workspace-ui`) 내에서 도메인 모델로만 사용됨.

## 의존성
- `activity`: 기본 UI 도메인 및 i18n
- `sayaya-ui`, `sayaya-rx`: MD3 컴포넌트 및 RxJS 연동
