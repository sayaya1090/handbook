# ui-platform-expert Operational Notes

---

## 탐색 패턴

(미확보)

## 반복 함정

- **`@JsOverlay` 재귀 호출 금지** — GWT ReferenceError. static 헬퍼로 우회.
- **`backdrop-filter` 가 fixed 자손의 containing block** — 모바일 하단 네비 width=0 증상.
- **하드코딩 금지** — MD3 토큰만 사용 (CLAUDE.md).

## 내부 체크리스트

- [ ] 새 토큰 추가 시 → `docs/contracts/design-tokens.md` + global.css 라이트/다크 양쪽
- [ ] 새 메뉴 항목 추가 시 → urlRegex 매칭 로직 확인
- [ ] 모바일 레이아웃 변경 시 → 48dp 터치 타겟 + Safe Area

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

---

마지막 감사: — (신규)
