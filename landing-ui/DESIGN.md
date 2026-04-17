# Landing-UI 설계

## 1. 핵심 설계 결정

| 결정 | 이유 |
|------|------|
| 개발은 GWT, 배포는 순수 정적 HTML | sayaya-ui·MD3 토큰 재사용으로 앱과 시각적 일관성 유지. 동시에 크롤러에겐 JS 의존 없는 HTML 제공 |
| `landing-content` 분리 (공통 원소스) | SEO 랜딩과 앱 내부 랜딩이 기능 설명 블록을 공유 — 콘텐츠 분기 방지 |
| 서브디렉토리 i18n (`/`, `/en/`) | Google 권장 패턴. 서브도메인/쿠키 분기는 크롤러 혼동 유발 |
| 빌드 타임 프리렌더 | 런타임 SSR 부담 없이 SEO 효과 확보. 결정적 빌드 |
| JWT 쿠키 기반 인라인 리다이렉트 | 크롤러는 쿠키 없음 → 리다이렉트 미발생 → 랜딩 색인. 로그인 사용자만 `/app.html` 자동 이동 |
| `/app.html` 색인 차단 (noindex, follow) | 앱 셸은 빈 DOM — thin content 판정 방지. SERP 에는 랜딩만 노출 |
| 별도 sync-job 템플릿 | 기존 frontend sync-job 은 GWT 번들 업로드용. 랜딩은 HTML + sitemap + robots + llms 를 여러 경로에 배치 |

---

## 2. 프리렌더 파이프라인

### 2.1 렌더 완료 시그널

Playwright 가 안정된 시점에 DOM 을 캡처할 수 있도록 다음 순서로 마커를 찍는다.

1. GWT EntryPoint 가 초기화 완료 시 DOM 조립 끝.
2. FontAwesome JS 가 `<i>` → `<svg>` 치환을 **완료**할 때까지 대기.
3. `document.body.classList.add('rendered')`.
4. Playwright 의 `waitForSelector('body.rendered')` 가 해소.

FontAwesome 치환 완료 감지는 특정 아이콘의 `<svg>` 존재 여부 또는 MutationObserver 로 구현. 단순히 `setTimeout` 으로 얼버무리면 빌드 결정성이 깨진다.

### 2.2 후처리 (PrerenderPostProcessor)

덤프된 HTML 에 대해 다음 변환을 **순서대로** 적용한다.

1. `<script src="...landing.nocache.js">` 및 관련 GWT 부트스트랩 제거
   - 이 스크립트가 배포 HTML 에 남으면 사용자 브라우저에서 GWT 가 재실행되어 DOM 을 재작성할 수 있음
2. `<html lang="{locale}">` 설정
3. `<link rel="canonical" href="{self-url}">` 주입 (self-canonical)
4. `<link rel="alternate" hreflang="{other-locale}" href="{other-url}">` 전체 로케일 링크 주입
5. `<link rel="alternate" hreflang="x-default" href="{ko-url}">` 주입
6. `<title>`, `<meta name="description">`, OG/Twitter 메타 주입 (i18n)
7. `<link rel="manifest" href="/manifest.json">` 주입
8. JSON-LD `<script type="application/ld+json">` 블록 삽입 (WebApplication + potentialAction + HowTo)
9. 인라인 `defer` 스크립트 삽입 — JWT 쿠키 감지 → `location.replace('/app.html')`

### 2.3 결정성 보장

- 타임스탬프·난수·빌드 번호를 HTML 에 절대 넣지 않는다.
- `/menus`·`/auth/*` 등 백엔드 호출 금지 — 프리렌더 실행 중 네트워크 요청이 있으면 응답 변동에 빌드가 의존.
- Google Fonts 등 외부 자산은 로컬 호스팅 또는 프리렌더 범위에 포함.
- 폰트 로드 순서에 따라 레이아웃이 흔들리지 않도록 `font-display: block` 등으로 안정화.

---

## 3. 인증 기반 리다이렉트 스크립트

```js
(function() {
  if (document.cookie.split('; ').some(c => c.startsWith('jwt='))) {
    location.replace('/app.html');
  }
})();
```

- `<head>` 최상단이 아닌 `<script defer>` 로 배치 (Googlebot 의 "지연된 서버 리다이렉트" 오해 방지).
- `location.replace` 는 뒤로가기 히스토리에 남지 않음 — 사용자가 "뒤로" 눌러도 랜딩이 아니라 이전 페이지.
- 쿠키 이름 `jwt` 는 login 서비스와 동기화된 상수를 사용한다 (변경 시 이쪽도 반영).

---

## 4. SEO 메타 전략

### 4.1 다국어 hreflang

ko 페이지:
```html
<html lang="ko">
<head>
  <link rel="canonical" href="https://handbook.sayaya.cloud/">
  <link rel="alternate" hreflang="ko" href="https://handbook.sayaya.cloud/">
  <link rel="alternate" hreflang="en" href="https://handbook.sayaya.cloud/en/">
  <link rel="alternate" hreflang="x-default" href="https://handbook.sayaya.cloud/">
```

en 페이지:
```html
<html lang="en">
<head>
  <link rel="canonical" href="https://handbook.sayaya.cloud/en/">
  <link rel="alternate" hreflang="ko" href="https://handbook.sayaya.cloud/">
  <link rel="alternate" hreflang="en" href="https://handbook.sayaya.cloud/en/">
  <link rel="alternate" hreflang="x-default" href="https://handbook.sayaya.cloud/">
```

### 4.2 JSON-LD

```json
{
  "@context": "https://schema.org",
  "@type": "WebApplication",
  "name": "Handbook",
  "url": "https://handbook.sayaya.cloud/",
  "applicationCategory": "BusinessApplication",
  "operatingSystem": "Web",
  "browserRequirements": "Requires JavaScript",
  "offers": { "@type": "Offer", "price": "0" }
}
```

필요 시 `WebSite` + `potentialAction` (SearchAction), `HowTo` (주요 사용 흐름 요약) 를 추가 블록으로 삽입.

### 4.3 앱 셸 색인 차단

`app.html` (별도 모듈) 의 `<head>` 에 다음을 **반드시** 유지:
```html
<meta name="robots" content="noindex, follow">
```

`robots.txt` 에서 `/app.html` 을 **Disallow 하지 않는다** — 크롤링을 막으면 메타를 못 읽고 빈 스니펫이 노출될 수 있다.

---

## 5. 사이드카 산출물

### 5.1 sitemap.xml

```xml
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
        xmlns:xhtml="http://www.w3.org/1999/xhtml">
  <url>
    <loc>https://handbook.sayaya.cloud/</loc>
    <xhtml:link rel="alternate" hreflang="ko" href="https://handbook.sayaya.cloud/"/>
    <xhtml:link rel="alternate" hreflang="en" href="https://handbook.sayaya.cloud/en/"/>
    <xhtml:link rel="alternate" hreflang="x-default" href="https://handbook.sayaya.cloud/"/>
  </url>
  <url>
    <loc>https://handbook.sayaya.cloud/en/</loc>
    <xhtml:link rel="alternate" hreflang="ko" href="https://handbook.sayaya.cloud/"/>
    <xhtml:link rel="alternate" hreflang="en" href="https://handbook.sayaya.cloud/en/"/>
    <xhtml:link rel="alternate" hreflang="x-default" href="https://handbook.sayaya.cloud/"/>
  </url>
</urlset>
```

### 5.2 robots.txt

```
User-agent: *
Allow: /

Sitemap: https://handbook.sayaya.cloud/sitemap.xml
```

### 5.3 llms.txt / llms-full.txt

AI 에이전트 디스커버리용 (§3.23.1). 랜딩 i18n 과 동일한 원천 텍스트에서 마크다운으로 생성.

---

## 6. 빌드 태스크 구조 (제안)

```kotlin
// build.gradle.kts
tasks.register("prerender") {
    dependsOn("compileGwt")
    doLast {
        locales.forEach { locale ->
            val outDir = layout.buildDirectory.dir("landing/$locale").get().asFile
            // 1. Jetty 로컬 서빙
            // 2. Playwright 헤드리스
            // 3. body.rendered 대기
            // 4. HTML 덤프
            // 5. PrerenderPostProcessor 적용
            // 6. outDir/index.html 저장
        }
        // sitemap.xml / robots.txt / llms.txt / llms-full.txt 생성
    }
}
```

세부 구현은 별도 `buildSrc` 플러그인으로 분리하여 다른 모듈에서도 재사용 가능하게 한다 (향후 docs-ui 등).

---

## 7. 테스트 전략

- **결정성 테스트**: 동일 입력으로 두 번 빌드해서 `index.html` diff 가 비어 있는지 확인.
- **메타 태그 존재 테스트**: 후처리 후 HTML 에 canonical/hreflang/JSON-LD/og 가 모두 주입되었는지 파싱 검증.
- **리다이렉트 스크립트 테스트**: Playwright 로 쿠키 없이 방문 시 리다이렉트 **안 일어나고**, 쿠키 주입 후 방문 시 `/app.html` 로 리다이렉트되는지 검증.
- **i18n 테스트**: ko/en 각각 렌더 결과가 해당 언어 텍스트를 포함하는지.
- **링크 무결성**: 모든 `<a href>` 가 유효한 내부 경로인지 (`/app.html`, `/auth/login` 등).
