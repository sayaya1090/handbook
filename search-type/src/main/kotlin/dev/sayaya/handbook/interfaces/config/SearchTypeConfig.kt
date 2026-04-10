package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
}
