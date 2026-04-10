# Login 클래스 다이어그램

## Usecase 계층

```mermaid
classDiagram
    class TokenPublisher {
        -UserRepository userRepository
        -TokenFactory factory
        +publish(provider: String, principal: OAuth2User): Mono~String~
        +validateRefreshToken(authentication: UserAuthentication): Mono~String~
        -createUser(provider: String, principal: OAuth2User): Mono~User~
    }

    class TokenFactory {
        -PrivateKey privateKey
        -TokenFactoryConfig config
        -ObjectMapper objectMapper
        +publish(user: User): String
        -sign(payload: Token): String
        -pemToPrivateKey(pemData: String): PrivateKey
    }

    class UserRepository {
        <<interface>>
        +findUserById(id: UUID): Mono~User~
        +findUserByProviderAndAccount(provider: String, account: String): Mono~User~
        +create(user: User): Mono~User~
        +updateLastLoginDateTime(id: UUID, lastLoginDateTime: LocalDateTime): Mono~Void~
    }

    class TokenFactoryConfig {
        +signatureAlgorithm: String
        +duration: Long
        +publisher: String
        +client: String
    }

    TokenPublisher --> UserRepository
    TokenPublisher --> TokenFactory
    TokenFactory --> TokenFactoryConfig
```

## Domain 계층

```mermaid
classDiagram
    class User {
        +id: UUID
        +provider: String
        +account: String
        +name: String
        +roles: MutableList~Role~
        +lastLoginDateTime: LocalDateTime
        +toToken(nbf, exp, iss, iat): Token
    }

    class Token {
        +nbf: LocalDateTime
        +exp: LocalDateTime
        +iss: String
        +iat: LocalDateTime
        +authorities: List~String~
        +name: String
        +id: String
    }

    class Role {
        <<interface>>
    }

    class SystemRole {
        <<enum>>
        ADMIN
        USER
    }

    class State {
        <<enum>>
        ACTIVATED
        INACTIVATED
    }

    User --> Role
    SystemRole ..|> Role
    User ..> Token : creates
```

## Interfaces 계층

```mermaid
classDiagram
    class LoginSecurityConfig {
        -AuthenticationConfig authConfig
        -AuthenticationUrlConfig urlConfig
        -TokenPublisher tokenPublisher
        -TokenFactoryConfig tokenConfig
        -JwtAuthenticationConverter jwtAuthenticationConverter
        -AuthenticationWebFilter authenticationWebFilter
        +securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain
        +sendAuthenticationCookie(token: String): Mono~Void~
        -clearAuthenticationCookie(): Mono~Void~
        -redirect()
    }

    class TokenRefreshController {
        -TokenPublisher tokenPublisher
        -LoginSecurityConfig config
        +refresh(authentication: UserAuthentication, exchange: ServerWebExchange): Mono~Void~
    }

    class UserController {
        +user(authentication: UserAuthentication): Mono~User~
    }

    class MenuController {
        +menus(principal: Principal?): Flux~Menu~
        +SIGN_IN: Menu$
        +SIGN_OUT: Menu$
    }

    class LoginConfig {
        <<Configuration>>
    }

    class AuthenticationUrlConfig {
        +loginRedirectUri: String
        +logoutRedirectUri: String
    }

    class R2dbcUserEntity {
        +id: UUID
        +provider: String
        +account: String
        +name: String
        +state: State
        +createDateTime: LocalDateTime
        +lastLoginDateTime: LocalDateTime
        +lastModifyDateTime: LocalDateTime
        +isNew(): Boolean
    }

    class R2dbcUserRepository {
        <<interface>>
        +findByProviderAndAccount(provider, account): Mono~R2dbcUserEntity~
        +updateLastLoginDateTimeById(id, lastLoginAt): Mono~Int~
    }

    class R2dbcUserRepositoryDelegate {
        -R2dbcUserRepository repo
        +findUserById(id): Mono~User~
        +findUserByProviderAndAccount(provider, account): Mono~User~
        +create(user): Mono~User~
        +updateLastLoginDateTime(id, lastLoginDateTime): Mono~Void~
    }

    class JsonConfig {
        <<Configuration>>
        +objectMapper(): ObjectMapper
    }

    class R2dbcConfig {
        <<Configuration>>
    }

    TokenRefreshController --> TokenPublisher
    TokenRefreshController --> LoginSecurityConfig
    LoginSecurityConfig --> TokenPublisher
    LoginSecurityConfig --> AuthenticationUrlConfig
    R2dbcUserRepositoryDelegate ..|> UserRepository
    R2dbcUserRepositoryDelegate --> R2dbcUserRepository
    R2dbcUserRepositoryDelegate --> R2dbcUserEntity
```

## 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **Port & Adapter (Hexagonal)** | UserRepository | usecase의 포트 인터페이스를 R2dbcUserRepositoryDelegate가 구현 |
| **Factory** | TokenFactory | RSA 개인키와 설정으로 JWT 토큰 생성을 캡슐화 |
| **Cookie-based Authentication** | SecurityConfig | Stateless JWT를 HttpOnly 쿠키로 전달하여 CSRF/XSS 방어 |
| **Auto-Registration** | TokenPublisher.createUser() | OAuth2 최초 로그인 시 자동으로 사용자 생성 (USER 역할 부여) |
| **Delegate** | R2dbcUserRepositoryDelegate | Spring Data R2dbcRepository를 감싸서 도메인 변환 담당 |
