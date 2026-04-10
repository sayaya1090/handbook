package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.interfaces.database.InMemoryAuditRepository
import dev.sayaya.handbook.interfaces.llm.LlmConfig
import dev.sayaya.handbook.interfaces.llm.OpenAiIntentParser
import dev.sayaya.handbook.interfaces.llm.SequentialPlanExecutor
import dev.sayaya.handbook.interfaces.event.KafkaAgentCommandEventPublisher
import dev.sayaya.handbook.interfaces.quality.DefaultQualityMonitor
import dev.sayaya.handbook.usecase.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(LlmConfig::class)
class AssistantConfig {
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
    fun openAiWebClient(llmConfig: LlmConfig): WebClient = WebClient.builder()
        .baseUrl(llmConfig.baseUrl)
        .build()

    @Bean
    fun intentParser(openAiWebClient: WebClient, objectMapper: ObjectMapper, llmConfig: LlmConfig): IntentParser =
        OpenAiIntentParser(openAiWebClient, objectMapper, llmConfig.apiKey, llmConfig.model)

    @Bean
    fun planExecutor(): PlanExecutor = SequentialPlanExecutor()

    @Bean
    fun agentCommandEventPublisher(kafkaTemplate: KafkaTemplate<String, String>, objectMapper: ObjectMapper): AgentCommandEventPublisher =
        KafkaAgentCommandEventPublisher(kafkaTemplate, objectMapper)

    @Bean
    fun auditRepository(): AuditRepository = InMemoryAuditRepository()

    @Bean
    fun assistantService(intentParser: IntentParser, planExecutor: PlanExecutor, eventPublisher: AgentCommandEventPublisher, auditRepository: AuditRepository) =
        AssistantService(intentParser, planExecutor, eventPublisher, auditRepository)

    @Bean
    fun searchDocumentClient(@Value("\${handbook.search-document.base-url:http://localhost:8080}") baseUrl: String): WebClient =
        WebClient.builder().baseUrl(baseUrl).build()

    @Bean
    fun qualityMonitor(searchDocumentClient: WebClient): QualityMonitor = DefaultQualityMonitor(searchDocumentClient)

    @Bean
    fun qualityMonitorService(qualityMonitor: QualityMonitor, eventPublisher: AgentCommandEventPublisher) =
        QualityMonitorService(qualityMonitor, eventPublisher)
}
