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
| `ContentElement` | 메인 컨테이너, 터미널 인터페이스 초기화 |
| `ConsoleElement` | 터미널 디스플레이 (라인 버퍼, ASCII 아트) |
| `LineElement` | 타이핑 효과가 적용된 개별 콘솔 라인 |
| `AuthenticationProviderButton` | OAuth2 프로바이더 버튼 (예: Google) |

## 로그인 흐름

1. Shell이 `login.nocache.js`를 로딩
2. `ConsoleElement`가 ASCII 아트 애니메이션 표시
3. OAuth2 프로바이더 버튼 렌더링
4. 사용자 클릭 → `window.location.href = "/oauth2/authorization/{provider}"`
5. Login 백엔드가 인증 처리 후 JWT 쿠키 설정

## 로그아웃 흐름

1. Shell이 `logout.nocache.js`를 로딩
2. `OAuthApi.logout()` → `POST /oauth2/logout`
3. JWT 쿠키 삭제 → 홈으로 리다이렉트

> 백엔드 인증 흐름은 [login/README.md](../login/README.md) 참조.
> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.
