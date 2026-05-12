package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.interfaces.database.*
import dev.sayaya.handbook.interfaces.event.KafkaTypeEventPublisher
import dev.sayaya.handbook.usecase.LayoutService
import dev.sayaya.handbook.usecase.TypeEventPublisher
import dev.sayaya.handbook.usecase.TypeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

/**
 * type-command 모듈의 Spring Bean 설정.
 *
 * **책임:** usecase 계층의 서비스/포트 구현체를 Spring Bean으로 등록하고,
 * Jackson ObjectMapper를 snake_case + JavaTime 지원으로 구성한다.
 *
 * **의존관계:**
 * - [AttributeEntityMapper] — 속성 엔티티 ↔ 도메인 매퍼
 * - [R2dbcTypeRepositoryAdapter] — 타입 영속화 어댑터
 * - [R2dbcLayoutRepositoryAdapter] — 레이아웃 영속화 어댑터
 * - [KafkaTypeEventPublisher] — Kafka 이벤트 발행 어댑터
 * - [TypeService] — 타입 비즈니스 로직
 * - [LayoutService] — 레이아웃 비즈니스 로직
 */
@Configuration
class TypeConfig {
    @Bean
    fun objectMapper(): ObjectMapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultVisibility { it.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .addModule(dev.sayaya.handbook.interfaces.jackson.JsPropertyMapModule())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    @Bean
    fun attributeEntityMapper(objectMapper: ObjectMapper) = AttributeEntityMapper(objectMapper)

    @Bean
    fun typeRepositoryAdapter(
        typeRepo: R2dbcTypeEntityRepository,
        attrRepo: R2dbcAttributeEntityRepository,
        attrMapper: AttributeEntityMapper,
        tx: TransactionalOperator,
        databaseClient: DatabaseClient,
    ) = R2dbcTypeRepositoryAdapter(typeRepo, attrRepo, attrMapper, tx, databaseClient)

    @Bean
    fun layoutRepositoryAdapter(repository: R2dbcLayoutEntityRepository, objectMapper: ObjectMapper) =
        R2dbcLayoutRepositoryAdapter(repository, objectMapper)

    @Bean
    fun typeEventPublisher(kafkaTemplate: KafkaTemplate<String, String>, objectMapper: ObjectMapper): TypeEventPublisher =
        KafkaTypeEventPublisher(kafkaTemplate, objectMapper)

    @Bean
    fun typeService(typeRepository: R2dbcTypeRepositoryAdapter, eventPublisher: TypeEventPublisher) =
        TypeService(typeRepository, eventPublisher)

    @Bean
    fun schemaService(
        typeRepository: R2dbcTypeRepositoryAdapter,
        layoutRepository: R2dbcLayoutRepositoryAdapter,
        eventPublisher: TypeEventPublisher,
        tx: TransactionalOperator,
    ) = dev.sayaya.handbook.usecase.SchemaService(typeRepository, layoutRepository, eventPublisher, tx)

    @Bean
    fun layoutService(layoutRepository: R2dbcLayoutRepositoryAdapter) =
        LayoutService(layoutRepository)
}
