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

### Client-side synthetic menus

백엔드 `/menus` 집계 밖에서 **Shell 이 런타임에 합성**하는 가상 메뉴. `MenuList` 에 등록되지 않고 `urlRegex` 도 미지정이므로 `UrlBasedMenuResolver` 매칭 대상이 아니며, MenuRail / MobileTabs 에도 노출되지 않는다. 오직 `MenuSelected` 스트림에만 push 되어 기존 `ModuleScriptManager` 파이프라인으로 모듈 스크립트 로드를 유도한다.

| 합성 소스 | 트리거 | 로드 대상 | UC |
|----------|--------|----------|-----|
| `shell-ui/WorkspaceOnboardingBootstrapper` | `WorkspaceList` 가 empty 방출 | `js/workspace/workspace.nocache.js` (workspace-ui Create/Join) | UC-12 / UC-S21 |

**제약**
- `urlRegex` 미지정 → 딥링크·URL 기반 자동 선택 불가. 합성 메뉴는 일시 상태 (도메인 조건 복귀 시 더 이상 발화하지 않음) 에만 사용.
- `loaded` 플래그로 세션 내 1회 발화 보장 — 도메인 조건이 재진입해도 반복 push 금지.
- 외부 에이전트의 navigate 커맨드로 직접 트리거 불가 (MenuList 부재). 에이전트가 유도하려면 도메인 조건을 우회 조작해야 한다.

## 소비자 (Consumers)

- **gateway** — `MenuService` 가 모든 공급자를 병렬 호출해 집계
  - `usecase/MenuService.kt`, `usecase/MenuSupplier.kt`
  - `interfaces/api/MenuController.kt` → `GET /menus`
- **shell-ui** — `UrlBasedMenuResolver` 가 URL의 **정규화된 pathname**을 정규식으로 매칭하여 자동 선택
  - `client/usecase/UrlBasedMenuResolver.java`
  - 정규화 로직: `origin`, `port`, `protocol` 을 제거한 순수 경로(예: `/workspace/123/types`) 만 남겨 매칭.

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 공급자 추가 | gateway Dagger 모듈에 `MenuSupplier` 빈 추가, shell-ui 매칭 로직 동작 확인 |
| Menu 도메인 필드 추가 | 모든 공급자 응답 스키마, shell-ui 렌더링 로직, `application/vnd.sayaya.handbook.v1+json` 미디어 타입 버저닝 |
| URL 정규식 변경 | `UrlBasedMenuResolverTest` + 실제 라우팅 동작 |
| 메뉴 정렬 기준 변경 | `MenuService.sort` 로직 + 각 공급자의 `order` 값 |
| `allowedSessionStates` 추가/변경 | 공급자: 각 메뉴에 노출 허용 `SessionStateKind` 집합 명시 (누락 ⇒ 무제약 노출). 소비자: shell-ui 가시성 평가 알고리즘 (§3.24.4) 재검증. 미디어타입은 **additive 라 v1 유지** |

---

## `Menu` 도메인 필드

```java
interface Menu {
    String title();       // i18n 키 (권장) 또는 표시 리터럴 (fallback)
    String order();       // 정렬 키 (알파벳순, 뒤로 갈수록 아래)
    String icon();        // FontAwesome 아이콘 클래스 (fa-xxx)
    String iconType();    // "light" | "solid"
    String script();      // GWT nocache.js 경로 (동적 로딩 대상)
    boolean bottom();     // 하단 고정 여부 (MenuRail/MobileTabs 내 정렬 힌트)
    String appBarSlot();  // null | "leading" | "center" | "trailing"  ← AppBar 승격 slot
    String url();         // 클릭 시 브라우저 주소창에 반영될 대표 URL (Clean URL)
    List<String> urlRegex(); // 브라우저 URL 기반으로 이 메뉴를 자동 선택(매칭)할 때 사용되는 정규식 목록
    Set<SessionStateKind> allowedSessionStates(); // null/누락 ⇒ 모든 상태 노출 (무제약). 요구사항 §3.24
    // 향후 href 필드 추가 검토 (SEO 랜딩 정적 링크용 — landing.md 참조)
}
```

### `title` i18n 키 규약

- **공급자 (backend)** 는 `title` 에 표시 리터럴이 아닌 **i18n 키**를 넣는다. 형식: `<module>.<snake_case>`.
  - 예: `login.sign_in`, `login.sign_out`, `search.types`, `search.workspaces`
  - 키가 아니어도 동작은 한다 — shell-ui `LabelProvider` 가 매칭 실패 시 리터럴 그대로 렌더.
- **라벨 팩 등록처** 는 **그 메뉴를 소유하는 UI 모듈**의 `src/main/i18n/language.{locale}.json` 이다.
  - 예: `login.sign_*` → `login-ui/src/main/i18n/`
  - 예: `search.workspaces` → `workspace-ui/src/main/i18n/` (예정) 또는 현재처럼 `shell-ui/src/main/i18n/`
  - 빌드 타임에 `app` 의 `mergeI18nProd` 가 모든 모듈의 파일을 하나로 머지해 `app/src/main/webapp/js/language.{locale}.json` 로 출력 → shell-ui 의 LabelProvider 가 fetch.
- **테스트 시**: 각 GWT 모듈 빌드는 `mergeI18n` 을 통해 자기 `test/webapp/js/` 에 머지본을 주입하므로 테스트에서도 동일한 머지된 라벨 팩을 본다.
- 동일 키가 여러 모듈에 등장하면 **머지 순서 의존(마지막 승리)** 이다 — 충돌 방지를 위해 반드시 모듈 namespace 접두(`login.`, `workspace.`, `type.` …) 를 둔다.

### `appBarSlot` 규약 — AppBar 승격

`appBarSlot` 은 공급자가 자신의 메뉴를 네비게이션 축(MenuRail / MobileTabs) 이 아닌
`ShellAppBarElement` 의 AppBar slot 으로 **승격**하도록 요청하는 필드.

| 값 | semantic | 렌더 위치 | 예시 |
|----|----------|----------|------|
| `null` (기본) | 네비게이션 (모듈 전환) | 데스크톱 MenuRail / 모바일 MobileTabs | documents, types, workspaces |
| `"leading"` | 전역 네비 아이콘 | AppBar leading | 예약됨 (현재 미사용) |
| `"center"` | 컨텍스트 셀렉터 / 제목 | AppBar center | 예약됨 — WorkspaceSelect 는 별도 컴포넌트로 직접 배치 |
| `"trailing"` | 세션 / 전역 액션 | AppBar trailing (md-icon-button) | **login** (Sign In / Sign Out) |

**동작 규칙**
- `appBarSlot != null` 메뉴는 MenuRail·MobileTabs 렌더에서 제외된다 (네비게이션 축 오염 방지).
- AppBar 승격 메뉴는 아이콘 버튼 형태로 렌더되고, 클릭 시 일반 `MenuSelected` 이벤트가 발행되어 기존 모듈 로딩 경로(`script` 실행)와 수렴한다.
- 인증 상태 분기(예: login 은 principal null 여부에 따라 SIGN_IN/SIGN_OUT 만 emit)는 공급자 책임이며, UI 는 MenuList 에 존재하는 엔트리만 렌더한다.

### `allowedSessionStates` 규약 (Phase 1)

공급자는 자기 메뉴가 **보여야 하는 세션 상태 집합** (요구사항 §3.24) 을 `allowedSessionStates` 로 선언한다. shell-ui 가 현재 `SessionState.kind` 가 집합에 포함되는지 비교해 가시/활성/CTA 를 결정한다.

```
enum SessionStateKind {
  ANONYMOUS           // 인증 없음 (쿠키 무효/부재)
  AUTHENTICATED       // 로그인 + 활성 워크스페이스 미선택
  IN_WORKSPACE        // 로그인 + 활성 워크스페이스 선택
  // IN_WORKSPACE_AS_ADMIN  // Phase 2 예약 (role 세분화 후)
}
```

**JSON wire 표현**
- 직렬화 이름: `allowed_session_states` (snake_case).
- 값은 `SessionStateKind` 이름의 UPPER_SNAKE_CASE 문자열 **배열** (예: `["AUTHENTICATED", "IN_WORKSPACE"]`).
- **optional / nullable** — 누락 또는 null 이면 소비자는 **모든 상태에서 노출 (무제약)** 으로 간주.
- 빈 배열 `[]` 은 "어떤 상태에서도 노출 금지" 의미 (공급자가 의도적으로 메뉴를 꺼두고 싶을 때). null 과 구분된다.
- v1 미디어타입 유지 (`application/vnd.sayaya.handbook.v1+json`) — additive 필드이므로 구 공급자/소비자와 호환.

> **공급자 주의 — 계층 추론 없음, 명시 열거 필수**
>
> 평가는 **집합 멤버십** (`currentKind ∈ allowedSet`) 만 수행한다. 상위 상태가 하위 상태를 **자동 포함하지 않는다**.
>
> - `{AUTHENTICATED}` 만 선언한 메뉴는 `IN_WORKSPACE` 사용자에게 **보이지 않는다**.
> - "로그인 이후 모두에게 보여야 하는" 메뉴는 반드시 `{AUTHENTICATED, IN_WORKSPACE}` 로 **두 값 모두 열거**.
> - default null ⇒ **모두에게 보임** 이다. 인증 필요 메뉴에서 필드를 빼먹으면 익명 사용자에게도 노출되는 실수가 조용히 발생하므로, 공급자 리뷰 체크리스트에 "필드 기입 여부" 포함을 권장.

**공급자 책임**
- 자기 메뉴의 `allowedSessionStates` 명시. 누락 시 default "무제약" 으로 폴백되므로 인증 필요 메뉴의 누락은 보안적 위험.
- 인증 상태 분기(로그인 공급자의 Sign In/Out 중 하나만 emit 하는 기존 동작, 워크스페이스 공급자의 멤버십 필터링)는 공급자 내부에서 유지 가능. `allowedSessionStates` 는 "이 메뉴가 어떤 상태에서 보여야 하는가" 선언이지 "언제 emit 할지" 를 제어하지 않는다.

**소비자 책임 (shell-ui)**
- 현재 `SessionState` (sealed class) 와 그 `kind` 값을 관찰.
- 평가 알고리즘 (요구사항 §3.24.4) 에 따라 각 메뉴를 enabled / disabled + CTA 로 렌더.
- disabled 메뉴 클릭 시 허용 집합과 현재 상태의 gap 에 맞는 CTA 라우팅 (Sign In · Create/Join Workspace).

**기본값 규칙 요약**
| 필드 | 공급자 누락/null 시 | 빈 배열 `[]` 시 | 비고 |
|------|---------------------|-----------------|------|
| `allowed_session_states` | **무제약 (모든 상태 노출)** | 어떤 상태에서도 숨김 | 기존 4개 공급자 (login · search-type · search-document · search-workspace) 는 마이그레이션 전까지 null default 로 상시 노출. Phase 1 마이그레이션 완료 목표는 §3.24.5 예시 표 참조 |

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

## SLA 및 실패 모드

- **응답 예산:** p95 ≤ 1500ms (`MenuService.AGGREGATE_TIMEOUT` 기본값). 개별 supplier 는 500ms 이내 응답해야 한다 (`ServiceDiscovery` WebClient 타임아웃).
- **부분 성공 허용:** 집계 컷오프 초과 시 그 시점까지 수집된 부분 결과를 그대로 emit (`Flux.take(Duration)` 기반). 전량 드랍 금지 — shell-ui MenuRail 이 빈 상태로 빠지는 silent degradation 을 피하기 위함.
- **개별 실패 격리:** 한 supplier 의 예외/타임아웃은 다른 supplier 결과에 영향을 주지 않는다 (`onErrorResume { Flux.empty() }` 공급자별 적용).
- **미인증 공급자:** 인증 필요 supplier 는 `principal == null` 시 즉시 빈 `Flux` 를 반환 (블로킹 없이 종료). login / landing-menu 처럼 항상 공급하는 supplier 만 엔트리를 내보낸다.

### 공급자별 SLO (예상 p50)

| Supplier | 목표 | 비고 |
|----------|------|------|
| login | < 20ms | 인증 여부 분기만, I/O 없음 |
| landing-menu (신규) | < 50ms | 정적 상수 (gateway 로컬 또는 별도 모듈) |
| search-workspace | < 30ms | 정적 Menu 상수 1개 emit, DB 비접근 |
| search-type | < 100ms | R2DBC read-only, cold start 시 pod 기동 지연 예외 |
| search-document | < 100ms | R2DBC read-only |

## 공급자별 현황

| Supplier | 엔트리 | 인증 분기 | Phase 1 목표 `allowedSessionStates` | 비고 |
|----------|-------|----------|--------------------------------------|------|
| login | Sign In | principal null 여부 | `{ANONYMOUS}` | 공급자 내부에서 익명일 때만 emit. 선언값도 익명 전용으로 일치 |
| login | Sign Out | principal 존재 여부 | `{AUTHENTICATED, IN_WORKSPACE}` | 공급자 내부에서 로그인 시만 emit. **두 값 명시 열거** (계층 추론 없음) |
| landing-menu (신규) | 앱 내부 "소개" | 항상 공급 | `null` (무제약) | 모든 상태에서 상시 노출. 이름/URL 미정 |
| search-type | 타입 목록 | 인증 필요 | `{IN_WORKSPACE}` | 워크스페이스 컨텍스트 필요 |
| search-document | 문서 목록 | 인증 필요 | `{IN_WORKSPACE}` | 워크스페이스 컨텍스트 필요 |
| search-workspace | 워크스페이스 (info/groups/permissions) | 인증 필요 | `{AUTHENTICATED, IN_WORKSPACE}` | Drawer 하단 고정 (order=S, bottom=true) |
| workspace-onboarding (신규 / search-workspace 내) | 워크스페이스 생성/참여 | 인증 필요 | `{AUTHENTICATED, IN_WORKSPACE}` | `AUTHENTICATED` 에 enabled 로 노출되어 `WorkspaceOnboardingBootstrapper` synthetic 메뉴를 대체 |

## `urlRegex` 매칭 규약

- **정규화 기준**: `UrlBasedMenuResolver`는 매칭 전 브라우저 URI에서 `origin`(프로토콜+호스트+포트)을 제거하고, 해시(`#`)를 제거하며, 반드시 `/`로 시작하는 **pathname**으로 정규화한다.
- **매칭 방식**: `new JsRegExp(regex).test(normalizedPath)`
- **선행 슬래시(/) 필수**: 브라우저 URI가 항상 `/`로 정규화되므로, `urlRegex` 패턴은 반드시 `/`로 시작해야 한다. (예: `^types` → `^/types`). 누락 시 직접 접속 시 메뉴 매칭에 실패한다.
- **공급자 권장사항**: `urlRegex`는 반드시 `/`로 시작하는 정규식을 제공해야 하며, 전체 URL이 아닌 상대 경로(pathname)를 기준으로 작성해야 한다. (예: `^/workspaces$`)
- **서버 연동**: 모든 `urlRegex` 경로에 대해 브라우저 직접 접속 시 SPA 진입점(`app.html`)이 반환되도록 Gateway 설정이 동기화되어야 한다. 현재 `gateway` 모듈의 `ui-clean-urls` 라우트가 이 역할을 수행한다. (주요 UI 경로 + `Accept: text/html` 매칭 시 `/app.html` 포워딩)
