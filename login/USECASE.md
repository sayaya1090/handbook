# Login 유스케이스

## OAuth2 로그인 → JWT 발행 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Browser as 브라우저
    participant Login as Login 서버 (LoginSecurityConfig)
    participant OAuth as OAuth2 Provider (Google 등)
    participant TP as TokenPublisher
    participant Repo as UserRepository
    participant DB as PostgreSQL
    participant TF as TokenFactory

    User->>Browser: 로그인 버튼 클릭
    Browser->>Login: GET /oauth2/authorization/{provider}
    Login->>OAuth: OAuth2 인가 요청 (redirect)
    OAuth-->>Browser: 로그인 페이지 표시
    User->>OAuth: 자격증명 입력
    OAuth-->>Login: 인가 코드 콜백
    Login->>OAuth: 액세스 토큰 요청
    OAuth-->>Login: OAuth2User (principal)
    Note over Login: authenticationSuccessHandler 실행
    Login->>TP: publish(provider, principal)
    TP->>Repo: findUserByProviderAndAccount(provider, account)
    Repo->>DB: SELECT * FROM user WHERE provider=:p AND account=:a
    alt "기존 사용자"
        DB-->>Repo: R2dbcUserEntity
        Repo-->>TP: User
        TP->>Repo: updateLastLoginDateTime(id, now)
        Repo->>DB: UPDATE user SET last_login_at=:now
    else "신규 사용자"
        DB-->>Repo: empty
        TP->>TP: createUser(provider, principal)
        TP->>Repo: create(user)
        Repo->>DB: INSERT INTO user
        DB-->>Repo: 저장된 User
    end
    TP->>TF: publish(user)
    TF->>TF: user.toToken(nbf, exp, iss, iat)
    TF->>TF: sign(token) — RSA 개인키로 JWT 서명
    TF-->>TP: JWT 문자열
    TP-->>Login: JWT 문자열
    Login->>Login: sendAuthenticationCookie(token)
    Note over Login: HttpOnly, Secure, SameSite=LAX 쿠키 설정
    Login-->>Browser: 302 Redirect + Set-Cookie (JWT)
    Browser->>Browser: loginRedirectUri로 이동
```

## 토큰 갱신 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Browser as 브라우저
    participant Filter as AuthenticationWebFilter
    participant JWT as JwtAuthenticationManager
    participant Ctrl as TokenRefreshController
    participant TP as TokenPublisher
    participant TF as TokenFactory

    Browser->>Filter: GET /auth/refresh (Cookie: JWT)
    Filter->>JWT: JWT 쿠키 검증
    JWT-->>Filter: UserAuthentication
    Filter->>Ctrl: refresh(authentication, exchange)
    Ctrl->>TP: validateRefreshToken(authentication)
    TP->>TP: authentication → User 변환 (id, roles 추출)
    TP->>TF: publish(user)
    TF->>TF: 새 토큰 생성 (새 nbf, exp, iat)
    TF-->>TP: 새 JWT 문자열
    TP-->>Ctrl: 새 JWT 문자열
    Ctrl->>Ctrl: sendAuthenticationCookie(newToken)
    Ctrl-->>Browser: 200 OK + Set-Cookie (새 JWT)
```

## 로그아웃 시퀀스

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Browser as 브라우저
    participant Login as Login 서버 (LoginSecurityConfig)

    User->>Browser: 로그아웃 클릭
    Browser->>Login: POST /oauth2/logout
    Login->>Login: SecurityContextServerLogoutHandler + WebSessionServerLogoutHandler
    Login->>Login: clearAuthenticationCookie()
    Note over Login: JWT 쿠키 maxAge=0으로 삭제
    Login-->>Browser: 302 Redirect + Set-Cookie (삭제)
    Browser->>Browser: logoutRedirectUri로 이동
```

## 메뉴 조회 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (shell-ui)
    participant Ctrl as MenuController

    Client->>Ctrl: GET /menus
    alt "미인증 (anonymous)"
        Ctrl-->>Client: [SIGN_IN 메뉴]
        Note over Client: login.nocache.js 스크립트 포함
    else "인증됨"
        Ctrl-->>Client: [SIGN_OUT 메뉴]
        Note over Client: logout.nocache.js 스크립트 포함
    end
```

---

## UC-L1: OAuth2 로그인 및 JWT 발행

| 항목 | 내용 |
|------|------|
| **액터** | 사용자 |
| **선행조건** | OAuth2 프로바이더(Google 등) 설정 완료 |
| **정상 흐름** | 1. 사용자가 로그인 버튼을 클릭하여 `/oauth2/authorization/{provider}`로 이동한다.<br>2. OAuth2 프로바이더에서 인증 후 콜백으로 `OAuth2User`가 반환된다.<br>3. `LoginSecurityConfig`의 `authenticationSuccessHandler`가 `TokenPublisher.publish()`를 호출한다.<br>4. `UserRepository`에서 provider+account로 사용자를 조회한다.<br>5. 기존 사용자면 `lastLoginDateTime`을 갱신하고, 신규면 `USER` 역할로 생성한다.<br>6. `TokenFactory`가 RSA 개인키로 JWT를 서명하여 발행한다.<br>7. JWT를 HttpOnly/Secure 쿠키로 설정하고 `loginRedirectUri`로 리다이렉트한다. |
| **결과** | 302 Redirect + JWT 쿠키 설정 |

## UC-L2: 토큰 갱신

| 항목 | 내용 |
|------|------|
| **액터** | 인증된 사용자 |
| **선행조건** | 유효한 JWT 쿠키 보유 |
| **정상 흐름** | 1. 클라이언트가 `GET /auth/refresh`를 요청한다.<br>2. `AuthenticationWebFilter`가 쿠키의 JWT를 검증하여 `UserAuthentication`을 생성한다.<br>3. `TokenRefreshController`가 `TokenPublisher.validateRefreshToken()`을 호출한다.<br>4. 기존 인증 정보(id, roles)에서 새 만료 시간으로 JWT를 재발행한다.<br>5. 새 JWT를 쿠키로 설정하여 응답한다. |
| **결과** | 200 OK + 갱신된 JWT 쿠키 |

## UC-L3: 로그아웃

| 항목 | 내용 |
|------|------|
| **액터** | 인증된 사용자 |
| **선행조건** | 로그인 상태 |
| **정상 흐름** | 1. 클라이언트가 `POST /oauth2/logout`을 요청한다.<br>2. `DelegatingServerLogoutHandler`가 SecurityContext와 WebSession을 정리한다.<br>3. JWT 쿠키를 `maxAge=0`으로 삭제한다.<br>4. `logoutRedirectUri`로 리다이렉트한다. |
| **결과** | 302 Redirect + JWT 쿠키 삭제 |

## UC-L4: 메뉴 제공

| 항목 | 내용 |
|------|------|
| **액터** | shell-ui |
| **선행조건** | 없음 |
| **정상 흐름** | 1. 클라이언트가 `GET /menus`를 요청한다.<br>2. 인증 여부에 따라 SIGN_IN 또는 SIGN_OUT 메뉴를 반환한다.<br>3. 각 메뉴에는 title, icon, script 경로, tools 정보가 포함된다. |
| **결과** | 200 OK + 메뉴 목록 |

---

## 트레이서빌리티 매트릭스

| UC | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---|---|---|
| UC-L1 (로그인) | OAuth2 로그인 → JWT 발행 | LoginSecurityConfig, TokenPublisher, TokenFactory, UserRepository, R2dbcUserRepositoryDelegate, R2dbcUserEntity, Token, User | LoginSecurityConfigTest, TokenFactoryTest, TokenPublisherTest, UserRepositoryTest |
| UC-L2 (토큰 갱신) | 토큰 갱신 | TokenRefreshController, LoginSecurityConfig, TokenPublisher, TokenFactory, AuthenticationWebFilter, JwtAuthenticationManager | LoginSecurityConfigTest |
| UC-L3 (로그아웃) | 로그아웃 | LoginSecurityConfig (logout 설정) | LoginSecurityConfigTest |
| UC-L4 (메뉴) | 메뉴 조회 | MenuController, Menu, Tool | MenuControllerTest |
