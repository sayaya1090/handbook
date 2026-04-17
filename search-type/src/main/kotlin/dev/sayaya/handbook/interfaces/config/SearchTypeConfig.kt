package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.interfaces.database.AttributeEntityMapper
import dev.sayaya.handbook.interfaces.database.R2dbcAttributeEntityRepository
import dev.sayaya.handbook.interfaces.database.R2dbcLayoutEntityRepository
import dev.sayaya.handbook.interfaces.database.R2dbcLayoutSearchRepositoryAdapter
import dev.sayaya.handbook.interfaces.database.R2dbcTypeEntityRepository
import dev.sayaya.handbook.interfaces.database.R2dbcTypeSearchRepositoryAdapter
import dev.sayaya.handbook.usecase.LayoutSearchRepository
import dev.sayaya.handbook.usecase.LayoutSearchService
import dev.sayaya.handbook.usecase.TypeSearchRepository
import dev.sayaya.handbook.usecase.TypeSearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

/**
 * search-type DI 구성.
 *
 * R2DBC read-only 어댑터 빈과 Repository 포트 구현을 등록한다.
 * 어댑터 클래스는 @Component 없는 순수 class 라 @Bean 명시가 필수 — 생략 시 bean 해석
 * 실패로 CrashLoopBackOff.
 */
@Configuration
class SearchTypeConfig {
    @Bean fun attributeEntityMapper(objectMapper: ObjectMapper) = AttributeEntityMapper(objectMapper)

    @Bean fun typeSearchRepository(
        typeRepo: R2dbcTypeEntityRepository,
        attrRepo: R2dbcAttributeEntityRepository,
        attrMapper: AttributeEntityMapper,
    ): TypeSearchRepository = R2dbcTypeSearchRepositoryAdapter(typeRepo, attrRepo, attrMapper)

    @Bean fun layoutSearchRepository(
        repo: R2dbcLayoutEntityRepository,
        objectMapper: ObjectMapper,
    ): LayoutSearchRepository = R2dbcLayoutSearchRepositoryAdapter(repo, objectMapper)

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
