# Login-UI 모듈

터미널 스타일의 로그인/로그아웃 화면 (GWT). OAuth2 프로바이더 버튼을 제공하며, Shell이 인증 상태에 따라 동적으로 로딩한다.

---

## 엔트리 포인트

| 모듈 | 스크립트 | 트리거 |
|------|---------|--------|
| **로그인** | `login.nocache.js` | MenuController가 SIGN_IN 메뉴 반환 시 |
| **로그아웃** | `logout.nocache.js` | 사용자가 로그아웃 메뉴 선택 시 |

## UI 컴포넌트

| 컴포넌트 | 역할 |
|---------|------|
| `ContentElement` | 메인 컨테이너. 웰컴 ASCII 아트 출력 → 높이 트랜지션(0→20rem) → OAuth 버튼 표시. 내부 트리거(OAuth 에러, 타임아웃, 클릭)를 `handbook-login-command` CustomEvent로 발행 |
| `ConsoleElement` | 터미널 디스플레이. `@Delegate HTMLContainerBuilder`, 라인 버퍼(최대 50줄), 중앙 정렬 토글, 커서 깜빡임 제어 |
| `LineElement` | 타이핑 효과가 적용된 개별 콘솔 라인. `border-right` 기반 블링킹 커서 |
| `AuthenticationProviderButton` | sayaya-ui `TextButton` 기반 OAuth2 버튼. `@Delegate TextButtonElementBuilder`. FontAwesome brands 아이콘 표시 |

## 인터랙션

- **자동 포커스**: 버튼 렌더링 후 첫 번째 버튼에 `select()` → focus + MD3 focus-ring 강제 표시
- **키보드 내비게이션**: ArrowUp/ArrowDown으로 버튼 간 이동
- **사운드 효과**: 포커스 시 `beep.mp3`, 클릭 시 `start.mp3` 재생
- **클릭 딜레이**: `start.mp3` 재생 후 300ms 대기 → OAuth 리다이렉트 실행

## 정적 에셋

| 파일 | 용도 |
|------|------|
| `css/console.css` | 터미널 콘솔 + 블링킹 커서 스타일 |
| `css/login.css` | 로그인 페이지 레이아웃 + OAuth 버튼 + 모바일 대응 |
| `css/brands.min.css` | FontAwesome brands 아이콘 |
| `js/brands.min.js` | FontAwesome brands 스크립트 |
| `wav/beep.mp3` | 버튼 포커스 사운드 |
| `wav/start.mp3` | 버튼 클릭(로그인 시작) 사운드 |
| `src/main/i18n/language.{ko,en}.json` | login.sign_in / login.sign_out 라벨. 빌드 시 app 의 mergeI18nProd 가 다른 모듈과 합쳐 `app/webapp/js/language.{locale}.json` 로 서빙 (shell-ui 의 LabelProvider 가 fetch) |

## 로그인 흐름

1. Shell이 `login.nocache.js`를 로딩
2. CSS(console.css, login.css, brands.min.css) + JS(brands.min.js) 로딩
3. `ConsoleElement`가 ASCII 아트 환영 메시지 출력 (높이 0, 중앙 정렬)
4. 100ms 후 콘솔 높이 20rem으로 트랜지션 확장
5. 100ms 후 "SELECT YOUR AUTHENTICATION PROVIDER:" 프롬프트 출력, 커서 깜빡임 중단
6. OAuth2 프로바이더 버튼 렌더링, 첫 버튼 자동 포커스 + beep 사운드
7. 사용자 클릭 → start 사운드 → 300ms 후 `window.location.href = "oauth2/authorization/{provider}"`
8. Login 백엔드가 인증 처리 후 JWT 쿠키 설정

## 로그아웃 흐름

1. Shell이 `logout.nocache.js`를 로딩
2. `OAuthApi.logout()` → `POST /oauth2/logout`
3. JWT 쿠키 삭제 → 홈으로 리다이렉트

## 커맨드 핸들러

Shell(agent-ui)이 `handbook-login-command` CustomEvent를 통해 로그인 화면을 제어할 수 있다. agent-ui와 동일한 Dispatcher/Router 아키텍처를 사용한다.

### 아키텍처

```
ContentElement  ──(dispatch)──▶  handbook-login-command (CustomEvent)
                                        │
LoginCommandRouter (수신) ──(type 분기)──▶ BehaviorSubject<T>
                                        │
                    ┌───────────────────┼───────────────────┐
                    ▼                   ▼                   ▼
          LoginNotifyHandler   LoginAttentionHandler  LoginProgressHandler ...
```

- `LoginCommandDispatcher` — 커맨드 타입별 Observable 제공 포트 (usecase 계층)
- `LoginCommandRouter` — `handbook-login-command` CustomEvent를 수신하여 `type` 필드 기준으로 BehaviorSubject에 라우팅 (interfaces 계층)
- `ContentElement` — 내부 트리거(OAuth 에러, 타임아웃, 클릭)를 `LoginCommandRouter.dispatch()`로 발행

### 지원 커맨드

| type | 핸들러 | 동작 |
|------|--------|------|
| `notify` | `LoginNotifyHandler` | level(error/warning/info)에 따라 콘솔에 메시지 출력 |
| `attention` | `LoginAttentionHandler` | 5초 미조작 시 안내 메시지 출력 (기본: "Click the button above to sign in") |
| `highlight` | `LoginHighlightHandler` | target CSS 셀렉터의 요소에 `login-highlight` 클래스 토글 |
| `progress` | `LoginProgressHandler` | OAuth 버튼 비활성화 + 진행 메시지 출력 |

### 내부 트리거

| 시점 | 발행되는 커맨드 |
|------|----------------|
| URL에 `?error=...` 존재 | `{type: "notify", level: "error", message: "Authentication failed: ..."}` |
| 버튼 표시 후 5초 미조작 | `{type: "attention"}` |
| OAuth 버튼 클릭 | `{type: "progress"}` |

> 백엔드 인증 흐름은 [login/README.md](../login/README.md) 참조.
> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
