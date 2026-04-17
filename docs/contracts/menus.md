# Menu 집계 프로토콜 계약

`/menus` 엔드포인트의 메뉴 집계 규약, `MenuSupplier` 인터페이스, `Menu` 도메인 구조.

## 공급자 (Providers)

- **login** — Sign In / Sign Out 엔트리 (인증 여부에 따라 분기)
  - `interfaces/api/MenuController.kt`
- **search-type** — 타입 메뉴
- **search-document** — 문서 메뉴
- **search-workspace** — 워크스페이스 메뉴 (Drawer 하단 고정, `bottom=true`)
  - `interfaces/api/MenuController.kt`
- **(신규) landing-menu** — 앱 내부 랜딩 엔트리 (구현 위치 미정 — 별도 모듈 또는 gateway 로컬)

## 소비자 (Consumers)

- **gateway** — `MenuService` 가 모든 공급자를 병렬 호출해 집계
  - `usecase/MenuService.kt`, `usecase/MenuSupplier.kt`
  - `interfaces/api/MenuController.kt` → `GET /menus`
- **shell-ui** — `UrlBasedMenuResolver` 가 URL 정규식으로 매칭하여 자동 선택
  - `client/usecase/UrlBasedMenuResolver.java`

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 공급자 추가 | gateway Dagger 모듈에 `MenuSupplier` 빈 추가, shell-ui 매칭 로직 동작 확인 |
| Menu 도메인 필드 추가 | 모든 공급자 응답 스키마, shell-ui 렌더링 로직, `application/vnd.sayaya.handbook.v1+json` 미디어 타입 버저닝 |
| URL 정규식 변경 | `UrlBasedMenuResolverTest` + 실제 라우팅 동작 |
| 메뉴 정렬 기준 변경 | `MenuService.sort` 로직 + 각 공급자의 `order` 값 |

---

## `Menu` 도메인 필드

```java
interface Menu {
    String title();       // 표시 이름 (i18n 키 또는 직접 문자열)
    String order();       // 정렬 키 (알파벳순, 뒤로 갈수록 아래)
    String icon();        // FontAwesome 아이콘 클래스 (fa-xxx)
    String iconType();    // "light" | "solid"
    String script();      // GWT nocache.js 경로 (동적 로딩 대상)
    boolean bottom();     // 하단 고정 여부
    List<String> urlRegex(); // 이 메뉴가 자동 선택될 URL 정규식 목록
    // 향후 href 필드 추가 검토 (SEO 랜딩 정적 링크용 — landing.md 참조)
}
```

## `MenuSupplier` 인터페이스

```kotlin
interface MenuSupplier {
    fun menu(headers: Map<String, List<String>>): Flux<Menu>
}
```

- 요청 헤더(인증 쿠키 포함)를 전달받아 상태 기반 분기 가능
- 실패 시 `onErrorResume { Flux.empty() }` 로 graceful degradation

## `/menus` 엔드포인트

```
GET /menus
Accept: application/vnd.sayaya.handbook.v1+json

200 OK
[Menu, Menu, ...]  // order 기준 정렬됨
```

- 인증 불필요 (미인증 시 공개 엔트리만 반환)
- gateway 의 `MenuService` 가 등록된 모든 `MenuSupplier` 를 parallel Scheduler 로 병렬 호출

## 공급자별 현황

| Supplier | 엔트리 | 인증 분기 | 비고 |
|----------|-------|----------|------|
| login | Sign In / Sign Out | principal null 여부 | Z order (최하단) |
| landing-menu (신규) | 앱 내부 "소개" | 항상 공급 (로그인 동일) | 이름/URL 미정 |
| search-type | 타입 목록 | 인증 필요 | 워크스페이스 컨텍스트 필요 |
| search-document | 문서 목록 | 인증 필요 | 워크스페이스 컨텍스트 필요 |
| search-workspace | 워크스페이스 (info/groups/permissions) | 인증 필요 | Drawer 하단 고정 (order=S, bottom=true) |

## shell-ui `UrlBasedMenuResolver` 동작

1. `MenuList` 구독 → 각 Menu 의 `urlRegex()` 를 `Map<JsRegExp, Menu>` 로 등록
2. URI 변경 감지 → 정규식 순회 → 첫 매칭 메뉴 자동 선택
3. `MenuSelected.next(menu)` → 해당 모듈 `script` 동적 로딩
4. 데스크톱: drawer COLLAPSE, 모바일: drawer HIDE (하단 네비만 노출)
