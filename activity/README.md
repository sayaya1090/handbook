# Activity 모듈

GWT 클라이언트 모듈 간 공유되는 핵심 도메인 클래스, 공통 유스케이스, 그리고 런타임 공유 브릿지를 정의하는 라이브러리.
Shell UI와 모든 기능 모듈(타입·문서 에디터 등)의 공통 기반이 된다.

## 핵심 역할
- **공용 언어(Domain)**: 애플리케이션 전반에서 사용하는 `Menu`, `Tool`, `Labels`, `Progress` 등 핵심 모델 정의.
- **런타임 공유(Bridge)**: 서로 다른 GWT 컴파일 단위(IFrame) 간의 데이터 및 함수 공유를 위한 `window` 객체 기반 브릿지 제공.
- **공통 서비스(Usecase)**: 다국어 제공(`LabelProvider`), 화면 크기 감지(`ViewportObserver`), 사용자 설정(`UserPreferences`) 관리.

## 주요 구성 요소

### 1. 도메인 (Domain)
| 클래스 | 설명 |
|--------|------|
| **Menu** | 네비게이션 메뉴 정보. 세션 상태별 노출 제어 로직(`isAllowedFor`) 포함. |
| **Tool** | 메뉴 내 액션 도구. 비동기 실행을 위한 `ToolFunction` 포함. |
| **Labels** | i18n 라벨 맵. 런타임에 JS 객체로 직접 주입 및 조회(`getOrDefault`) 가능. |
| **Progress** | 전역 진행률 표시 데이터 구조. |
| **Render** | 모듈 간 렌더링 위임을 위한 콜백 인터페이스. |

### 2. 브릿지 (Bridge/Sharing)
| 클래스 | 설명 |
|--------|------|
| **ToolPublisher** | 자식 모듈의 도구 목록을 부모 쉘 툴바에 노출. |
| **ToolSubscriber** | 쉘에서 선택된 도구 실행 이벤트를 자식 모듈로 전달. |
| **RenderSharing** | 자식 모듈의 렌더링 함수를 쉘의 프레임 관리자(`FrameUpdater`)로 전달. |
| **ProgressSharing** | 모듈별 작업 진행 상태를 전역 프로그레스 바에 공유. |
| **LabelSharing** | 쉘이 로드한 다국어 자원을 모든 모듈이 공유. |
| **UriSharing** | 클린 URL 기반 내비게이션 요청 공유. |

### 3. 유스케이스 (Usecase)
| 클래스 | 설명 |
|--------|------|
| **LabelProvider** | 언어 감지 및 언어팩 로드, 실시간 레이블 구독 제공. |
| **ViewportObserver** | 창 크기 변화를 감지하여 모바일/데스크톱 모드 발행. |
| **UserPreferences** | localStorage 연동 테마/언어 설정 저장소. |

## 에이전트 연동
**에이전트 연동: 없음 (내부 전용).**
도구 노출 및 상태 공유의 기반을 제공하나, 에이전트 전용 커맨드 브릿지는 [agent-bridge](../agent-bridge/README.md) 모듈에서 담당한다.

## 개발 및 테스트
- **빌드**: JAR에 Java 소스를 포함하여 GWT 컴파일러 호환성 유지.
- **테스트 전략**: 
    - `Application.java`(GWT)에서 실제 브릿지 및 도메인 로직을 수행하고 로그를 출력.
    - `BridgeSharingTest.kt`(Kotlin/Playwright)에서 브라우저 로그를 검증하는 **정석적인 런타임 테스트** 수행.

## 의존성
- `dev.sayaya:sayaya-ui`: MD3 디자인 시스템 및 컴포넌트
- `dev.sayaya:sayaya-rx`: RxJS GWT 래퍼
- `org.jboss.elemento:elemento-core`: GWT DOM 빌더 유틸리티
