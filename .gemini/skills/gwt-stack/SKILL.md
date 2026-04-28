# GWT 기술 스택 가이드

> 참고 시리즈: https://medium.com/@sayaya1090
> - [Part 0] A Backend Developer's Daydream
> - [Part 1] Frontend Development with Java: Building a Gradle and Modern GWT Environment
> - [Part 2] DI Container: Compile-time Dependency Injection in Frontend
> - [Part 3] GWT + RxJS via JsInterop: A Discipline-First Approach to UI State
> - [Part 4] Type-safe DOM: Declarative UI with Elemental2 and Elemento

---

## 기술 스택 구성

| 라이브러리 | 버전 | 역할 |
|-----------|------|------|
| GWT | 2.13.0 | Java → JavaScript 소스-투-소스 트랜스파일러 |
| sayaya-gwt | 2.2.7 | GWT Gradle 플러그인 (컴파일, DevMode, 테스트 인프라, Lombok 통합) |
| sayaya-ui | 2.4.1.3 | MD3 웹 컴포넌트 래퍼 (Select, Checkbox, Button 등) |
| sayaya-rx | 2.2.3 | RxJS BehaviorSubject/Observable GWT 바인딩 |
| Elemento | 2.x | 타입 안전 DOM 조작 빌더 (`div()`, `label()`, `body()` 등) |
| Elemental2 | - | 브라우저 API Java 타입 래퍼 (DOM, Console, Array 등) |
| Dagger | 2.59 | 컴파일 타임 DI (GWT 호환, 런타임 리플렉션 없음) |
| Lombok | 1.18.x | 보일러플레이트 제거 (`@Delegate`, `@Getter(onMethod_)`) |

---

## 공용 도메인 모델 (Shared Domain) 전략

백엔드(JVM)와 프론트엔드(GWT)가 동일한 Java 소스 파일을 공유하여 **단일 출처(SSOT)**를 유지하고 **제로 카피(Zero-copy)** 통신을 실현한다.

### 1. 설계 원칙
- **Java 단일화**: GWT의 Kotlin 지원 미흡을 고려하여 공용 도메인은 **Java**로 작성한다.
- **애너테이션 병기**: Jackson(`@JsonProperty`)과 JsInterop(`@JsType`) 애너테이션을 한 클래스에 공존시킨다.
- **네이티브 지향**: 프론트엔드 성능을 위해 `isNative = true` 설정을 우선시한다.

### 2. Native JsType 구현 규칙
| 항목 | 규칙 | 사유 |
|------|------|------|
| **필드 제어** | **`public`**으로 선언 | 네이티브 JS 객체의 속성에 직접 매핑되기 위해 필수 |
| **Lombok** | **사용 불가** (`@Getter`, `@Setter`) | 네이티브 타입은 자바 메서드 바디를 가질 수 없음 |
| **생성자** | 본문이 없는 기본 생성자만 허용 | 자바 인스턴스화가 아닌 JS 객체 캐스팅이 주 목적 |
| **로직 추가** | **`@JsOverlay`** 사용 | 자바 전용 메서드(팩토리, 비즈니스 규칙)를 안전하게 추가 |
| **타입 호환** | `Instant` → `double`, `UUID` → `String` | GWT/JS 표준 타입으로 치환하여 호환성 보장 |

---

## Part 1: GWT 환경 구축

### GWT 모듈 설정 (.gwt.xml)

```xml
<module rename-to="모듈명">
    <inherits name="com.google.gwt.user.User"/>
    <inherits name="org.jboss.elemento.Elemento"/>
    <inherits name="dagger.Dagger"/>
    <inherits name="dev.sayaya.Ui"/>    <!-- sayaya-ui MD3 컴포넌트 -->
    <inherits name="dev.sayaya.Rx"/>    <!-- RxJS 바인딩 -->
    <entry-point class="...Application"/>
    <source path="client"/>
</module>
```

**주의:**
- 모듈 이름(`rename-to`)에 **하이픈 금지** (`_` 사용) — GWT 컴파일 에러.
- 라이브러리 JAR 빌드 시 **`from(sourceSets.main.get().allSource)`** 필수 (GWT는 소스를 읽음).

---

## Part 2: Dagger 컴파일 타임 DI

### 핵심 원칙
- **제로 런타임 리플렉션**: 생성된 코드가 명시적 Java → GWT 트랜스파일과 호환.
- **Composition Root**: 오직 EntryPoint에서만 컨테이너 생성 (`DaggerComponent.create()`).

### @Inject 생성자 주입 예시
```java
@Singleton
public class ClearCompletedButton extends TodoActionButton {
    @Inject ClearCompletedButton(TodoListStore listStore) {
        super("Clear Completed");
        on(EventType.click, evt -> listStore.clearCompleted());
    }
}
```

---

## Part 3: 반응형 상태 관리 (sayaya-rx)

### 5가지 Reactive 규율
1. **단일 Store 방출**: 상태는 오직 Store의 BehaviorSubject에서만 발생.
2. **Two-Door 패턴**: Store는 **읽기**(`.subscribe()`)와 **쓰기**(커맨드 메서드) 인터페이스만 노출.
3. **연산자 체인 제한**: `map`, `filter`, `distinctUntilChanged` 위주 사용.
4. **경계에서만 subscribe**: 컴포넌트 마운트 시점에만 구독.
5. **반드시 구독 해제**: `Subscription.unsubscribe()` 호출로 메모리 누수 방지.

---

## Part 4: 타입 안전 DOM (Elemental2 + Elemento)

### Elemento — 선언적 DOM 빌더
```java
import static org.jboss.elemento.Elements.*;

HTMLDivElement container = div().css("container")
    .add(h1().text("Title"))
    .add(ul().css("todo-list"))
    .element();
```

---

## 정석적인 런타임 테스트 구조

GWT 테스트는 컴파일된 JS와 브라우저 환경의 상호작용을 검증해야 하므로 **로그 기반 자동화** 방식이 가장 안정적이다.

### 1. 테스트 EntryPoint (`Application.java`)
`onModuleLoad`에서 테스트 시나리오를 자동으로 실행하고 결과를 `console.log`로 출력한다.

```java
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        MyDomain d = MyDomain.create("id-1", "test");
        console.log("LOG_RESULT:" + d.isValid());
        console.log("ACTIVITY_TEST_READY");
    }
}
```

### 2. Playwright 검증 (`GwtTestSpec`)
코틀린 테스트 코드는 브라우저 로그를 가로채서 기대값과 일치하는지 확인한다.

```kotlin
@GwtHtml("my_test.html")
class MyTest : GwtTestSpec({
    beforeTest { Thread.sleep(1000) } // 로딩 대기
    Then("도메인 로직 검증") {
        page shouldContainLog "LOG_RESULT:true"
    }
})
```

---

## Lombok 활용 및 JsInterop 요약

### @Delegate 활용 패턴
- **BehaviorSubject 래핑**: `next()`는 숨기고 `Observable` 메서드만 노출.
- **IsElement 위임**: `element()`, `add()`, `css()` 등 빌더 메서드 자동 위임.

### JsInterop 어노테이션
- **`@JsType(isNative=true)`**: 네이티브 JS 객체 매핑 (필드는 `public` 필수).
- **`@JsOverlay`**: 자바 전용 로직 추가 (`final` 메서드만 가능).
- **`@JsIgnore`**: JS 노출 제외 (오버로드된 생성자 등).
- **`@JsProperty`**: Jackson의 `@JsonProperty`와 이름을 맞춰 속성명 고정.
