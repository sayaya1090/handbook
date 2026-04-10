package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.interfaces.discovery.ServiceDiscovery
import dev.sayaya.handbook.interfaces.discovery.ServiceListProperties
import dev.sayaya.handbook.usecase.MenuService
import dev.sayaya.handbook.usecase.MenuSupplier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.client.WebClient

/**
 * 게이트웨이 인프라 설정.
 *
 * **책임:** Jackson ObjectMapper(snake_case + JavaTime), WebClient(커스텀 코덱),
 * 서비스 디스커버리([ServiceDiscovery]), [MenuService]를 Bean으로 등록한다.
 *
 * **의존관계:**
 * - [ServiceListProperties] — 메뉴 제공 서비스 목록 (application.yml)
 * - [ServiceDiscovery] — WebClient 기반 메뉴 조회 어댑터
 */
@Configuration
@EnableConfigurationProperties(ServiceListProperties::class)
class GatewayConfig {
    @Bean
    fun objectMapper(): ObjectMapper = JsonMapper.builder()
        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    @Suppress("DEPRECATION")
    @Bean
    fun webClientBuilder(objectMapper: ObjectMapper): WebClient.Builder {
        return WebClient.builder().codecs { configurer ->
            configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
        }
    }

    @Bean
    fun menuSuppliers(
        webClientBuilder: WebClient.Builder,
        serviceList: ServiceListProperties,
    ): List<MenuSupplier> = serviceList.map { ServiceDiscovery(webClientBuilder, it.name) }

    @Bean
    fun menuService(suppliers: List<MenuSupplier>) = MenuService(suppliers)

    @Bean
    fun corsWebFilter(
        @Value("\${cors.allowed-origins:http://localhost:8080}") origins: String,
    ): CorsWebFilter {
        val config = CorsConfiguration().apply {
            allowedOrigins = origins.split(",").map { it.trim() }
            allowedMethods = listOf(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name(),
            )
            allowedHeaders = listOf("Authorization", "Content-Type", "X-Correlation-Id")
            maxAge = 3600L
            allowCredentials = true
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return CorsWebFilter(source)
    }
}
