package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Webhook
import dev.sayaya.handbook.interfaces.database.R2dbcWebhookEntity
import dev.sayaya.handbook.interfaces.database.R2dbcWebhookRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class WebhookServiceTest : BehaviorSpec({
    val webhookRepo = mockk<R2dbcWebhookRepository>()
    val service = WebhookService(webhookRepo)
    val workspace = UUID.randomUUID()

    Given("웹훅 등록") {
        val webhook = Webhook(
            id = UUID.randomUUID(),
            workspace = workspace,
            url = "https://example.com/hook",
            events = listOf("DOCUMENT_CREATED"),
        )
        val entity = R2dbcWebhookEntity.fromDomain(webhook).copy(createdAt = Instant.now())

        When("유효한 웹훅을 등록하면") {
            every { webhookRepo.save(any()) } returns Mono.just(entity)

            Then("저장된 웹훅이 반환된다") {
                StepVerifier.create(service.register(webhook))
                    .assertNext { saved ->
                        saved.id shouldBe webhook.id
                        saved.url shouldBe "https://example.com/hook"
                        saved.events shouldBe listOf("DOCUMENT_CREATED")
                    }
                    .verifyComplete()
            }
        }

        When("중복 URL의 웹훅을 등록하면") {
            val duplicate = webhook.copy(id = UUID.randomUUID())
            val duplicateEntity = R2dbcWebhookEntity.fromDomain(duplicate).copy(createdAt = Instant.now())
            every { webhookRepo.save(any()) } returns Mono.just(duplicateEntity)

            Then("별도의 웹훅으로 저장된다") {
                StepVerifier.create(service.register(duplicate))
                    .assertNext { saved ->
                        saved.id shouldBe duplicate.id
                        saved.url shouldBe "https://example.com/hook"
                    }
                    .verifyComplete()
            }
        }
    }

    Given("웹훅 목록 조회") {
        When("웹훅이 없는 워크스페이스를 조회하면") {
            val emptyWorkspace = UUID.randomUUID()
            every { webhookRepo.findByWorkspace(emptyWorkspace) } returns Flux.empty()

            Then("빈 목록이 반환된다") {
                StepVerifier.create(service.list(emptyWorkspace))
                    .verifyComplete()
            }
        }

        When("여러 웹훅이 등록된 워크스페이스를 조회하면") {
            val entities = (1..3).map { i ->
                R2dbcWebhookEntity(
                    id = UUID.randomUUID(),
                    workspace = workspace,
                    url = "https://example.com/hook$i",
                    events = "DOCUMENT_CREATED",
                    active = true,
                    createdAt = Instant.now(),
                )
            }
            every { webhookRepo.findByWorkspace(workspace) } returns Flux.fromIterable(entities)

            Then("모든 웹훅이 반환된다") {
                StepVerifier.create(service.list(workspace))
                    .expectNextCount(3)
                    .verifyComplete()
            }
        }
    }

    Given("웹훅 삭제") {
        When("존재하는 웹훅을 삭제하면") {
            val id = UUID.randomUUID()
            every { webhookRepo.deleteById(id) } returns Mono.empty()

            Then("성공적으로 완료된다") {
                StepVerifier.create(service.delete(id))
                    .verifyComplete()
                verify { webhookRepo.deleteById(id) }
            }
        }

        When("존재하지 않는 웹훅을 삭제하면") {
            val id = UUID.randomUUID()
            every { webhookRepo.deleteById(id) } returns Mono.empty()

            Then("멱등하게 완료된다") {
                StepVerifier.create(service.delete(id))
                    .verifyComplete()
            }
        }
    }

    Given("워크스페이스+이벤트 기반 웹훅 조회") {
        val hookAll = R2dbcWebhookEntity(
            id = UUID.randomUUID(), workspace = workspace,
            url = "https://example.com/all", events = "", active = true,
        )
        val hookDoc = R2dbcWebhookEntity(
            id = UUID.randomUUID(), workspace = workspace,
            url = "https://example.com/doc", events = "DOCUMENT_CREATED,DOCUMENT_DELETED", active = true,
        )
        val hookType = R2dbcWebhookEntity(
            id = UUID.randomUUID(), workspace = workspace,
            url = "https://example.com/type", events = "TYPE_CREATED", active = true,
        )

        every { webhookRepo.findByWorkspaceAndActiveTrue(workspace) } returns Flux.just(hookAll, hookDoc, hookType)

        When("DOCUMENT_CREATED 이벤트로 조회하면") {
            Then("빈 events(=전체 구독)와 매칭 이벤트를 가진 웹훅이 반환된다") {
                StepVerifier.create(service.findByWorkspaceAndEvent(workspace, "DOCUMENT_CREATED"))
                    .assertNext { it.url shouldBe "https://example.com/all" }
                    .assertNext { it.url shouldBe "https://example.com/doc" }
                    .verifyComplete()
            }
        }

        When("매칭되지 않는 이벤트로 조회하면") {
            Then("빈 events 웹훅만 반환된다") {
                StepVerifier.create(service.findByWorkspaceAndEvent(workspace, "WORKSPACE_DELETED"))
                    .assertNext { it.url shouldBe "https://example.com/all" }
                    .verifyComplete()
            }
        }

        When("빈 워크스페이스에서 조회하면") {
            val emptyWs = UUID.randomUUID()
            every { webhookRepo.findByWorkspaceAndActiveTrue(emptyWs) } returns Flux.empty()

            Then("빈 결과가 반환된다") {
                StepVerifier.create(service.findByWorkspaceAndEvent(emptyWs, "DOCUMENT_CREATED"))
                    .verifyComplete()
            }
        }
    }
})
