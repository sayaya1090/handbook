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

/**
 * document-command 모듈의 Spring Bean 설정.
 *
 * **책임:** usecase 계층의 서비스/포트 구현체를 Spring Bean으로 등록하고,
 * Jackson ObjectMapper를 snake_case + JavaTime 지원으로 구성한다.
 *
 * **의존관계:**
 * - [R2dbcDocumentRepositoryAdapter] — 문서 영속화 어댑터
 * - [KafkaDocumentEventPublisher] — Kafka 이벤트 발행 어댑터
 * - [DocumentService] — 문서 비즈니스 로직
 */
@Configuration
class DocumentConfig {
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
