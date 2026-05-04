# Schema 모듈

**에이전트 연동: 없음 (내부 전용).**

백엔드(Kotlin)와 프론트엔드(GWT)가 공유하는 타입 시스템 도메인 모델 및 검증 로직을 관리하는 공용 도메인 모듈.

## 핵심 역할
- **타입 정의(SSOT)**: `Type`, `Attribute`, `AttributeType` 등 시스템 전반에서 통용되는 타입 스키마 명세 정의.
- **제로 카피(Zero-copy)**: Jackson과 JsInterop 설정을 병기하여 서버와 클라이언트 간 데이터 변환 오버헤드 제거.
- **레이아웃 관리**: 타입 캔버스에서의 시각적 위치(`Position`, `TypeLayout`) 정보 정의.

## 주요 구성 요소
| 클래스 | 설명 |
|--------|------|
| **Type** | 최상위 타입 명세. 속성 목록 및 캔버스 크기 포함. |
| **Attribute** | 개별 속성 정의. 이름, 순서, 데이터 타입을 가짐. |
| **AttributeType** | 데이터 타입 명세. 복합 타입(Array, Map, Document) 및 단순화(`simplify`) 로직 포함. |
| **Validator** | 다형성 기반 데이터 검증기 명세 (Text, Number 등). |
| **LayoutPeriod** | 레이아웃의 유효 기간 및 중첩 계산(`overlap`) 로직 포함. |

## 에이전트 연동
**에이전트 연동: 없음 (내부 전용).**
타입 편집기 및 문서 구조 분석의 기반 데이터 모델로 사용됨.

## 개발 및 테스트
- **GWT 라이브러리**: JAR에 Java 소스를 포함하여 모든 GWT 모듈에서 상속 가능.
- **테스트 전략**: 
    - 백엔드: Kotest 기반의 도메인 규칙 및 직렬화 검증.
    - 프론트엔드: `Application.java`와 `SchemaDomainTest.kt`를 통한 런타임 호환성 및 비즈니스 로직(overlap, simplify) 검증.
