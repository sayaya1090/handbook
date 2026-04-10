# Activity 모듈

GWT 클라이언트 모듈 간 공유되는 도메인 클래스와 공통 유스케이스를 정의하는 라이브러리.
Shell UI와 각 기능 모듈(타입 에디터, 문서 에디터 등)이 공통으로 참조한다.

## 도메인 클래스

| 클래스 | 설명 |
|--------|------|
| **Menu** | 네비게이션 메뉴 항목. title, icon, script(모듈 JS), order, tools, urlRegex 등 |
| **Tool** | 메뉴 내 도구. icon, title, order, function(실행 함수) |
| **ToolFunction** | 도구 실행 함수 (@JsFunction). `exec()` + `repeat()` 패턴으로 비동기 완료 대기 |
| **Render** | 프레임 렌더링 콜백 (@JsFunction) |
| **Progress** | 진행률 표시 VO (enabled, intermediate, value, max, description) |
| **Labels** | i18n 라벨 맵 (key → 번역 문자열) |

## 유스케이스

| 클래스 | 설명 |
|--------|------|
| **FetchApi** | HTTP fetch 래퍼 인터페이스 (테스트 모킹 가능) |
| **LabelProvider** | i18n 라벨 구독/제공. 언어 변경 시 자동 갱신 |
| **LanguageDetector** | 브라우저 언어 감지 |
| **LanguagePackRepository** | 언어 팩 로딩 포트 |
| **ViewportObserver** | 뷰포트 크기 변경 감지. mobile(<768px), compact(<480px) 상태를 BehaviorSubject로 발행 |

> 에이전트 연동 인터페이스(MutationReceiver, StateProvider, SearchProvider)와 Window 브릿지는
> [agent-bridge](../agent-bridge/README.md) 모듈로 분리되었다.

## GWT 모듈

```xml
<!-- Activity.gwt.xml -->
<module>
    <source path="domain"/>
    <source path="usecase"/>
</module>
```

다른 GWT 모듈에서 `<inherits name="dev.sayaya.handbook.Activity"/>`로 참조한다.

## 라이브러리 빌드

JAR에 Java 소스를 포함시켜 GWT 컴파일러가 참조할 수 있도록 한다:

```kotlin
tasks.jar {
    from(sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.WARN
}
```

## JS Interop

모든 도메인 클래스는 `@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")`로 선언되어
JavaScript 객체와 직접 매핑된다. Getter는 `@JsOverlay`로 구현하여 GWT 컴파일러 호환성을 유지한다.

## 의존성

- **sayaya-ui** — UI 컴포넌트 라이브러리
- **sayaya-rx** — RxJava GWT 래퍼
- **Elemento** — GWT DOM 빌더
- **Dagger** — 컴파일 타임 DI
- **Lombok** — 보일러플레이트 제거
