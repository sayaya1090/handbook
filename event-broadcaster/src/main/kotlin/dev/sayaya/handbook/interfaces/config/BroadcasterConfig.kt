package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.usecase.Broadcaster
import dev.sayaya.handbook.usecase.WorkspaceSinkManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 이벤트 브로드캐스터의 인프라 설정.
 * usecase 계층의 객체들을 Spring Bean으로 등록하고, Jackson ObjectMapper를 구성한다.
 */
@Configuration
class BroadcasterConfig {
    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    @Bean
    fun workspaceSinkManager() = WorkspaceSinkManager()

    @Bean
    fun broadcaster(objectMapper: ObjectMapper, sinkManager: WorkspaceSinkManager) =
        Broadcaster(objectMapper, sinkManager)
}
