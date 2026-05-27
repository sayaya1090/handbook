# Document 모듈

**에이전트 연동: 없음 (내부 전용).**

백엔드(Kotlin)와 프론트엔드(GWT)가 공유하는 문서 도메인 모델 및 핵심 로직을 관리하는 공용 도메인 모듈.

## 핵심 역할
- **공용 도메인 모델**: `DocumentValue`, `TypeInfo`, `AttributeInfo` 등 Jackson과 JsInterop을 모두 지원하는 데이터 모델 정의.
- **백엔드 로직**: `Document.kt` 엔티티 및 비즈니스 규칙 정의.
- **저장소 계약**: `DocumentRepository` 인터페이스를 통해 서버와 클라이언트 간의 데이터 접근 계층 통일.

## 주요 구성 요소
| 클래스 | 설명 |
|--------|------|
| **DocumentValue** | 문서 데이터 VO. `@JsType` 및 `@JsonProperty` 병기로 양방향 호환. |
| **DocumentRepository** | 문서 CUD 및 조회를 위한 포트 인터페이스. |
| **ValidationTask** | 문서 유효성 검증 상태 관리. |

## 개발 및 테스트
- **GWT 라이브러리**: JAR에 Java 소스를 포함하여 모든 GWT 모듈에서 상속 가능.
- **테스트 전략**: 
    - 백엔드: Kotest 기반의 도메인 규칙 검증.
    - 프론트엔드: `Application.java`와 `DocumentDomainTest.kt`를 통한 런타임 호환성 검증.
속 가능.
- **테스트 전략**: 
    - 백엔드: Kotest 기반의 도메인 규칙 검증.
    - 프론트엔드: `Application.java`와 `DocumentDomainTest.kt`를 통한 런타임 호환성 검증.
