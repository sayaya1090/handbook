

`JsonConfig` 클래스는 Spring Boot 환경에서 `ObjectMapper`를 구성하는 설정 클래스로, Jackson 라이브러리를 활용하여 JSON 직렬화/역직렬화 동작 방식을 미세 조정합니다. `@ConditionalOnMissingBean(ObjectMapper::class)`를 사용하여 `ObjectMapper` 빈이 정의되지 않은 경우에만 이 설정이 적용되도록 되어 있습니다.

`PageResponseAdvice` 클래스는 WebFlux 환경에서 `Page` 데이터를 반환하는 API 응답을 커스터마이즈하는 클래스로 보입니다. 주된 역할은 **Spring Data JPA의 `Page` 객체**를 처리하고, 그 메타데이터(`X-Total-Count`, `X-Total-Pages` 등)를 HTTP 헤더로 추가하는 기능을 제공합니다.