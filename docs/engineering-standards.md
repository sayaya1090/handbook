# HandBook 엔지니어링 표준 및 설계 교훈 (2026-04-28)

이 문서는 프로젝트의 대규모 도메인 통합 및 공통 모듈 정리 과정을 통해 확립된 핵심 설계 원칙을 기록합니다.

---

## 1. 기술 스택 표준 (2026-04-28 정립)

### 1.1 서버 사이드
- **Kotlin 2.3.0**: 코루틴 및 리액티브 프로그래밍 최적화
- **Spring Boot 4.0.1**: Jakarta EE 11 및 Java 21+ 최적화
- **Jackson 3**: Spring Boot 4의 기본 직렬화 도구. 패키지명이 `tools.jackson.*`으로 변경되었음에 유의한다.
  - 신규 코드는 `tools.jackson.databind.*`, `tools.jackson.core.*` 패키지를 사용한다.
  - 애너테이션은 하위 호환성을 위해 `com.fasterxml.jackson.annotation.*`을 유지한다.
- **R2DBC**: 리액티브 드라이버를 통한 논블로킹 DB 접근

### 1.2 클라이언트 사이드
- **GWT 2.13.0**: Java 21+ 호환성 및 Elemental2 활용
- **Material Design 3**: MD3 디자인 시스템 및 토큰 준수
- **Playwright**: 헤드리스 브라우저 기반 UI 테스트 자동화

### 1.3 데이터 및 메시징
- **PostgreSQL 17+**: JSONB 및 리액티브 연동
- **Kafka**: Spring Cloud Stream 기반 이벤트 소싱
- **Elasticsearch 9.3.3**: 전문 검색 및 복합 필터링 최적화

---

## 2. 공용 도메인 모델 (Shared Domain) 전략

백엔드(JVM)와 프론트엔드(GWT)가 동일한 Java 소스 파일을 공유하여 **단일 출처(SSOT)**를 유지하고 **제로 카피(Zero-copy)** 통신을 실현한다.

- **Java 단일화**: GWT의 Kotlin 지원 미흡을 고려하여 공용 도메인은 **Java**로 작성한다.
- **애너테이션 병기**: Jackson(`@JsonProperty`)과 JsInterop(`@JsType`) 애너테이션을 한 클래스에 공존시킨다.
- **제로 카피(Zero-copy)**: 서버 응답 JSON을 프론트엔드에서 데이터 변환 없이 직접 도메인 객체로 캐스팅하여 사용한다.

---

## 2. Native JsInterop (isNative = true) 적용 원칙

공용 도메인 모델에 `isNative = true` 설정을 적용하는 이유는 경계를 넘나드는 데이터 교환의 정합성을 확보하기 위함이다.

### 구현 지침 (캡슐화된 네이티브 모델)
| 항목 | 표준 지침 | 이유 |
|------|----------|------|
| **필드 접근** | **`private`** 필드 + **`@JsProperty`** | 자바의 캡슐화를 유지하면서 JS 객체 속성에 매핑 |
| **Lombok 활용** | **`@Getter(onMethod_ = {@JsOverlay, @JsIgnore})`** | 자바 코드에서 플루언트 게터(`id()`) 사용 가능 |
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

## 3. Dagger DI 및 상태 관리 표준

### 3.1 컴파일 타임 검증 원칙
- **제로 런타임 리플렉션**: 런타임에 리플렉션 비용을 없애기 위해 Dagger의 컴파일 타임 DI를 활용한다.
- **Composition Root**: 모든 컨테이너(`DaggerComponent`) 생성은 오직 `EntryPoint`(`Application.java` 등)에서만 1회 수행되어야 한다. 컴포넌트 내부에서 컨테이너를 직접 생성해서는 안 된다.

### 3.2 5가지 Reactive 규율 (sayaya-rx)
상태 전파의 복잡성을 줄이기 위해 다음 규율을 준수한다.
1. **단일 Store 방출**: 상태는 오직 `Store` 클래스의 `BehaviorSubject`에서만 발생해야 한다.
2. **Two-Door 패턴**: Store는 읽기(`Observable.subscribe()`)와 쓰기(커맨드 메서드) 인터페이스만 노출하며, `@Delegate`를 통해 내부 Subject 구현을 은닉한다.
3. **연산자 체인 제한**: `map` (변환), `filter` (조건), `distinctUntilChanged` (중복 방지) 위주로 사용하며 과도한 파이프라인은 지양한다.
4. **경계에서만 subscribe**: 화면 마운트 시 등 명확한 라이프사이클 경계에서만 구독한다.
5. **반드시 구독 해제**: `Subscription.unsubscribe()`를 통해 메모리 누수를 방지한다.

### 3.3 GWT UI 테스트 표준 (Playwright)
- **비동기 렌더링 대응**: GWT 모듈의 로딩 및 초기화 지연으로 인해 `querySelector`는 `null`을 반환할 위험이 높다. 반드시 **`page.waitForSelector(".selector")`**를 사용하여 요소가 나타날 때까지 대기한 후 검증을 수행한다.
- **빌드 구성 누락 방지**: 신규 테스트용 GWT 모듈(`*Test.gwt.xml`) 추가 시, 해당 프로젝트의 `build.gradle.kts` 내 `gwt { devMode { modules = [...] } }` 목록에 반드시 명시하여 컴파일 대상에 포함시킨다.

---

## 4. UI 렌더링 및 통신 표준

### 4.1 Shell Frame Mount 계약
`Application.onModuleLoad()`에서 절대 `body().add(container)`를 직접 호출해서는 안 된다. 이는 전역 CSS에 의해 뷰포트 밖으로 요소가 밀려나는 회귀 버그를 유발한다. 반드시 다음과 같이 `WindowRenderBridge`를 사용해야 한다.
```java
Render render = frame -> {
    frame.append(container.element());
    return true;
};
WindowRenderBridge.next(render);
```

### 4.2 이벤트 및 DOM 규칙
- **선언적 DOM (Elemento)**: HTML 문자열이나 네이티브 HTML 조작 대신 `div()`, `button()` 등 Elemento 빌더와 `sayaya-ui` MD3 컴포넌트 래퍼를 사용한다.
- **이벤트 1회 등록**: 이벤트 리스너는 동적으로 여러 번 달고 제거하지 않으며, 초기 생성 시 1회만 등록하고 핸들러 내부에서 조건문으로 동작을 제어한다.
- **모듈 간 통신**: 모듈 간 통신은 SSE 메시지를 `WindowToolPublisherBridge` 등이 수신하여 `DomGlobal.window.dispatchEvent(CustomEvent)` 형태로 브로드캐스트하는 방식을 따른다.

---

## 5. JVM 환경에서의 GWT 네이티브 객체 호환성 (Proxy 패턴)

공용 도메인이 GWT 전용 네이티브 인터페이스(`JsPropertyMap` 등)를 포함할 경우, 백엔드(JVM) 환경에서 `UnsatisfiedLinkError`가 발생한다. 이를 방지하기 위해 **Java Reflection Proxy**를 사용하여 JVM용 가짜 객체를 주입한다.

- **Proxy 패턴**: `java.lang.reflect.Proxy`를 사용하여 인터페이스의 가짜 구현체를 생성하고, 내부적으로는 Java/Kotlin의 `Map`에 위임한다.
- **Jackson 호환성**: Proxy가 `Map` 인터페이스도 함께 구현하도록 하여 Jackson이 JSON 직렬화 시 값을 정상적으로 추출할 수 있게 한다.
- **구현 예시**:
```kotlin
val map = java.lang.reflect.Proxy.newProxyInstance(
    JsPropertyMap::class.java.classLoader,
    arrayOf(JsPropertyMap::class.java, Map::class.java)
) { _, method, args ->
    if (method.declaringClass == Map::class.java) {
        method.invoke(dataMap, *(args ?: emptyArray()))
    } else when (method.name) {
        "get" -> dataMap[args[0] as String]
        else -> null
    }
} as JsPropertyMap<String>
```
