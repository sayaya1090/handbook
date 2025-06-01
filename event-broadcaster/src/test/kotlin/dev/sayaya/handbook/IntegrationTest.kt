package dev.sayaya.handbook

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.testcontainer.Database
import dev.sayaya.handbook.testcontainer.MessageQueue
import io.kotest.core.spec.style.BehaviorSpec
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import java.time.Duration
import java.util.*
import java.util.function.Supplier

@SpringBootTest(properties = [
    "management.endpoint.health.probes.enabled=true",
], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(IntegrationTest.Companion.TestConfig::class)
@Testcontainers
internal class IntegrationTest(
    private val _client: WebTestClient,
    private val databaseClient: DatabaseClient,
    private val queue: Sinks.Many<Event<*, *>>
): BehaviorSpec({
    beforeSpec {
        databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name, provider, account) VALUES ('93951bc3-be1e-4fc8-865f-d6376ac3e87b', NOW(), NOW(), null, 'system', 'test-provider', 'test-user');
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    Given("큐에 메시지가 발행 중") {
        val client = _client
        Flux.interval(Duration.ofMillis(10)).subscribe {
            queue.tryEmitNext(TestEvent(UUID.randomUUID(), "Test$it", Event.Type.entries.random()))
        }

        When("메시지를 요청하면") {
            val req = client.get().uri("/messages").accept(MediaType.TEXT_EVENT_STREAM)
            Then("OK 코드와 결과를 반환한다") {
                val exchange = req.exchange().expectStatus().isOk
                val flux = exchange.returnResult(String::class.java).responseBody.take(3)

                StepVerifier.create(flux)
                    .expectNextMatches { it.contains("Test") }
                    .expectNextMatches { it.contains("Test") }
                    .expectNextMatches { it.contains("Test") }
                    .thenCancel()
                    .verify(Duration.ofSeconds(5))
            }
        }
        When("여러 개의 요청을 동시에 수행하면") {
            val requestCount = 50
            val take = 5L
            val responses = Flux.range(0, requestCount)
                .flatMap {
                    client.get().uri("/messages").accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk
                        .returnResult(String::class.java)
                        .responseBody
                        .take(take)
                }

            Then("모든 요청이 누락 없이 응답을 받는다") {
                StepVerifier.create(responses)
                    .expectNextCount(requestCount * take)
                    .thenCancel()
                    .verify(Duration.ofSeconds(5))
            }
        }
    }
})  {
    companion object {
        @TestConfiguration
        class TestConfig {
            private val logger = LoggerFactory.getLogger(TestConfig::class.java)
            @Bean fun objectMapper(): ObjectMapper = ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .registerModule(JavaTimeModule())
                .registerModule(
                    KotlinModule.Builder()
                        .withReflectionCacheSize(512)
                        .configure(KotlinFeature.NullToEmptyCollection, false)
                        .configure(KotlinFeature.NullToEmptyMap, false)
                        .configure(KotlinFeature.NullIsSameAsDefault, false)
                        .configure(KotlinFeature.SingletonSupport, false)
                        .configure(KotlinFeature.StrictNullChecks, false)
                        .build())
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            @Bean fun buffer(): Sinks.Many<Event<*, *>> = Sinks.many().unicast().onBackpressureBuffer()
            @Bean("handbook-mock") fun handbookEventQueueMock(buffer: Sinks.Many<Event<*, *>>, om: ObjectMapper): Supplier<Flux<String>> = Supplier {
                buffer.asFlux().mapNotNull { msg -> om.writeValueAsString(msg) }.onErrorResume { e ->
                    logger.error("Error occurred while processing buffer", e)
                    Flux.empty()
                }
            }
        }
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            Database().registerDynamicProperties(registry)
            MessageQueue().registerDynamicProperties(registry)

            registry.add("spring.cloud.function.definition") { "handbook; handbook-mock" }
            registry.add("spring.cloud.stream.bindings.handbook-in-0.destination") { "handbook-event" }
            registry.add("spring.cloud.stream.bindings.handbook-mock-out-0.destination") { "handbook-event" }
        }
        private fun DatabaseClient.insert(sql: String) = this.sql(sql).fetch().rowsUpdated().block()
        private fun DatabaseClient.insertUserAsAdmin(user: String) {
            insert("INSERT INTO public.\"user\" (id, department, name, password, email, key, serial, state, config, role, pw_fail_cnt, department_code, department_detail) VALUES ('$user', 'department', 'name', null, null, null, null, 'ACTIVATE', null, 'A', 0, null, null)")
        }
        data class TestEvent (
            private val id: UUID,
            private val param: String,
            private val type: Event.Type
        ): Event<String, TestEvent> {
            override fun id(): UUID = id
            override fun type(): Event.Type = type
            override fun param(): String = param
        }
    }
}