# MD3 디자인 토큰 계약

모든 UI 모듈이 공유하는 Material Design 3 기반 디자인 토큰.

## 공급자 (Providers)

- **app / global.css** — CSS 커스텀 프로퍼티로 정의
  - `app/src/main/webapp/css/global.css`
- **shell-ui / shell.css** — shell 전용 레이아웃 토큰 확장
- **ui-components** — 재사용 컴포넌트별 토큰 적용

## 소비자 (Consumers)

- **모든 UI 모듈** — `type-ui`, `document-ui`, `workspace-ui`, `login-ui`, `dashboard-ui`, `agent-ui`, `landing-content`, `landing-ui`
- **module-*.css** — 각 모듈이 `var(--md-sys-*)` 로 참조

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 토큰 값 변경 | 라이트/다크 테마 양쪽 + 대비비 (4.5:1 이상) |
| 토큰 추가 | 전 UI 모듈에 대응 사용 여부 검토, MD3 표준 준수 |
| 토큰 제거 | 모든 소비자 CSS 파일 grep 후 대체 토큰으로 마이그레이션 |
| 테마 전환 방식 변경 | `color-theme` 속성 외 진입점 없는지 |

---

## 규칙 (CLAUDE.md 에도 명시)

- **MD3 디자인 토큰만 사용. 하드코딩 금지.**
- 상세는 `.claude/skills/design-tokens.md` 참조.

## 토큰 카테고리

| 카테고리 | 접두사 | 대표 변수 |
|---------|-------|----------|
| Color | `--md-sys-color-*` | `--md-sys-color-primary`, `--md-sys-color-on-primary`, `--md-sys-color-surface` |
| Typography | `--md-sys-typescale-*` | `--md-sys-typescale-headline-large-size`, `--md-sys-typescale-body-medium-font` |
| Elevation | `--md-sys-elevation-*` | `--md-sys-elevation-level0` ~ `--md-sys-elevation-level5` |
| Shape | `--md-sys-shape-*` | `--md-sys-shape-corner-small`, `--md-sys-shape-corner-extra-large` |
| Motion | `--md-sys-motion-*` | `--md-sys-motion-easing-standard`, `--md-sys-motion-duration-medium2` |
| Shell 레이아웃 | `--shell-*` | `--shell-app-bar-height`, `--shell-mobile-tabs-height`, `--shell-drawer-width`, `--shell-frame-left-offset` |

### Shell 레이아웃 토큰 (shell-ui 전용)

`shell-ui/src/main/webapp/css/shell.css` 에서 정의·관리. 자식 UI 모듈은 이 토큰을
직접 읽지 않는다 — Frame mount 시 shell 의 `.frame` 이 여백·오프셋을 적용해준다
([`frame.md`](frame.md)).

| 토큰 | 기본 (Desktop) | Mobile | 소비자 | 용도 |
|------|--------------|--------|-------|------|
| `--shell-app-bar-height` | 56px | 56px | `.frame`, `.shell-app-bar`, `.menu-tabs` | Top App Bar 점유 영역 |
| `--shell-mobile-tabs-height` | 0 | 49px (`.menu-tabs[hide]` → 0) | `.frame` | MobileTabs 점유 영역 (Frame top 오프셋 합산) |
| `--shell-frame-left-offset` | 3.5rem (rail collapse 고정) | 0 | `.frame` | Frame 좌측 rail 영역 확보. rail EXPAND 는 overlay 의도 |
| `--shell-drawer-width` | 3.5 ~ 32rem (rail 상태 조합, 동적) | 0 | `.shell-app-bar`, `.menu-tabs` | AppBar/MobileTabs 좌측 시작점 (Frame 은 미사용) |

## Tooltip 규약

모든 UI 모듈은 tooltip 표시 시 `ui-components.TooltipCard` 를 사용한다. 직접
`title` 속성이나 커스텀 flyout 금지(예외: `OverlayContainer` 의 attention/coachmark
계열). Tooltip 파라미터:

| 항목 | 값 | 비고 |
|------|----|------|
| 지연 (hover intent) | 기본 **300ms** (`TooltipCard.DEFAULT_DELAY_MS`) | MD3 short duration, 마우스 스쳐감 방지 |
| Elevation | Card elevated (level 2, `--md-sys-elevation-level2`) | `OverlayContainer.renderTooltipCard` 와 동일 |
| Position | `start` / `end` / `top` / `bottom` — 기본 `end` | Navigation Rail 우측 |
| Auto-hide (외부 트리거) | `AUTO_HIDE_HIGHLIGHT_MS` = 3000ms | `showImmediate(3000)` — `highlight` agent-command 동반 표시 |
| CSS 클래스 | `ui-tooltip-card`, `ui-tooltip-portal`, `ui-position-<pos>`, `ui-tooltip-headline`, `ui-tooltip-supporting` | `app/src/main/webapp/css/ui-components.css` |

Tooltip 공용 컴포넌트 추출 배경: sayaya-ui 2.4.1.3 에 Tooltip 미제공 — 프로젝트
자체 `TooltipCard` 가 단일 출처. 후속에 sayaya-ui 가 Tooltip 을 제공하면 래핑
교체 검토.

## 테마 전환

```html
<html color-theme="light">  <!-- 또는 dark -->
```

- `color-theme` 속성 전환만으로 전체 팔레트 교체
- 다크 모드에서는 `color-scheme: dark` 자동 적용 (스크롤바·폼 요소도 다크)

## 반응형 Breakpoint

| 이름 | 범위 | 레이아웃 |
|------|------|---------|
| Compact | < 480px | 하단 네비 + 카드 뷰 |
| Medium | 480-768px | 하단 네비 + 수평 스크롤 |
| Expanded | ≥ 768px | 좌측 Navigation Rail |

## 터치 타겟

모바일: 최소 **48dp** — `min-height: 48px`, `min-width: 48px`

## 접근성

- 모든 `on-<color>` / `<color>` 쌍은 4.5:1 이상 대비비 보장
- 키보드 네비: `:focus-visible` 로 `outline: 2px solid primary`

---

## 상세 명세 위치

상세 컬러 팔레트, 타입 스케일, 엘리베이션 값, 셰이프 토큰, 모션 커브는
[`docs/requirements/shell.md`](../requirements/shell.md) (또는 현재 `docs/design.md`) 참조.

이 파일은 **계약 요약** — 전체 UI 가 의존하는 공통 시스템 식별 목적.
