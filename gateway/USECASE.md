# Gateway 유스케이스

## 메뉴 집계 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (shell-ui)
    participant GW as Gateway (MenuController)
    participant Svc as MenuService
    participant SD1 as ServiceDiscovery (search-type)
    participant SD2 as ServiceDiscovery (search-document)
    participant SDn as ServiceDiscovery (...)
    participant Svc1 as search-type /menus
    participant Svc2 as search-document /menus

    Client->>GW: GET /menus
    Note over Client,GW: Accept: application/vnd.sayaya.handbook.v1+json
    GW->>Svc: menus(headers)
    Svc->>SD1: menu(headers) [parallel]
    Svc->>SD2: menu(headers) [parallel]
    Svc->>SDn: menu(headers) [parallel]
    SD1->>Svc1: GET /menus (WebClient, timeout 1200ms)
    SD2->>Svc2: GET /menus (WebClient, timeout 1200ms)
    Svc1-->>SD1: Flux<Menu>
    Svc2-->>SD2: Flux<Menu>
    Note over Svc: 개별 서비스 실패 시 onErrorResume → Flux.empty()
    Svc->>Svc: 병렬 수집 후 order 기준 정렬
    Svc-->>GW: Flux<Menu>
    GW-->>Client: 200 OK + 정렬된 메뉴 목록
```

## API 라우팅 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant GW as Spring Cloud Gateway
    participant Target as 대상 마이크로서비스

    Client->>GW: HTTP 요청
    GW->>GW: Route Predicate 평가 (Path + Method)
    alt 매칭되는 라우트 존재
        GW->>Target: 요청 프록시
        Target-->>GW: 응답
        GW-->>Client: 응답 전달
    else 매칭 실패
        GW-->>Client: 404 Not Found
    end
```

## event-broadcaster 프록시 시퀀스

```mermaid
sequenceDiagram
    actor Client as 클라이언트 (shell-ui)
    participant GW as Gateway
    participant EB as event-broadcaster

    Client->>GW: GET /workspace/{id}/messages
    Note over GW: Route: event-broadcaster (Path + Method=GET)
    GW->>EB: SSE 프록시 연결
    EB-->>GW: text/event-stream
    GW-->>Client: SSE 스트림 전달
    loop 연결 유지
        EB-->>GW: SSE 이벤트 / ping
        GW-->>Client: SSE 이벤트 / ping
    end
```

---

## UC-GW1: API 라우팅

| 항목 | 내용 |
|------|------|
| **액터** | 클라이언트 (브라우저, 외부 시스템) |
| **선행조건** | Gateway 서버 구동 및 라우트 설정 완료 |
| **정상 흐름** | 1. 클라이언트가 HTTP 요청을 Gateway로 전송한다.<br>2. Spring Cloud Gateway가 `application.yml`에 정의된 라우트 목록에서 Path/Method Predicate를 평가한다.<br>3. 매칭된 라우트의 대상 URI로 요청을 프록시한다.<br>4. 대상 서비스의 응답을 클라이언트에 전달한다. |
| **라우트 목록** | `login` (/auth/**, /user), `event-broadcaster` (/workspace/*/messages GET), `search-type` (/workspace/*/types/**, /workspace/*/layouts/** GET), `persist-type` (PUT,DELETE), `search-document` (/workspace/*/documents/**, /workspace/*/*/*  GET), `persist-document` (PUT,DELETE), `persist-workspace` (POST,PUT,DELETE), `assistant` (/assistant/**), `static` (/js/**, /css/**, /icons/**) |
| **결과** | 클라이언트 요청이 적절한 마이크로서비스로 라우팅되어 응답을 수신 |

## UC-GW2: 메뉴 집계

| 항목 | 내용 |
|------|------|
| **액터** | 클라이언트 (shell-ui 경유) |
| **선행조건** | `ServiceListProperties`에 메뉴 제공 서비스 목록이 등록되어 있음 |
| **정상 흐름** | 1. 클라이언트가 `GET /menus`를 요청한다.<br>2. `MenuController`가 요청 헤더를 추출하여 `MenuService.menus(headers)`를 호출한다.<br>3. `MenuService`가 등록된 모든 `MenuSupplier`에 병렬로 `/menus`를 요청한다.<br>4. 각 `ServiceDiscovery`가 WebClient로 대상 서비스의 `/menus`를 호출한다 (타임아웃 1200ms).<br>5. 개별 서비스 실패 시 `onErrorResume`으로 빈 결과를 반환하여 graceful degradation한다.<br>6. 수집된 메뉴를 `order` 기준으로 정렬하여 반환한다. |
| **결과** | 200 OK + 전체 서비스의 메뉴를 통합·정렬한 목록 |

## UC-GW3: 인증 필터

| 항목 | 내용 |
|------|------|
| **액터** | 클라이언트 (인증된 사용자) |
| **선행조건** | 클라이언트가 유효한 JWT 토큰을 보유 (쿠키) |
| **정상 흐름** | 1. 클라이언트가 API 요청 시 쿠키에 JWT 토큰을 포함하여 전송한다.<br>2. Gateway가 요청 헤더를 그대로 하위 서비스로 전달한다.<br>3. 각 하위 서비스는 `authentication` 모듈을 통해 JWT를 검증한다.<br>4. 인증 정보(Principal)가 컨트롤러에서 사용 가능해진다. |
| **비고** | Gateway 자체는 별도의 인증 필터를 구현하지 않으며, 요청 헤더(쿠키 포함)를 하위 서비스에 전파하여 각 서비스가 `authentication` 라이브러리로 JWT를 검증한다. 메뉴 집계 시에도 헤더를 전달하여 인증 컨텍스트를 유지한다. |
| **결과** | 인증 정보가 하위 서비스로 전파됨 |

## UC-GW4: event-broadcaster 프록시

| 항목 | 내용 |
|------|------|
| **액터** | 클라이언트 (shell-ui 경유) |
| **선행조건** | event-broadcaster 서비스 구동 중 |
| **정상 흐름** | 1. 클라이언트가 `GET /workspace/{id}/messages`를 요청한다.<br>2. Gateway의 라우트 설정에서 `event-broadcaster` 라우트가 매칭된다 (Path: `/workspace/*/messages`, Method: GET).<br>3. Gateway가 SSE 연결을 event-broadcaster로 프록시한다.<br>4. event-broadcaster의 `text/event-stream` 응답이 클라이언트에 그대로 전달된다.<br>5. SSE 스트림이 종료될 때까지 연결이 유지된다. |
| **비고** | SSE 라우트는 `search-document` 라우트보다 먼저 정의되어 우선 매칭된다 |
| **결과** | 클라이언트가 워크스페이스별 실시간 이벤트 스트림을 수신 |

---

## 트레이서빌리티 매트릭스

| UC | 요구사항 | 시퀀스 다이어그램 | 주요 클래스 | 테스트 |
|----|---------|---|---|---|
| UC-GW1 (API 라우팅) | 3.9, 아키텍처 | API 라우팅 | application.yml (Spring Cloud Gateway Routes) | - |
| UC-GW2 (메뉴 집계) | 3.11 (Shell - Menu Rail) | 메뉴 집계 | MenuController, MenuService, MenuSupplier, ServiceDiscovery, ServiceListProperties, GatewayConfig | - |
| UC-GW3 (인증 필터) | 3.8 | — (헤더 전파) | application.yml (라우트), authentication 모듈 | - |
| UC-GW4 (SSE 프록시) | 3.9 (SSE 실시간 알림) | event-broadcaster 프록시 | application.yml (event-broadcaster 라우트) | - |
