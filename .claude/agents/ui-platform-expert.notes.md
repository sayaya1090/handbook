# ui-platform-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: outline/filled 아이콘 display:none → opacity+visibility 크로스페이드 전환 → main/test shell.css 동기화 + DrawerTest display → visibility 어설션 업데이트, 방식 변형 C (absolute inset:0 스택)
- 2026-04-18: 햄버거 고정 재확인 + 메뉴 아이콘 2개 오버랩 → shell.css 에 `align-self:flex-start + margin-left:8px` 적용 확인, md-item start slot 단일 아이콘 구조 확인 (outlined/filled 2-icon 없음)
- 2026-04-18: MenuRail collapse/expand 아이콘 시프트 → `.rail[expand] padding 0 0.6rem + align-items:flex-start` 제거 + 햄버거 `align-self:center → flex-start + margin-left:8px` 로 icon center x=28px 고정
- 2026-04-18: 햄버거 드로어 우측 밀림 회귀 진단 → AppBar leading mount + AppBar `left:var(--drawer-width)` 조합 원인, MenuRail 상단 이관 권고 (사용자 승인 후 수정 완료)

## 탐색 패턴

(미확보)

## 반복 함정

- **`@JsOverlay` 재귀 호출 금지** — GWT ReferenceError. static 헬퍼로 우회.
- **`backdrop-filter` 가 fixed 자손의 containing block** — 모바일 하단 네비 width=0 증상.
- **하드코딩 금지** — MD3 토큰만 사용 (CLAUDE.md).
- **AppBar `left: var(--shell-drawer-width)` + 좌측 slot mount 조합 주의** — AppBar 가 drawer 우측부터 시작하는 MD3 Top App Bar + Navigation Rail 병치 디자인에서, AppBar leading slot 에 mount 된 요소는 drawer 상태에 따라 viewport 좌표가 같이 이동. drawer toggle 처럼 "drawer 에 소속된 것처럼" 보여야 하는 요소는 AppBar leading 금지, Rail 상단 mount 가 정답 (MD3 Navigation Rail 정석). (2026-04-18 햄버거 회귀)
- **부분 봉합 회귀 주의** — 모바일 한정 `display:none` 으로 증상 봉합 시 데스크톱 경로는 그대로 남음. 증상이 "뷰포트 한쪽에서만 안 보인다" 인지 "양쪽 다 고친 거" 인지 커밋 메시지에 명시 (3440cfe 의 "모바일 햄버거 숨김" 이 사용자에겐 "햄버거 위치 정리" 로 인식된 사례).
- **Rail collapse/expand 는 "폭만 변하고 아이콘 x 좌표는 고정"** 이 MD3 정석. `.item .collapse`(margin 8px, 40px 버튼) 과 `md-item` slot=start(padding-inline-start 16px, 24px 아이콘) 의 icon center 가 둘 다 x=28px 에 맞도록 설계됨. `.rail[expand]` 에 `padding-left` / `align-items:flex-start` 류의 오프셋을 추가하면 이 정렬이 깨져 아이콘이 전환 시 시프트된다. 햄버거도 `align-self:center` 대신 `align-self:flex-start + margin-left:8px` 로 icon center=28px 고정 필요. (2026-04-18)
- **outline/filled 아이콘 토글은 display:none 금지 — absolute overlap + visibility 전환**. `display:none` 은 flex 재계산 → 1px 시프트 + MD3 fade-through 위배. 두 `<i>` 에 `position:absolute; inset:0` 로 bbox 공유 후 opacity/visibility 200ms emphasized-decelerate 크로스페이드. Playwright 검증은 `getComputedStyle(...).visibility` 로 "visible" / "hidden" 비교 (toBeVisible() 도 visibility:hidden 을 invisible 로 인식). md-icon-button host / md-item light DOM `<i>` 에만 CSS 걸면 되므로 shadow DOM 경계 회피. (2026-04-18)

## 내부 체크리스트

- [ ] 새 토큰 추가 시 → `docs/contracts/design-tokens.md` + global.css 라이트/다크 양쪽
- [ ] 새 메뉴 항목 추가 시 → urlRegex 매칭 로직 확인
- [ ] 모바일 레이아웃 변경 시 → 48dp 터치 타겟 + Safe Area

## 과거 실수

(미확보)

## 원칙 갱신 제안

- **Drawer 에 소속된 것처럼 보여야 하는 컨트롤은 AppBar 가 아니라 Drawer/Rail 에 mount** — AppBar 가 drawer-width 오프셋을 받는 병치 레이아웃에서 mount slot 선택이 시각 위치를 좌우. 일반 규칙으로 승격 검토.

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)
