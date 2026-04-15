package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.usecase.LayoutSearchRepository
import dev.sayaya.handbook.usecase.LayoutSearchService
import dev.sayaya.handbook.usecase.TypeSearchRepository
import dev.sayaya.handbook.usecase.TypeSearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SearchTypeConfig {
    @Bean fun typeSearchService(repo: TypeSearchRepository) = TypeSearchService(repo)
    @Bean fun layoutSearchService(repo: LayoutSearchRepository) = LayoutSearchService(repo)

    @Bean fun objectMapper(): ObjectMapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultVisibility { it.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
}
