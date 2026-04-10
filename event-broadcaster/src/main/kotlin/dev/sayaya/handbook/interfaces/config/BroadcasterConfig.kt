package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.usecase.Broadcaster
import dev.sayaya.handbook.usecase.WorkspaceSinkManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 이벤트 브로드캐스터의 인프라 설정.
 *
 * **책임:** usecase 계층의 [WorkspaceSinkManager], [Broadcaster]를 Spring Bean으로 등록하고,
 * Jackson ObjectMapper를 snake_case + JavaTime 지원으로 구성한다.
 *
 * **의존관계:**
 * - [WorkspaceSinkManager] — 워크스페이스별 Sink 관리
 * - [Broadcaster] — 이벤트 수신 및 분배
 */
@Configuration
class BroadcasterConfig {
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

    @Bean
    fun workspaceSinkManager() = WorkspaceSinkManager()

    @Bean
    fun broadcaster(objectMapper: ObjectMapper, sinkManager: WorkspaceSinkManager) =
        Broadcaster(objectMapper, sinkManager)
}
