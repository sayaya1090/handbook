# Landing-UI 유스케이스

## 빌드 타임 프리렌더 시퀀스 (UC-LU1)

```mermaid
sequenceDiagram
    participant G as Gradle (:landing-ui:prerender)
    participant GC as GWT Compiler
    participant J as Jetty (Local)
    participant P as Playwright (Headless)
    participant PP as PrerenderPostProcessor

    G->>GC: "compile(locale)"
    loop "ko, en"
        G->>J: "serve build/gwt/out"
        G->>P: "headless navigate('/')"
        P->>P: "waitForSelector('body.rendered')"
        P-->>G: "page.content() HTML"
        G->>PP: "apply(HTML, locale)"
        PP-->>G: "finalized HTML"
        G->>G: "write build/landing/{locale}/index.html"
    end
    G->>G: "write sitemap.xml, robots.txt, llms.txt, llms-full.txt"
```

| 항목 | 내용 |
|------|------|
| **액터** | 시스템 (CI/CD 빌드 에이전트) |
| **정상 흐름** | 1. Gradle 태스크가 실행된다.<br>2. GWT 컴파일러가 Java 소스를 JS로 변환한다.<br>3. 로컬 서버(Jetty)가 컴파일된 파일을 서빙한다.<br>4. Playwright가 헤드리스 브라우저로 접속하여 완전한 DOM이 생성될 때까지 대기한다.<br>5. 렌더링된 HTML을 덤프하고, 스크립트 제거 및 SEO 메타 태그 주입을 거쳐 정적 파일로 저장한다. |
| **결정성** | 동일 커밋은 동일 바이트 생산 (타임스탬프·난수 없음) |

---

## UC-LU2: 검색엔진 크롤링 및 인덱싱

| 항목 | 내용 |
|------|------|
| **액터** | 검색엔진 크롤러 (Googlebot 등) |
| **선행조건** | 랜딩이 S3 에 배포되어 있고 HTTPRoute 가 활성화됨 |
| **정상 흐름** | 1. 크롤러가 `/` 요청<br>2. S3 에서 `static/landing/ko/index.html` 반환<br>3. 크롤러가 HTML 파싱 (`<title>`, meta, JSON-LD, body 콘텐츠)<br>4. 크롤러가 JS 실행 (WRS)<br>5. 인라인 스크립트가 쿠키 검사 → 크롤러는 쿠키 없음 → 리다이렉트 미발생<br>6. 크롤러가 `<link rel="alternate" hreflang>` 를 따라 `/en/` 도 개별 인덱싱<br>7. `/sitemap.xml` 로부터 추가 URL 확인 |
| **결과** | SERP 에 ko/en 랜딩이 각각 언어별로 노출 |
| **주의** | `/app.html` 은 `noindex, follow` 메타로 색인에서 제외 |

---

## UC-LU3: 비로그인 사용자가 SEO 랜딩 방문

| 항목 | 내용 |
|------|------|
| **액터** | 비로그인 방문자 |
| **선행조건** | JWT 쿠키 없음 |
| **정상 흐름** | 1. 방문자가 `/` 접속 (SERP 클릭, 직접 입력 등)<br>2. 정적 HTML 로딩<br>3. 인라인 스크립트 쿠키 검사 → 쿠키 없음 → 랜딩 유지<br>4. 방문자가 FeatureGrid + CTA 확인<br>5. CTA `<a href="/app.html">` 클릭 → 앱으로 이동 → 로그인 요구 |

---

## UC-LU4: 로그인 사용자가 랜딩으로 진입

| 항목 | 내용 |
|------|------|
| **액터** | 로그인 상태 사용자 |
| **선행조건** | JWT 쿠키 유효 |
| **정상 흐름** | 1. 사용자가 `/` 접속<br>2. 정적 HTML 로딩<br>3. 인라인 스크립트 쿠키 감지 → `location.replace('/app.html')`<br>4. 브라우저가 `/app.html` 로 이동<br>5. 앱 셸 정상 부팅 |
| **대안** | 쿠키가 만료되었거나 무효하면 UC-LU3 로 폴백 |
| **주의** | `location.replace` 는 뒤로가기 히스토리에 남지 않음 — 사용자가 "뒤로" 눌러도 앱 내부에서만 이동 |

---

## 테스트 전략

| 대상 | 도구 | 검증 |
|------|------|------|
| 결정성 | Gradle + diff | 두 번 빌드 결과 바이트 동일 |
| 메타 태그 | HTML 파서 | canonical, hreflang, JSON-LD, og 주입 확인 |
| 리다이렉트 | Playwright | 쿠키 없음 → 리다이렉트 안 됨, 쿠키 있음 → `/app.html` 이동 |
