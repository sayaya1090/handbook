# Schema Domain 모듈

GWT 기반의 타입 스키마 도메인 모델 및 저장소 인터페이스를 제공하는 라이브러리.

## 역할 및 책임
- **도메인 모델**: `TypeValue`, `AttributeValue`, `AttributeTypeValue`, `Position` 등 타입 시스템 관련 JsInterop 객체 정의.
- **저장소 포트**: `TypeRepository`, `LayoutRepository` 인터페이스 정의.
- **클라이언트 상태**: 레이아웃 기간(`LayoutPeriod`) 및 좌표 정보 관리.

## 에이전트 연동
**에이전트 연동: 없음 (내부 전용).**
프론트엔드 모듈(`type-ui`, `document-ui`) 내에서 도메인 모델로만 사용됨.

## 의존성
- `activity`: 기본 UI 도메인 및 i18n
- `sayaya-ui`, `sayaya-rx`: MD3 컴포넌트 및 RxJS 연동
