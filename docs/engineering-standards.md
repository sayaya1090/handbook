# HandBook 엔지니어링 표준 및 설계 교훈 (2026-04-28)

이 문서는 프로젝트의 대규모 도메인 통합 및 공통 모듈 정리 과정을 통해 확립된 핵심 설계 원칙을 기록합니다.

---

22## 1. 기술 스택 표준 (2026-04-28 정립)

### 1.1 서버 사이드
- **Kotlin 2.3.0**: 코루틴 및 리액티브 프로그래밍 최적화
- **Spring Boot 4.0.1**: 가상 스레드(Project Loom) 기본 활용 및 Spring Cloud 2025.x 연동
- **Jackson 3**: `tools.jackson.databind.*`, `tools.jackson.core.*`, `tools.jackson.module.kotlin.*` 패키지 사용.
  - 주의: Jackson 3에서는 `ObjectMapper` 생성 후 뮤테이션이 금지되므로 `JsonMapper.builder()` 패턴을 강제한다.
  - 어노테이션은 `com.fasterxml.jackson.annotation.*`을 유지한다.
- **R2DBC**: 리액티브 드라이버를 통한 논블로킹 DB 접근

### 1.2 클라이언트 사이드
- **GWT 2.13.0**: Java 21+ 호환성 및 Elemental2 활용
- **Material Design 3**: MD3 디자인 시스템 및 토큰 준수
- **Playwright**: 헤드리스 브라우저 기반 UI 테스트 자동화

---

## 2. 공용 도메인 모델 (Shared Domain) 전략

- **SSOT(Single Source of Truth)**: 백엔드(JVM)와 프론트엔드(GWT)가 하나의 Java 소스를 공유한다.
- **캡슐화된 네이티브 모델**: 
    - `@JsType(isNative = true)` 사용.
    - 필드는 `private`으로 캡슐화하고 `@JsProperty`로 노출.
    - 자바 측 접근은 `@Getter(onMethod_ = {@JsOverlay, @JsIgnore})`를 사용하여 플루언트 API(`id()`)를 제공.
- **제로 카피(Zero-copy)**: 데이터 변환 없이 JSON을 직접 도메인 객체로 캐스팅하여 사용한다.

---

## 3. Native JsInterop (isNative = true) 적용 원칙

공용 도메인 모델에 `isNative = true` 설정이 적용되는 이유는 경계를 넘나드는 데이터 교환의 정합성을 보장하기 위함이다.

### 구현 지침 (캡슐화된 네이티브 모델)
| 항목 | 표준 지침 | 이유 |
|------|----------|------|
| **필드 접근** | **`private`** 필드 + **`@JsProperty`** | 자바의 캡슐화를 유지하면서 JS 객체 속성에 매핑 |
| **Lombok 사용** | **`@Getter(onMethod_ = {@JsOverlay, @JsIgnore})`** | 자바 코드에서 플루언트 게터(`id()`) 사용 가능 |
| **생성자** | 본문이 없는 기본 생성자만 허용 | 자바 인스턴스화가 아닌 JS 객체 캐스팅이 주 목적 |
| **로직 보호** | 모든 자바 메서드는 **`@JsOverlay`** 필수 | 네이티브 타입의 자바 메서드 바디 제한 우회 |

### 예시 코드 (Encapsulated Standard Pattern)
```java
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class MyDomain {
    @JsonProperty("id") @JsProperty private String id;

    @JsOverlay @JsIgnore
    public static MyDomain create(String id) {
        MyDomain instance = new MyDomain();
        instance.id = id;
        return instance;
    }
}
```

---

## 4. Dagger DI 및 상태 관리 표준
- **Store 기반 상태 관리**: `BehaviorSubject`를 전용 `Store` 클래스로 캡슐화한다.
- **인터페이스 기반 주입**: 컴포넌트에는 구체 Store가 아닌 `Observable`(읽기) 및 `Observer`(쓰기) 인터페이스를 주입한다.
- **Composition Root**: 모든 컨테이너 생성은 EntryPoint에서만 수행한다.
