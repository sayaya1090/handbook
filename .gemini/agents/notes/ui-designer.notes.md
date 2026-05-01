# ui-designer Notes

Handbook의 UI/UX 디자인 및 시각적 폴리싱 전문가 에이전트의 자율 작업 노트.

## 작업 원칙 및 가이드라인
- **MD3 준수**: 모든 컴포넌트는 Material Design 3 표준을 따르며, 하드코딩 대신 디자인 토큰을 사용한다.
- **sayaya-ui 활용**: 기본적으로 `sayaya-ui` 컴포넌트를 사용하고, 필요 시 스타일만 오버라이드한다.
- **반응형 최적화**: 뷰포트 크기에 따라 레이아웃이 유연하게 변화하도록 설계한다.

## 도메인 모델 패턴 (GWT 호환 표준)
`@JsType(isNative = true)`를 사용하는 도메인 모델은 Java 캡슐화 규칙 뿐만 아니라 GWT/JS 상호운용성과 직렬화 호환성을 최우선으로 한다.

```java
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class MyModel implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("field") @JsProperty private String field; // private 필드 권장

    @JsOverlay @JsIgnore
    public static MyModel create(...) { ... }
}
```
- **핵심**: `Lombok` Getter/Setter에 `@JsOverlay`를 붙여 Java 환경에서도 사용 가능하게 한다.


## UI/UX 결정 사항
- *(작업을 진행하며 채워나갈 예정)*

## 미해결 UI 이슈 및 개선점
- *(작업을 진행하며 채워나갈 예정)*
