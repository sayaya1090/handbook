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
    <entry-point class="...Application"/>
    <source path="client"/>
</module>
```

**주의:**
- `dev.sayaya.Ui` (대문자 U, 소문자 i) — `dev.sayaya.UI`는 에러
- `dev.sayaya.Rx` — BehaviorSubject 사용 시 필수
- 모듈 이름(`rename-to`)에 **하이픈 금지** — GWT 컴파일 에러
- `<inherits>` 누락 시 컴파일 에러 ("module must be inherited")

### 프로젝트 디렉토리 구조

```
src/
├── main/
│   ├── java/dev/sayaya/handbook/
│   │   ├── MyModule.gwt.xml       # GWT 모듈 정의
│   │   └── client/                # Java → JS 변환 대상
│   │       ├── domain/            # 도메인 규칙 (Record, 검증)
│   │       ├── usecase/           # Store, Interactor
│   │       └── interfaces/        # UI 컴포넌트 (IsElement)
│   ├── i18n/                      # 다국어 파일 (language.ko.json 등)
│   └── webapp/
│       └── index.html
└── test/
    ├── java/dev/sayaya/handbook/
    │   ├── MyTest.gwt.xml         # 테스트 GWT 모듈
    │   └── client/                # 테스트 EntryPoint
    ├── kotlin/dev/sayaya/handbook/ # Kotest 테스트
    └── webapp/
        └── mytest.html            # Playwright 테스트 HTML
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
        sourceLevel = "auto"    // Java 17/21/24 자동 감지
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

### 플러그인 구조 (sayaya-gwt)
```
src/main/kotlin/dev/sayaya/gwt/
├── GwtPlugin.kt           — 기본 GWT 플러그인 (org.docstr.gwt 확장)
├── GwtTestPlugin.kt       — 테스트 인프라 (HTML 생성, 웹서버, 컴파일)
├── GwtTestCompileTask.kt  — GWT 테스트 소스 컴파일 태스크
├── GwtGenerateTestHtmlTask.kt — 테스트 HTML 호스트 자동 생성
├── WebServerService.kt    — Ktor 내장 웹서버 (BuildService, 동적 포트)
├── GwtLombokPlugin.kt     — Lombok 어노테이션 프로세서 통합 (Java Agent 자동 주입)
└── GwtPluginUtils.kt      — Jakarta Servlet 충돌 방지 유틸
```

---

## Part 2: Dagger 컴파일 타임 DI

### 핵심 원칙
1. **컴파일 타임 검증**: 어노테이션 프로세서가 빌드 시 의존성 그래프 검증 → 누락된 의존성 실행 전 발견
2. **제로 런타임 리플렉션**: 생성된 코드가 명시적 Java → GWT 트랜스파일/트리셰이킹과 호환
3. **Composition Root**: 오직 EntryPoint에서만 컨테이너 생성 → 나머지는 주입받기만

### @Component 정의
```java
@Singleton
@dagger.Component(modules = { MyModule.class, ApiModule.class })
public interface Component {
    ShellInitializer shell();
    AgentInitializer agent();
    // 테스트용 static 팩토리
    static Component create() { return DaggerComponent.create(); }
}
```

### @Module + @Provides / @Binds
```java
@Module
public class MyModule {
    @Provides @Singleton
    static SomePort somePort() { return new SomeAdapter(); }

    @Binds
    abstract FrameContainer bindFrame(ContentElement impl);
}
```

### @Inject 생성자 주입
```java
@Singleton
public class ClearCompletedButton extends TodoActionButton {
    @Inject ClearCompletedButton(TodoListStore listStore) {
        super("Clear Completed");
        on(EventType.click, evt -> listStore.clearCompleted());
    }
}
```

### @AssistedFactory / @AssistedInject — 런타임 파라미터
```java
// 정적 의존성은 DI, 런타임 값은 팩토리 파라미터
class StateFilterButton extends TodoActionButton {
    @AssistedFactory
    interface FilterButtonFactory {
        StateFilterButton create(@Assisted String label, @Assisted State value);
    }
    @AssistedInject
    StateFilterButton(@Assisted String label, @Assisted State value, StateFilter filter) {
        super(label);
    }
}

// 사용 측
@Inject TodoToolbarElement(StateFilterButton.FilterButtonFactory buttonFactory, ...) {
    var btnAll = buttonFactory.create("All", null);
    var btnActive = buttonFactory.create("Active", State.ACTIVE);
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

### 초기화 패턴 (모든 UI 모듈 공통)
1. `DaggerComponent.create()` — 컨테이너 생성
2. `injectCss("css/module-ui.css")` — CSS 로드
3. 이벤트 핸들러 `.init()` — Observable 구독 시작
4. Window 브릿지 등록 — 모듈 간 통신
5. DOM 빌드 (Elemento DSL) → `body().add(container)`

### 환경별 그래프 (테스트)
```java
// 테스트 전용 Component — 최소 의존성
@Singleton
@dagger.Component
interface TestComponent {
    TodoListStore store();
    static TestComponent create() { return DaggerTestComponent.create(); }
}
```

---

## Part 3: 반응형 상태 관리 (sayaya-rx)

### RxJS 연동 구조
- sayaya-rx = RxJS의 Observable/Subject/Operator를 JsInterop으로 래핑
- HTML 호스트에서 RxJS CDN 명시적 로드 필요: `<script src="rxjs.umd.min.js">`
- GWT 모듈: `<inherits name="dev.sayaya.Rx"/>`

### BehaviorSubject — 상태의 단일 출처
```java
// 생성 — 최신 값 캐싱 + 구독 시 즉시 현재 값 방출
BehaviorSubject<String> subject = BehaviorSubject.behavior("initial");
subject.next("new value");       // 값 발행
subject.getValue();               // 현재 값
subject.subscribe(val -> ...);    // 구독 (즉시 현재 값 수신)
subject.asObservable();           // 읽기 전용 변환

// map + subscribe 체인
subject.map(val -> val.toUpperCase())
       .subscribe(label::textContent);
```

### 5가지 Reactive 규율

**규율 1: 단일 Store 방출**
- 상태는 오직 Store의 BehaviorSubject에서만 발생
- UI 컴포넌트가 직접 상태 컬렉션을 생성/조작하지 않음

**규율 2: Two-Door 패턴**
- Store는 정확히 두 인터페이스를 노출: **읽기** (`.subscribe()`) + **쓰기** (커맨드 메서드)

```java
@Singleton
public class TodoListStore {
    private final BehaviorSubject<JsArray<TodoStore>> _this = behavior(new JsArray<>());
    @Delegate private final Observable<JsArray<TodoStore>> observable = _this;
    // @Delegate로 Observable 메서드만 노출 → 외부에서 .next() 불가

    // 상태 변경은 커맨드 메서드로만
    public void add(Todo todo) { ... }
    public void remove(TodoStore store) { ... }
}
```

**규율 3: 연산자 체인 제한**
- 권장 연산자: `map` (변환), `filter` (필터링), `distinctUntilChanged` (중복 방지)
- 긴 파이프라인은 Store/UseCase 레이어의 명명된 함수로 추출

**규율 4: 경계에서만 subscribe**
- 컴포넌트 생성/마운트 시점, 앱 부트스트랩에서만 구독
- 다른 곳에서는 구독 금지

**규율 5: 반드시 구독 해제**
```java
public void remove() {
    element().remove();       // 1. DOM에서 제거
    dispose();                // 2. 구독 해제
    todo.remove();            // 3. Store에서 제거
}
private void dispose() {
    subscriptions.forEach(Subscription::unsubscribe);
    subscriptions.clear();
}
```
- 해제하지 않으면: 같은 화면 재오픈 시 이중 처리, 제거된 컴포넌트의 렌더링 지속, 메모리 누수

### Lombok @Delegate 활용 패턴

**패턴 A: BehaviorSubject 래퍼 (상태 제공자)**
```java
@Singleton
public class MenuList {
    @Delegate
    private final BehaviorSubject<List<Menu>> _this = behavior(List.of());
    @Inject MenuList() {}
}
// 사용: menuList.subscribe(items -> ...); menuList.next(newList);
```

**패턴 B: Observable만 노출 (읽기 전용)**
```java
@Singleton
public class TodoListStore {
    private final BehaviorSubject<JsArray<TodoStore>> _this = behavior(new JsArray<>());
    @Delegate private final Observable<JsArray<TodoStore>> observable = _this;
    // 외부에서는 subscribe만 가능, next는 Store 내부 커맨드로만
}
```

---

## Part 4: 타입 안전 DOM (Elemental2 + Elemento)

### Elemental2 — 브라우저 API 직접 매핑
```java
// 1:1 JavaScript 매핑 — 제로 런타임 오버헤드
var div = DomGlobal.document.createElement("div");
div.textContent = "Hello, GWT!";
div.addEventListener("click", evt -> console.log("Clicked!"));
```

### Elemento — 선언적 DOM 빌더
```java
import static org.jboss.elemento.Elements.*;

// 간결한 빌더 패턴
HTMLDivElement container = div()
    .css("container")
    .add(h1().text("Title"))
    .add(ul().css("todo-list"))
    .element();

// body에 추가
body().add(
    div().css("app")
        .add(component.controller())
        .add(component.canvas())
);
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
    // Lombok이 element() 등 모든 메서드를 자동 위임
    @Inject TodoToolbarElement(...) {
        container.css("todo-toolbar")
            .add(lblCnt)
            .add(btnAll);
    }
}
```

**수동 구현:**
```java
public class TypeElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    @Override public HTMLDivElement element() { return root; }
}
```

### 이벤트 처리 규칙

**규칙: 생성 시 한 번만 등록, 핸들러 내부에서 조건 처리**
```java
// ✅ Good: 한 번 등록, 조건은 핸들러 내부
button().text("Delete")
    .onClick(evt -> {
        if (canRemove()) remove();
    });

// ❌ Bad: 동적으로 리스너 추가/제거
if (isEnabled) button.addEventListener("click", handler);
```

**키보드 이벤트:**
```java
input().onKeyDown(evt -> {
    if ("Enter".equals(evt.key)) submitForm();
    else if ("Escape".equals(evt.key)) clearForm();
});
```

### 컴포넌트 재사용 캐싱
```java
// ✅ Good: Map으로 컴포넌트 캐싱 — DOM만 재배치
private final Map<TodoStore, TodoCardElement> cards = new HashMap<>();
private void update(JsArray<TodoStore> todos) {
    ul.element().textContent = "";
    todos.map((todo, idx) -> cards.computeIfAbsent(todo, factory::create))
         .forEach((child, idx) -> ul.add(child));
}
```

### Builder vs Element 구분
- **Builder** (`HTMLContainerBuilder`): 선언적 DOM 조립 (메서드 체이닝) → `.add()`, `.css()`, `.text()`
- **Element** (`.element()`): 실제 DOM 객체 직접 제어 → `classList`, `dataset`, `setAttribute`

```java
var item = li().css("todo-item");              // Builder
item.element().classList.toggle("active");      // Element 직접 접근
item.element().dataset.set("id", "123");        // HTML5 Dataset
item.element().setAttribute("aria-label", "...");
```

### Elemental2 JavaScript 내장 타입
```java
// JsArray<T> — java.util.List 대신 사용
JsArray<String> arr = new JsArray<>();
arr.push("item");
arr.forEach((item, idx) -> console.log(item));
arr.map((item, idx) -> item.length());

// JsPropertyMap<T> — 동적 객체
JsPropertyMap<Object> config = Js.asPropertyMap(new Object());
config.set("key", "value");

// Promise<T>
DomGlobal.fetch("/api/data").then(r -> r.json()).then(data -> { ... });

// 타이머
Global.setTimeout(() -> ..., 1000);
Global.setInterval(() -> ..., 1000);

// JSON
String json = JSON.stringify(obj);
JsPropertyMap<Object> parsed = Js.cast(JSON.parse(json));
```

### React/Vue 상호운용
- **GWT → React**: GWT가 팩토리 함수(`HTMLElement` 반환)를 글로벌 노출 → React가 ref 컨테이너에 추가
- **React → GWT**: GWT가 `<div id="react-root-...">` 컨테이너만 생성 → React/Vue가 mount
- **원칙**: 프레임워크 간 데이터 바인딩 혼합 금지 — DOM 경계에서만 연결

---

## JsInterop 어노테이션 가이드

| 어노테이션 | 용도 | 예시 |
|-----------|------|------|
| `@JsType(isNative=true)` | 네이티브 JS 타입 매핑 | `HTMLElement`, `Storage` |
| `@JsType` | Java 클래스를 JS로 내보내기 | 컴포넌트 노출 |
| `@JsProperty` | getter/setter를 JS 프로퍼티로 | `getLength()` → `.length` |
| `@JsMethod(name="...")` | 메서드 이름 변경 | `print()` → `console.log()` |
| `@JsFunction` | 함수형 인터페이스를 JS 함수로 | 이벤트 핸들러 |
| `@JsOverlay` | Java 전용 헬퍼 메서드 추가 | native 타입에 유틸 메서드 |
| `@JsIgnore` | JS 내보내기에서 제외 | |

**@JsOverlay 규칙:**
- `final` 이어야 함
- 인스턴스 메서드에서 **재귀 호출 금지** → `ReferenceError` 발생 → static 헬퍼로 우회

---

## Lombok 연동 상세

### GWT에서 Lombok이 필요한 이유
GWT 컴파일러는 소스코드를 직접 읽으므로 Lombok이 생성하는 메서드를 인식하지 못함.
sayaya-gwt 플러그인이 GWT 컴파일 시 Lombok Java Agent를 자동 주입하여 해결.

### GWT 호환 Lombok 어노테이션

| 어노테이션 | GWT 호환 | 주요 용도 |
|-----------|---------|----------|
| `@Delegate` | O | BehaviorSubject 래핑, IsElement 위임, 인터페이스 위임 |
| `@Getter(onMethod_)` | O | native JsType에 `@JsOverlay` getter 자동 생성 |
| `@Accessors(fluent=true)` | O | `.title()` 형태 플루언트 접근자 |
| `@Builder` | O | 불변 객체 팩토리 |
| `@Data` | **X** | equals/hashCode가 GWT native 타입과 충돌 |
| `@Value` | **X** | 같은 이유 |
| `@RequiredArgsConstructor` | △ | Dagger `@Inject`와 충돌 가능 |

### @Delegate 활용 패턴 (프로젝트 내 38개 파일 사용)

**1. BehaviorSubject 전체 위임 (쓰기+읽기)**
```java
@Singleton
public class MenuList {
    @Delegate
    private final BehaviorSubject<List<Menu>> _this = behavior(List.of());
    @Inject MenuList() {}
}
// menuList.subscribe(...), menuList.next(...) 모두 가능
```

**2. Observable만 위임 (읽기 전용)**
```java
@Singleton
public class TodoListStore {
    private final BehaviorSubject<JsArray<TodoStore>> _this = behavior(new JsArray<>());
    @Delegate private final Observable<JsArray<TodoStore>> observable = _this;
    // 외부: subscribe만 가능. 내부: _this.next()로 상태 변경
}
```

**3. HTMLContainerBuilder 위임 (IsElement)**
```java
public class DrawerElement implements IsElement<HTMLElement> {
    @Delegate private final HTMLContainerBuilder<HTMLElement> _this = nav();
    // Lombok이 element(), add(), css() 등 모든 빌더 메서드 자동 위임
}
```

**4. sayaya-ui 컴포넌트 빌더 위임 (MD3 컴포넌트 간소화)**

모든 sayaya-ui 컴포넌트 빌더에 `@Delegate`를 적용하면 수동 `element()`, `addEventListener`, `textContent`, disabled 캐스팅을 모두 제거할 수 있다.

```java
// Button — .filled() / .outlined() / .text()
@Singleton
public class UndoButton implements IsElement<HTMLElement> {
    @Delegate private final ButtonElementBuilder.TextButtonElementBuilder _this;
    @Inject UndoButton(ActionManager actionManager) {
        _this = ButtonElementBuilder.button().text().css("my-btn", "my-btn-undo");
        _this.onClick(e -> actionManager.undo());
        actionManager.onCanUndo(can -> _this.disabled(!can));
    }
}

// Select — .outlined() / .filled()
@Delegate private final SelectElementBuilder.OutlinedSelectElementBuilder _this;
_this = SelectElementBuilder.select().outlined().label("Type")
        .option().value("v").text("t").done();

// Checkbox
@Delegate private final CheckboxElementBuilder _this;
_this = CheckboxElementBuilder.checkbox().select(true).ariaLabel("Snap");

// TextField — .outlined() / .filled()
@Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder _this;
_this = TextFieldElementBuilder.textField().outlined().label("Name");
```

**원칙:** sayaya-ui 빌더에서 `.element()` 호출 후 raw HTMLElement에 직접 조작하는 대신,
빌더 자체를 `@Delegate`로 유지하면 빌더 API(`.text()`, `.onClick()`, `.disabled()`, `.select()` 등)를 직접 사용할 수 있다.

### @Getter(onMethod_) + @JsOverlay — native JS 타입 getter
```java
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class Menu {
    private String title;
    @JsProperty(name = "supporting_text")
    private String supportingText;
    // → Lombok이 @JsOverlay @JsIgnore public final String title() { return title; } 자동 생성
    // → JSON 필드명과 Java 필드명이 다를 때 @JsProperty 사용
}
```

### 롬복 활용 확대 가능 영역

**현재 미사용이지만 적용 가능한 패턴:**

| 패턴 | 적용 대상 | 효과 |
|------|----------|------|
| `@Builder` + Record 대체 | ColumnDef, Arrow 등 DTO | 다중 생성자 제약 우회 + 가독성 |
| `@Delegate` IsElement 확대 | 수동 element() 구현 59개 파일 | 보일러플레이트 제거 |
| `@Getter(onMethod_)` 확대 | 도메인 모듈의 native JsType | 수동 @JsOverlay getter 제거 |

---

## GWT 제약사항

| 제약 | 설명 | 우회 방법 |
|------|------|----------|
| Java record 다중 생성자 | compact + 추가 생성자 시 NPE | static 팩토리 `of()` 또는 `@Builder` |
| JSNI `.catch()` | JS 예약어 | `['catch']()` 사용 |
| `@JsOverlay` 재귀 | ReferenceError | static 헬퍼로 우회 |
| `@JsType(isNative=true)` | 메서드 추가 불가 | `@JsOverlay` static 메서드 |
| GWT 모듈 이름 하이픈 | 컴파일 에러 | camelCase 사용 |
| `String.format()` | GWT 미지원 | 직접 문자열 연결 |
| `@Data`/`@Value` | native 타입 equals/hashCode 충돌 | `@Getter` + `@Builder` 조합 |

---

## CSS 클래스 네이밍 규칙

```java
// 모든 UI 요소에 테스트 가능한 고유 CSS 클래스 필수
.css("type-ctrl-btn", "type-ctrl-btn-add")    // 제네릭 + 구체적

// classList 안전한 조작 (className 직접 할당은 기존 클래스 덮어씀)
element.classList.add("active");
element.classList.toggle("selected", isSelected);

// :first-child/:last-child 대신 data 속성 사용
.attr("data-type-key", type.key())            // 테스트에서 확실한 선택
```

---

## 테스트 패턴 (sayaya-gwt + Playwright)

### GwtTestSpec 동작 원리
- Ktor 내장 웹서버로 컴파일된 GWT 앱 서빙 (모듈별 고유 포트)
- `page.waitForLoadState(LoadState.NETWORKIDLE)` — GWT JS 로딩 완료까지 자동 대기
- 브라우저 콘솔 로그 자동 수집 → `page shouldContainLog "message"` 검증 가능
- Playwright headless Chromium 사용

### 테스트 작성법
```kotlin
@GwtHtml("src/test/webapp/mytest.html")
class MyTest : GwtTestSpec({
    Given("초기화됨") {
        Then("요소가 존재한다") {
            page.querySelector(".my-class") shouldNotBe null
        }
        When("버튼을 클릭하면") {
            page.click(".my-btn")
            Thread.sleep(500)  // 비동기 DOM 업데이트 대기
            Then("상태가 변한다") {
                page.querySelectorAll(".item").count() shouldBe 3
            }
        }
    }
})
```

### 테스트 HTML 요구사항
- GWT 모듈의 `rename-to` 값과 nocache.js 경로가 일치해야 함
- 예: `rename-to="canvastest"` → `src="canvastest/canvastest.nocache.js"`
- JS 라이브러리(rxjs, sayaya-ui, fontawesome)를 HTML `<head>`에서 로드

### 테스트 Gradle 설정
```kotlin
// 모듈별 고유 포트 — 병렬 테스트 가능
tasks.withType<Test> {
    extensions.configure<dev.sayaya.gwt.GwtTestTaskExtension>("gwt") {
        webPort.set(18081)  // 모듈마다 다른 포트
    }
}
```

---

## 이벤트 / 모듈 간 통신 패턴

### SSE → CustomEvent 브릿지
```java
// WorkspaceEventListener: SSE 수신 → window CustomEvent 디스패치
DomGlobal.window.dispatchEvent(
    new CustomEvent<>("handbook-workspace-event", detail)
);
// detail 형식: "EVENT_TYPE:payload_json" (콜론 구분)
```

### 모듈 간 CustomEvent 수신
```java
DomGlobal.window.addEventListener("handbook-workspace-event", evt -> {
    CustomEvent<?> ce = Js.cast(evt);
    String data = Js.cast(ce.detail);
    if (data.startsWith("DOCUMENT_CREATED:")) {
        String json = data.substring("DOCUMENT_CREATED:".length());
        // 처리
    }
});
```

### 이벤트 타입별 BehaviorSubject 분기 (CommandRouter)
```java
// 각 커맨드 타입마다 별도 BehaviorSubject
BehaviorSubject<OverlayRequest> overlaySubject;
BehaviorSubject<NavigateInfo> navigateSubject;
BehaviorSubject<String[]> mutateSubject;
// JSON 파싱 후 해당 Subject로 라우팅
```
