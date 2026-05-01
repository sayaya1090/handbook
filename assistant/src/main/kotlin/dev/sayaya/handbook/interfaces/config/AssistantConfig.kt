package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.interfaces.database.InMemoryAuditRepository
import dev.sayaya.handbook.interfaces.event.KafkaAgentCommandEventPublisher
import dev.sayaya.handbook.interfaces.llm.*
import dev.sayaya.handbook.interfaces.quality.DefaultQualityMonitor
import dev.sayaya.handbook.usecase.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

@Configuration
@EnableConfigurationProperties(LlmConfig::class)
class AssistantConfig {
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
    fun openAiWebClient(llmConfig: LlmConfig): WebClient = WebClient.builder()
        .baseUrl(llmConfig.baseUrl)
        .build()

    @Bean
    fun intentParser(openAiWebClient: WebClient, objectMapper: ObjectMapper, llmConfig: LlmConfig): IntentParser =
        OpenAiIntentParser(openAiWebClient, objectMapper, llmConfig.apiKey, llmConfig.model)

    @Bean
    fun planExecutor(): PlanExecutor = GroupedPlanExecutor()

    @Bean
    fun agentCommandEventPublisher(kafkaTemplate: KafkaTemplate<String, String>, objectMapper: ObjectMapper): AgentCommandEventPublisher =
        KafkaAgentCommandEventPublisher(kafkaTemplate, objectMapper)

    @Bean
    fun auditRepository(): AuditRepository = InMemoryAuditRepository()

    @Bean
    fun executionContextManager() = ExecutionContextManager()

    @Bean
    fun subAgentPlanExecutor(intentParser: IntentParser, planExecutor: PlanExecutor, eventPublisher: AgentCommandEventPublisher): SubAgentPlanExecutor =
        DefaultSubAgentPlanExecutor(intentParser, planExecutor, eventPublisher)

    @Bean
    fun artifactAggregator(): ArtifactAggregator = DefaultArtifactAggregator()

    @Bean
    fun subAgentOrchestrator(subAgentPlanExecutor: SubAgentPlanExecutor, artifactAggregator: ArtifactAggregator, eventPublisher: AgentCommandEventPublisher) =
        SubAgentOrchestrator(subAgentPlanExecutor, artifactAggregator, eventPublisher)

    @Bean
    fun assistantService(intentParser: IntentParser, planExecutor: PlanExecutor, eventPublisher: AgentCommandEventPublisher, auditRepository: AuditRepository, executionContextManager: ExecutionContextManager, subAgentOrchestrator: SubAgentOrchestrator) =
        AssistantService(intentParser, planExecutor, eventPublisher, auditRepository, executionContextManager, subAgentOrchestrator)

    @Bean
    fun searchDocumentClient(@Value("\${handbook.document-query.base-url:http://localhost:8080}") baseUrl: String): WebClient =
        WebClient.builder().baseUrl(baseUrl).build()

    @Bean
    fun qualityMonitor(searchDocumentClient: WebClient): QualityMonitor = DefaultQualityMonitor(searchDocumentClient)

    @Bean
    fun qualityMonitorService(qualityMonitor: QualityMonitor, eventPublisher: AgentCommandEventPublisher) =
        QualityMonitorService(qualityMonitor, eventPublisher)
}
