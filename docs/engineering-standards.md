# HandBook 엔지니어링 표준 및 설계 교훈 (2026-04-28)

이 문서는 프로젝트의 대규모 도메인 통합 및 공통 모듈 정리 과정을 통해 확립된 핵심 설계 원칙을 기록합니다.

---

## 1. 공용 도메인 모델 (Shared Domain) 전략

(중략)

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
(이후 생략)
