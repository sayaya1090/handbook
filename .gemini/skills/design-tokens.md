# MD3 디자인 토큰

## 색상
- `--md-sys-color-primary`, `--md-sys-color-secondary`, `--md-sys-color-tertiary`
- `--md-sys-color-error`, `--md-sys-color-surface` 계열
- `--md-sys-color-on-primary`, `--md-sys-color-on-surface` 등 대비 색상

## 타이포그래피
- `--md-sys-typescale-headline-*` (headline-large, headline-medium, headline-small)
- `--md-sys-typescale-body-*` (body-large, body-medium, body-small)
- `--md-sys-typescale-label-*` (label-large, label-medium, label-small)

## 모션
- `--md-sys-motion-duration-medium2` (300ms)
- `--md-sys-motion-easing-standard`

## 형태
- `--md-sys-shape-corner-small` (8px)
- `--md-sys-shape-corner-medium` (12px)
- `--md-sys-shape-corner-extra-large` (28px)

## 상태 색상 매핑
| 상태 | 색상 토큰 |
|------|-----------|
| created | tertiary |
| changed | tertiary |
| deleted | 투명화 |
| valid | primary |
| invalid | error |
| conflict | secondary |

## 사용 규칙
- 하드코딩된 색상/크기/트랜지션 금지
- 색상은 `var(--md-sys-color-*)`, 크기는 `var(--md-sys-typescale-*)`
- 형태는 `var(--md-sys-shape-*)`, 모션은 `var(--md-sys-motion-*)` 사용
- 상태 색상은 docs/design-patterns.md에 정의된 매핑을 따른다
- 버튼, 카드, 컨테이너, 툴바 등 공통 UI 패턴은 모듈 간 시각적으로 일관되어야 한다
