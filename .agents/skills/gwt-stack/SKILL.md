---
name: gwt-stack
description: GWT 기술 스택 및 프론트엔드 개발 가이드
---

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

### 2. Native JsType 구현 규칙 (캡슐화된 네이티브 모델)
| 항목 | 표준 지침 | 사유 |
|------|----------|------|
| **필드 제어** | **`private`** 필드 + **`@JsProperty`** | 자바의 캡슐화를 유지하면서 네이티브 JS 객체의 속성에 매핑 |
| **Lombok 활용** | **`@Getter(onMethod_ = {@JsOverlay, @JsIgnore})`** | 네이티브 타입은 자바 메서드 바디를 가질 수 없으므로 `@JsOverlay`로 우회하여 플루언트 게터 제공 |
| **생성자** | 본문이 없는 기본 생성자만 허용 | 자바 인스턴스화가 아닌 JS 객체 캐스팅이 주 목적 |
| **로직 추가** | **`@JsOverlay`** 사용 | 자바 전용 메서드(팩토리, 비즈니스 규칙)를 안전하게 추가 |
| **타입 호환** | `Instant` → `double`, `UUID` → `String` | GWT/JS 표준 타입으로 치환하여 호환성 보장 |
| **상속 제한** | **인터페이스/클래스 상속 금지** | `isNative=true` 타입은 `Serializable`을 포함한 어떤 Java 타입도 상속/구현할 수 없음 |

---

## Part 1: GWT 환경 구축

### GWT 컴파일러 동작 원리
- **소스-투-소스 트랜스파일러**: Java 소스코드(.java)를 직접 분석하여 최적화된 JavaScript로 변환
- 바이트코드가 아닌 소스코드를 읽기 때문에 Lombok 사용 시 Java Agent 주입 필요 → sayaya-gwt 플러그인이 자동 처리
- 런타임 오버헤드, 플러그인, 애플릿 없음

### GWT 모듈 설정 (.gwt.xml)

```xml
<module rename-to="모듈명">
    <inherits name="com.google.gwt.user.User"/>
    <inherits name="org.jboss.elemento.Elemento"/>
    <inherits name="dagger.Dagger"/>
    <inherits name="dev.sayaya.Ui"/>    <!-- sayaya-ui MD3 컴포넌트 -->
    <inherits name="dev.sayaya.Rx"/>    <!-- RxJS 바인딩 -->
    <inherits name="dev.sayaya.handbook.AgentBridge"/> <!-- 브릿지 통신용 -->
    <entry-point class="...Application"/>
    <source path="client"/>
</module>
```

**주의:**
- `dev.sayaya.Ui` (대문자 U, 소문자 i) — `dev.sayaya.UI`는 에러
- 모듈 이름(`rename-to`)에 **하이픈 금지** (`_` 사용) — GWT 컴파일 에러.
- 라이브러리 JAR 빌드 시 **`from(sourceSets.main.get().allSource)`** 필수 (GWT는 소스를 읽음).

### 프로젝트 디렉토리 구조

```
src/
├── main/
│   ├── java/dev/sayaya/handbook/
│   │   ├── MyModule.gwt.xml       # GWT 모듈 정의
│   │   └── client/                # Java → JS 변환 대상
│   │       ├── domain/            # 도메인 규칙
│   │       ├── usecase/           # Store, Interactor
│   │       └── interfaces/        # UI 컴포넌트 (IsElement)
│   ├── i18n/                      # 다국어 파일 (language.ko.json 등)
│   └── webapp/
│       └── index.html
└── test/
    ├── java/dev/sayaya/handbook/
    │   ├── MyTest.gwt.xml         # 테스트 GWT 모듈
    │   └── client/                # 테스트 EntryPoint
    ├── kotlin/dev/sayaya/handbook/ # Kotest + Playwright 테스트
    └── webapp/
        └── mytest.html            # 테스트 HTML
```

### Gradle 빌드 설정 (sayaya-gwt)

```kotlin
plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")     // sayaya-gwt 플러그인
}
dependencies {
    implementation(libs.bundles.sayaya.web)  // ui + rx + elemento + lombok
    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.lombok)
    testImplementation(libs.bundles.test.web) // kotest + playwright
}
tasks {
    gwt {
        gwtVersion = "2.13.0"
        sourceLevel = "auto"
        modules = listOf("dev.sayaya.handbook.MyModule")
        war = file("src/main/webapp")
        devMode {
            modules = listOf("dev.sayaya.handbook.MyModule", "...TestModule")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler { strict = true }
    }
}
```

### DevMode
- `gradle gwtDevMode` 실행
- Java 소스 변경 → 저장 → 백그라운드 재컴파일 → 브라우저 새로고침(F5)으로 확인
- Source Maps 자동 구성 → 브라우저 DevTools에서 Java 레벨 디버깅 가능

---

## Part 2: Dagger 컴파일 타임 DI

### 핵심 원칙
1. **컴파일 타임 검증**: 어노테이션 프로세서가 빌드 시 의존성 그래프 검증 → 누락된 의존성 실행 전 발견
2. **제로 런타임 리플렉션**: 생성된 코드가 명시적 Java → GWT 트랜스파일과 호환.
3. **Composition Root**: 오직 EntryPoint에서만 컨테이너 생성 (`DaggerComponent.create()`).

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

### Composition Root (EntryPoint)
```java
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        component.shell().initialize();
        component.agent().initialize();
    }
}
```

### 계층형 모듈 구조 (Layered Modules)
의존성 성격에 따라 모듈을 분리하여 운영/테스트 환경 대응력을 높인다.
- **`StateModule`**: 비즈니스 상태(`Progress`, `Observable` 등) 싱글톤 제공.
- **`EventModule`**: 외부 시스템 브릿지(`MutationReceiver` 등) 제공.
- **`UiModule`**: 공용 UI 컴포넌트(`Toast`, `Dialog`) 제공.
- **`ApiModule`**: 실제 API 어댑터(`FetchApi`, `Repository`) 바인딩.
- **`ProductionModule` / `TestModule`**: 위 모듈들을 조합하여 최종 컴포넌트 구성.

### 초기화 패턴 (모든 UI 모듈 공통)
1. `DaggerComponent.create()` — 컨테이너 생성
2. `injectCss("css/module-ui.css")` — CSS 로드
3. 이벤트 핸들러 `.init()` — Observable 구독 시작 (데이터 주입 전 수행)
4. Window 브릿지 등록 — 모듈 간 통신
5. **DOM 마운트**: 절대 `body().add()`를 직접 사용하지 않고 `WindowRenderBridge.next(render)`를 통해 Shell 프레임에 마운트한다.

--- ## Part 5: 협업 및 동기화 전략

### 지능형 병합 (Smart Merge)
외부 이벤트를 통해 데이터를 갱신할 때 로컬 편집 본을 보존하며 서버 데이터와 병합하는 전략.

1. **상태 판별**: 영속화되지 않은 로컬 전용 작업물(Dirty state)을 식별하는 기준을 수립한다.
2. **병합 규칙**: 
   - 서버 데이터를 기본 베이스라인으로 삼는다.
   - 로컬 작업물 중 서버 데이터와 충돌하지 않는 항목만 선별하여 보존한다.
3. **일관성**: 객체의 동등성 비교(`equals/hashCode`) 로직을 강화하여 영속 전후의 객체를 동일하게 식별할 수 있어야 한다.

---

## Part 3: 반응형 상태 관리 (sayaya-rx)

### RxJS 연동 구조
- sayaya-rx = RxJS의 Observable/Subject/Operator를 JsInterop으로 래핑
- HTML 호스트에서 RxJS CDN 명시적 로드 필요: `<script src="rxjs.umd.min.js">`
- GWT 모듈: `<inherits name="dev.sayaya.Rx"/>`

### 5가지 Reactive 규율
1. **단일 Store 방출**: 상태는 오직 Store의 BehaviorSubject에서만 발생.
2. **Two-Door 패턴**: Store는 **읽기**(`.subscribe()`)와 **쓰기**(커맨드 메서드) 인터페이스만 노출. `@Delegate` 활용.
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

### IsElement 인터페이스 — 컴포넌트 합성

```java
public interface IsElement<E extends HTMLElement> {
    E element();
}
```

**@Delegate로 구현 (권장):**
```java
@Singleton
public class TodoToolbarElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    @Inject TodoToolbarElement(...) {
        container.css("todo-toolbar")
            .add(lblCnt)
            .add(btnAll);
    }
}
```

### 이벤트 처리 규칙
- **생성 시 한 번만 등록, 핸들러 내부에서 조건 처리** (동적 등록/제거 지양)

---

## Lombok 활용 및 JsInterop 요약

### @Delegate 활용 패턴 (MD3 컴포넌트 간소화)
모든 sayaya-ui 컴포넌트 빌더에 `@Delegate`를 적용하면 수동 조작을 줄이고 빌더 체인(`.disabled()`, `.text()` 등)을 바로 활용할 수 있다.

```java
@Singleton
public class UndoButton implements IsElement<HTMLElement> {
    @Delegate private final ButtonElementBuilder.TextButtonElementBuilder _this;
    @Inject UndoButton(ActionManager actionManager) {
        _this = ButtonElementBuilder.button().text().css("my-btn", "my-btn-undo");
        _this.onClick(e -> actionManager.undo());
    }
}
```

### JsInterop 어노테이션
- **`@JsType(isNative=true)`**: 네이티브 JS 객체 매핑 (필드는 캡슐화 후 `@JsProperty` 사용, 메서드는 `@JsOverlay`).
- **`@JsOverlay`**: 자바 전용 로직 추가 (`final` 필수, 인스턴스 메서드 재귀 호출 금지).
- **`@JsIgnore`**: JS 노출 제외.
- **`@JsProperty`**: Jackson의 `@JsonProperty`와 이름을 맞춰 속성명 고정.

---

## GWT 제약사항

| 제약 | 설명 | 우회 방법 |
|------|------|----------|
| Java record 지원 | GWT 2.13.0은 Java Record를 완벽히 지원하지 않음 | 일반 class 사용 + `@Builder` 등 활용 |
| JSNI `.catch()` | JS 예약어 | `['catch']()` 사용 |
| `@JsOverlay` 재귀 | ReferenceError 발생 가능성 | static 헬퍼로 우회 |
| GWT 모듈 이름 하이픈 | 컴파일 에러 | `_` 또는 camelCase 사용 |
| `String.format()` | GWT 미지원 | 직접 문자열 연결 |

---

## 테스트 패턴 (sayaya-gwt + Playwright)

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

## 이벤트 / 모듈 간 통신 패턴

### SSE → CustomEvent 브릿지
```java
// WindowToolPublisherBridge, WorkspaceEventListener 등: SSE 수신 → window CustomEvent 디스패치
DomGlobal.window.dispatchEvent(
    new CustomEvent<>("handbook-workspace-event", detail)
);
```

### 모듈 간 CustomEvent 수신
```java
DomGlobal.window.addEventListener("handbook-workspace-event", evt -> {
    CustomEvent<?> ce = Js.cast(evt);
    String data = Js.cast(ce.detail);
    if (data.startsWith("DOCUMENT_CREATED:")) {
        // 처리
    }
});
```