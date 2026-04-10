package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.interfaces.database.*
import dev.sayaya.handbook.interfaces.event.KafkaTypeEventPublisher
import dev.sayaya.handbook.usecase.LayoutService
import dev.sayaya.handbook.usecase.TypeEventPublisher
import dev.sayaya.handbook.usecase.TypeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
class TypeConfig {
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
    fun typeRepositoryAdapter(
        typeRepo: R2dbcTypeEntityRepository,
        attrRepo: R2dbcAttributeEntityRepository,
        objectMapper: ObjectMapper,
        tx: TransactionalOperator,
    ) = R2dbcTypeRepositoryAdapter(typeRepo, attrRepo, objectMapper, tx)

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
    fun layoutService(layoutRepository: R2dbcLayoutRepositoryAdapter) =
        LayoutService(layoutRepository)
}
