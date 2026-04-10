# Gateway 클래스 다이어그램

## Usecase 계층

```mermaid
classDiagram
    class MenuService {
        -List~MenuSupplier~ suppliers
        +menus(headers: Map~String, List~String~~): Flux~Menu~
    }

    class MenuSupplier {
        <<interface>>
        +menu(headers: Map~String, List~String~~): Flux~Menu~
    }

    MenuService --> MenuSupplier
```

## Interfaces 계층

```mermaid
classDiagram
    class MenuController {
        -MenuService menuService
        +menus(request: ServerHttpRequest): Flux~Menu~
    }

    class ServiceDiscovery {
        -WebClient client
        +menu(headers: Map~String, List~String~~): Flux~Menu~
        -MENU_URI: String$
        -ACCEPT_MEDIA_TYPE: String$
    }

    class ServiceListProperties {
        <<@ConfigurationProperties>>
        +ArrayList~ServiceEntry~
    }

    class ServiceEntry {
        +name: String
    }

    class GatewayConfig {
        <<@Configuration>>
        +objectMapper(): ObjectMapper
        +webClientBuilder(objectMapper): WebClient.Builder
        +menuSuppliers(webClientBuilder, serviceList): List~MenuSupplier~
        +menuService(suppliers): MenuService
    }

    ServiceDiscovery ..|> MenuSupplier
    MenuController --> MenuService
    ServiceListProperties *-- ServiceEntry
    class RateLimitFilter {
        <<@Component>>
        -windowMillis: Long
        -maxRequests: Int
        -counters: ConcurrentHashMap
        +filter(exchange, chain): Mono~Void~
    }

    class CorrelationIdFilter {
        <<@Component @Order(HIGHEST_PRECEDENCE)>>
        +HEADER_NAME: String$
        +MDC_KEY: String$
        +filter(exchange, chain): Mono~Void~
    }

    class FallbackController {
        <<@RestController>>
        +fallbackGet(): ResponseEntity~Map~
        +fallbackPost(): ResponseEntity~Map~
    }

    RateLimitFilter ..|> WebFilter
    CorrelationIdFilter ..|> WebFilter

    GatewayConfig --> ServiceListProperties
    GatewayConfig ..> ServiceDiscovery : creates
    GatewayConfig ..> MenuService : creates
    GatewayConfig ..> CorsWebFilter : creates
```

## 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **Aggregator** | MenuService | 여러 서비스의 메뉴를 병렬로 수집하여 정렬 반환 |
| **Port & Adapter** | MenuSupplier | usecase의 포트를 ServiceDiscovery가 구현 |
| **Graceful Degradation** | MenuService | 개별 서비스 실패 시 해당 서비스의 결과만 무시하고 나머지 반환 |
| **Configuration Properties** | ServiceListProperties | 서비스 목록을 YAML에서 바인딩 |
| **Filter Chain** | RateLimitFilter, CorrelationIdFilter | WebFilter로 횡단 관심사(보안, 관측성) 처리 |
| **Circuit Breaker** | FallbackController + application.yml | 선택 서비스(assistant, event-broadcaster) 장애 시 빈 응답 반환 |
