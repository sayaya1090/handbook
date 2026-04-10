package dev.sayaya.handbook.interfaces.discovery

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 메뉴를 제공하는 서비스 목록을 프로퍼티에서 바인딩한다.
 *
 * ```yaml
 * services:
 *   - name: search-type
 *   - name: search-document
 * ```
 */
@ConfigurationProperties(prefix = "services")
class ServiceListProperties : ArrayList<ServiceListProperties.ServiceEntry>() {
    data class ServiceEntry(val name: String)
}
