# Landing-UI 모듈 (SEO 프리렌더)

비로그인 방문자·검색엔진 크롤러 대상 **SEO 랜딩 페이지** (GWT).
앱(`/app.html`) 과 완전히 분리된 정적 산출물로 배포되며, **빌드 타임 프리렌더를 거쳐 최종 산출물은 순수 정적 HTML** 이다.
런타임에 GWT 스크립트를 포함하지 않는다.

관련 요구사항: `docs/requirements.md` §3.22.2
관련 아키텍처: `docs/architecture.md` §24
설계 상세: [DESIGN.md](DESIGN.md)

---

## 목적

- **SEO**: 검색엔진이 인덱싱할 수 있는 의미 있는 정적 HTML 을 제공
- **공개 진입점**: 로그인 없이 접근 가능한 제품 소개
- **앱 진입 유도**: `<a href="/app.html">` CTA + 로그인 사용자용 쿠키 기반 자동 리다이렉트

---

## 구성

- `landing-content` 의 `FeatureGridElement` 로부터 공통 기능 카드 DOM 을 받아 중앙에 조립
- 히어로 섹션 (제목·부제·CTA 앵커)
- 푸터 (회사·라이선스·언어 전환)
- 인라인 `defer` 스크립트: JWT 쿠키 감지 시 `/app.html` 로 `location.replace`
- 후처리에서 주입될 메타 마커 (JSON-LD, hreflang, canonical, `<html lang>`)

---

## 빌드 파이프라인

```
./gradlew :landing-ui:prerender
  → (ko, en 로케일 각각)
      1. GWT 컴파일 (landing-ui + landing-content, language.{locale}.json 머지)
      2. Jetty 로컬 서빙
      3. Playwright 헤드리스 접속 → body.rendered 대기
      4. page.content() 로 HTML 덤프
      5. PrerenderPostProcessor:
           - <script src="...landing.nocache.js"> 제거
           - <html lang>, <link rel="canonical">, hreflang 주입
           - OG/Twitter/title/description 주입
           - JSON-LD (WebApplication + potentialAction + HowTo) 삽입
           - <link rel="manifest" href="/manifest.json"> 주입
      6. build/landing/{locale}/index.html 저장
  → sitemap.xml, robots.txt, llms.txt, llms-full.txt 생성
```

---

## 산출물

| 파일 | 경로 (S3 키) | URL |
|------|-------------|-----|
| index.html (ko) | `handbook-<stage>/static/landing/ko/index.html` | `/` |
| index.html (en) | `handbook-<stage>/static/landing/en/index.html` | `/en/` |
| sitemap.xml | `handbook-<stage>/static/sitemap.xml` | `/sitemap.xml` |
| robots.txt | `handbook-<stage>/static/robots.txt` | `/robots.txt` |
| llms.txt | `handbook-<stage>/static/llms.txt` | `/llms.txt` |
| llms-full.txt | `handbook-<stage>/static/llms-full.txt` | `/llms-full.txt` |

Kargo Release Train 에 참여하여 Sync Hook Job 으로 S3 에 sync. `handbook-lib` 의 `handbook.landing-sync-job` named template 을 사용한다.

---

## 의존성

- landing-content (공통 DOM)
- sayaya-web (GWT, Elemento, Dagger)
- ui-components

activity 는 **의존하지 않는다** — SEO 랜딩은 `/menus`·FetchApi 를 호출하지 않는다.

---

## 제약

- **Cloaking 금지**: 크롤러/사용자에게 같은 HTML 반환. User-Agent 분기 없음.
- **프리렌더 중 백엔드 호출 금지**: 결정적 빌드 보장.
- **인증 종속 콘텐츠 금지**: 워크스페이스·사용자 데이터 노출 안 함.
- **CTA/메뉴 링크는 실제 `<a href>` 앵커**: JS 전용 핸들러 금지 (크롤러가 못 따라감).
- **빌드 결정성**: 타임스탬프·난수·빌드 번호를 HTML 에 넣지 않음.
