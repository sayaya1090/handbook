package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
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
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

@Configuration
class DocumentConfig : WebFluxConfigurer {
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

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val mapper = objectMapper() as JsonMapper
        configurer.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(mapper))
        configurer.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(mapper))
    }

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
