# landing-expert Operational Notes

---

## 탐색 패턴

(미확보)

## 반복 함정

- **cloaking 유혹**: "로그인 사용자만 앱으로 리다이렉트" 는 **쿠키 기반**이 필수.
  User-Agent/Referrer 분기는 정책 위반.
- **빌드 결정성 파괴**: 프리렌더 중 시간/난수/외부 API 호출 포함 시 빌드 재현 불가.

## 내부 체크리스트

- [ ] 랜딩 콘텐츠 변경 시 → landing-content 공통 원소스 수정 → SEO + 앱 내부 양쪽 자동 반영 확인
- [ ] 새 언어 추가 시 → i18n 파일 + hreflang 양방향 + sitemap 엔트리
- [ ] 새 OpenAPI 엔드포인트 공개 시 → llms.txt 에도 반영
- [ ] `/app.html` noindex 메타는 절대 제거 금지 (thin content 색인 방지)

## 과거 실수

(미확보)

## 원칙 갱신 제안

(미확보)

---

마지막 감사: — (신규)
