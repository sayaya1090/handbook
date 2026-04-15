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
import dev.sayaya.handbook.interfaces.api.SearchArgumentResolver
import dev.sayaya.handbook.interfaces.database.R2dbcDocumentSearchRepository
import dev.sayaya.handbook.interfaces.database.R2dbcStatsRepository
import dev.sayaya.handbook.usecase.DocumentSearchService
import dev.sayaya.handbook.usecase.StatsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

@Configuration
class SearchDocumentConfig : WebFluxConfigurer {
    @Bean
    fun objectMapper(): ObjectMapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultVisibility { it.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    @Bean
    fun documentSearchRepository(template: R2dbcEntityTemplate, objectMapper: ObjectMapper) =
        R2dbcDocumentSearchRepository(template, objectMapper)

    @Bean
    fun documentSearchService(repo: R2dbcDocumentSearchRepository) = DocumentSearchService(repo)

    @Bean
    fun statsRepository(databaseClient: DatabaseClient) = R2dbcStatsRepository(databaseClient)

    @Bean
    fun statsService(repo: R2dbcStatsRepository) = StatsService(repo)

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(SearchArgumentResolver())
    }
}
