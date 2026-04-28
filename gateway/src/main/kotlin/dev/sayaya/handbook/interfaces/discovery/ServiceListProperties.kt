package dev.sayaya.handbook.interfaces.discovery

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 메뉴를 제공하는 서비스 목록을 프로퍼티에서 바인딩하는 설정 클래스.
 *
 * **책임:** `services` 프로퍼티 하위의 서비스 엔트리 목록을 바인딩하여,
 * [GatewayConfig][dev.sayaya.handbook.interfaces.config.GatewayConfig]에서 [ServiceDiscovery] 인스턴스를 생성하는 데 사용한다.
 *
 * **주의:** ArrayList를 상속하므로 프로퍼티 바인딩 시 리스트 형태로 직접 사용된다.
 *
 * ```yaml
 * services:
 *   - name: type-query
 *   - name: document-query
 * ```
 */
@ConfigurationProperties(prefix = "services")
class ServiceListProperties : ArrayList<ServiceListProperties.ServiceEntry>() {
    data class ServiceEntry(val name: String)
}
