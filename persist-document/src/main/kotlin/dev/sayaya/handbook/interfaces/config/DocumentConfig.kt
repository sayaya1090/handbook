package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.interfaces.database.R2dbcDocumentEntityRepository
import dev.sayaya.handbook.interfaces.database.R2dbcDocumentRepositoryAdapter
import dev.sayaya.handbook.interfaces.event.KafkaDocumentEventPublisher
import dev.sayaya.handbook.usecase.DocumentEventPublisher
import dev.sayaya.handbook.usecase.DocumentService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
class DocumentConfig {
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
    fun documentRepositoryAdapter(
        repo: R2dbcDocumentEntityRepository,
        objectMapper: ObjectMapper,
        tx: TransactionalOperator,
        databaseClient: DatabaseClient,
    ) = R2dbcDocumentRepositoryAdapter(repo, objectMapper, tx, databaseClient)

    @Bean
    fun documentEventPublisher(kafkaTemplate: KafkaTemplate<String, String>, objectMapper: ObjectMapper): DocumentEventPublisher =
        KafkaDocumentEventPublisher(kafkaTemplate, objectMapper)

    @Bean
    fun documentService(documentRepository: R2dbcDocumentRepositoryAdapter, eventPublisher: DocumentEventPublisher) =
        DocumentService(documentRepository, eventPublisher)
}
