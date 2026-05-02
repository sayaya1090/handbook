---
name: sayaya-ui
description: sayaya-ui 컴포넌트 사용법 및 주의사항
---

# sayaya-ui 컴포넌트 사용법

## 의존성
`libs.bundles.sayaya.web`에 포함. GWT 모듈에서 `<inherits name="dev.sayaya.Ui"/>` 필요.

## 컴포넌트 매핑 (네이티브 -> sayaya-ui)

| 네이티브 HTML | sayaya-ui | 예시 |
|---|---|---|
| `<select>` | `SelectElementBuilder.select().outlined()` | `.label("Label").option().value("v").text("t").done()` |
| `<input checkbox>` | `CheckboxElementBuilder.checkbox()` | `.select(true).onChange(e -> ...)` |
| `<input radio>` | `RadioElementBuilder.radio()` | `.name("group").value("v").select(true)` |
| `<input text>` | `TextFieldElementBuilder.textField().outlined()` | `.label("Label").value("v")` |
| `<button>` | `ButtonElementBuilder.button().filled/outlined/text()` | `.text("Click")` |
| `<dialog>` | `DialogElementBuilder.dialog()` | -- |
| `<progress>` | `ProgressElementBuilder.progress()` | `.indeterminate()` |

## 주요 API 패턴

### Select
```java
SelectElementBuilder.select().outlined()
  .label("Type")
  .option().value("text").text("Text").done()
  .option().value("number").text("Number").done()
  .onChange(e -> { String val = select.value(); })
  .selectByValue("text")
  .removeAllOptions()
```

### Checkbox
```java
CheckboxElementBuilder.checkbox()
  .select(true)       // not checked()
  .onChange(e -> ...)
  .isSelected()       // getter
  .ariaLabel("Snap")
```

### Radio
```java
RadioElementBuilder.radio()
  .name("group")      // from FormAssociable
  .value("option1")   // from FormAssociable
  .select(true)       // not checked()
  .onChange(e -> ...)
```

### TextField
```java
TextFieldElementBuilder.textField().outlined()
  .label("Name")
  .css("my-class")
  .value("initial")
```

### Button
```java
ButtonElementBuilder.button().filled()  // or .outlined() or .text()
  .text("Apply")
  .css("my-btn")
```

## 주의사항
- `checked(boolean)` 메서드 없음 -> `select(boolean)` 사용
- `name(String)`, `value(String)` setter는 `FormAssociable` 인터페이스에 정의
- Select 옵션 추가 후 `.done()` 호출 필수
- MD3 웹 컴포넌트 기반이므로 `md-outlined-select`, `md-checkbox` 등 커스텀 엘리먼트로 렌더링
- **`ConfirmDialog` 자가 부착**: 다이얼로그 호출 시 `root.parentNode`가 null이면 자동으로 `body`에 append하는 로직 권장 (테스트 환경 안정성).
- **버튼 타겟팅**: 다이얼로그 내부 버튼은 `.ui-confirm-actions md-text-button` 셀렉터로 접근 시 Shadow DOM 이슈를 최소화할 수 있음.
