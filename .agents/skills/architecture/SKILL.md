# 아키텍처 상세

## 시스템 계층 구조

### 도메인 (공용 Java 라이브러리)
백엔드(JVM)와 프론트엔드(GWT)가 동일한 Java 소스를 공유하여 정합성을 보장하는 **Single Source of Truth (SSOT)** 계층.
- **workspace**: 워크스페이스·조직·권한
- **schema**: 타입 시스템·검증 규칙
- **document**: 문서 생명주기 및 데이터 모델
- **event**: 시스템 전반의 도메인 이벤트

### 프론트엔드 (GWT 2.13.0)
- **shell-ui**: SPA 프레임 (Drawer, MenuRail, ToolRail)
- **document-ui / type-ui / workspace-ui / login-ui**: 기능별 UI 모듈
- **ui-components**: 공유 컴포넌트 및 클라이언트 상태 도구
- **agent-bridge**: 모듈 간 자바스크립트 네이티브 브릿지

### 백엔드 (Spring Boot 4.0.1 + Kotlin 2.3.0)
- **gateway**: API 라우팅 및 메뉴 집계
- **command-services**: 도메인별 CUD 및 Kafka 이벤트 발행
- **query-services**: CQRS 읽기 전용 서비스 (Elasticsearch 등)
- **assistant**: AI 에이전트 오케스트레이션

## 핵심 설계 패턴

### 1. 캡슐화된 네이티브 모델 (Encapsulated Native Model)
공용 도메인 클래스에 적용되는 표준 패턴.
- **네이티브 호환성**: `@JsType(isNative = true)`를 사용하여 자바스크립트 객체와 제로 카피 연동.
- **객체지향 캡슐화**: 필드는 `private`으로 보호하고, `@Getter(onMethod_ = {@JsOverlay, @JsIgnore})`를 통해 자바 전용 게터(Fluent API) 제공.
- **로직 보호**: `@JsOverlay`를 사용하여 GWT 컴파일러 간섭 없이 비즈니스 규칙 및 팩토리 메서드 포함.

### 2. 제로 카피(Zero-copy) 데이터 교환
서버 응답 JSON을 별도의 DTO 매핑 없이 프론트엔드 도메인 객체로 즉시 캐스팅하여 사용함으로써 성능과 정합성 극대화.

### 3. Port & Adapter (Hexagonal)
도메인 모듈에 인터페이스(Port)를 정의하고, 각 플랫폼(Spring, GWT)에 맞는 어댑터를 구현하여 기술 중립적 설계 유지.
