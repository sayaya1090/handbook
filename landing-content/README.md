# Landing-Content 모듈

SEO 랜딩과 앱 내부 랜딩이 **공유하는 기능 설명 카드 컬렉션** 을 제공하는 순수 GWT 라이브러리.
DOM 팩토리만 노출하고 EntryPoint·SEO·CTA·외부 앵커는 포함하지 않는다 — 외피는 각 소비자에서 추가한다.

관련 요구사항: `docs/requirements.md` §3.22.1
관련 아키텍처: `docs/architecture.md` §23

---

## 역할

- **단일 원소스(Single Source of Truth)**: 기능 설명 콘텐츠를 `landing-ui` (SEO 프리렌더) 와 앱 내부 랜딩 activity 양쪽에 동일하게 공급.
- **DOM 팩토리만 제공**: EntryPoint·상태 관리·API 호출 없음. 재사용 가능한 Elemental2 `HTMLElement` 반환.
- **i18n 지원**: 기존 `src/main/i18n/language.{locale}.json` 패턴으로 제공 (빌드 시 머지).

---

## 제공 API

| 팩토리 | 반환 | 설명 |
|--------|------|------|
| `FeatureGridElement.build()` | `HTMLElement` | Handbook 핵심 기능 카드 그리드 (스키마 변경, 이력 관리, AI 에이전트, 실시간 협업 등) |
| `FeatureCardElement.build(content)` | `HTMLElement` | 단일 기능 카드 — 아이콘, 제목, 요약 |

각 카드의 콘텐츠는 `FeatureCardContent` VO 로 캡슐화되어 i18n 바인딩된다.

---

## 비목적 (Non-Goals)

- **히어로 섹션** — SEO 랜딩 전용이므로 `landing-ui` 에서 조립
- **CTA 버튼** — 상태·표면에 따라 다르므로 각 소비자가 추가
- **SEO 메타 태그** — 후처리 단계에서 주입
- **API 호출** — 순수 렌더링 라이브러리

---

## 의존성

- sayaya-web (GWT, Elemento)
- ui-components (카드 MD3 컴포넌트 재사용)

외부 서비스·네트워크·상태 관리 의존성 없음.

---

## 주의

- 카드 구성이 변경되면 SEO 랜딩과 앱 내부 랜딩 양쪽에 **자동 반영**된다 — 분기 콘텐츠를 만들지 말 것.
- `LandingContent.gwt.xml` 의 `inherits` 는 최소한으로 유지 (GWT Core, sayaya-web 기본). 외부 서비스 의존 금지.
- 순수 렌더러이므로 테스트는 DOM 어설션만 필요 (상태·비동기 없음).
