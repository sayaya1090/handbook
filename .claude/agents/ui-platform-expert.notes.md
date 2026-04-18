# ui-platform-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

---

## 요청 로그

- 2026-04-18: 햄버거 드로어 우측 밀림 회귀 진단 → AppBar leading mount + AppBar `left:var(--drawer-width)` 조합 원인, MenuRail 상단 이관 권고 (사용자 승인 후 수정 완료)

## 탐색 패턴

(미확보)

## 반복 함정

- **`@JsOverlay` 재귀 호출 금지** — GWT ReferenceError. static 헬퍼로 우회.
- **`backdrop-filter` 가 fixed 자손의 containing block** — 모바일 하단 네비 width=0 증상.
- **하드코딩 금지** — MD3 토큰만 사용 (CLAUDE.md).
- **AppBar `left: var(--shell-drawer-width)` + 좌측 slot mount 조합 주의** — AppBar 가 drawer 우측부터 시작하는 MD3 Top App Bar + Navigation Rail 병치 디자인에서, AppBar leading slot 에 mount 된 요소는 drawer 상태에 따라 viewport 좌표가 같이 이동. drawer toggle 처럼 "drawer 에 소속된 것처럼" 보여야 하는 요소는 AppBar leading 금지, Rail 상단 mount 가 정답 (MD3 Navigation Rail 정석). (2026-04-18 햄버거 회귀)
- **부분 봉합 회귀 주의** — 모바일 한정 `display:none` 으로 증상 봉합 시 데스크톱 경로는 그대로 남음. 증상이 "뷰포트 한쪽에서만 안 보인다" 인지 "양쪽 다 고친 거" 인지 커밋 메시지에 명시 (3440cfe 의 "모바일 햄버거 숨김" 이 사용자에겐 "햄버거 위치 정리" 로 인식된 사례).

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
