package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class DocumentServiceTest : BehaviorSpec({
    val repo = mockk<DocumentRepository>()
    val publisher = mockk<DocumentEventPublisher>(relaxed = true)
    val service = DocumentService(repo, publisher)
    val workspace = UUID.randomUUID()

    Given("문서 저장 요청이 주어졌을 때") {
        val doc = Document(
            id = null,
            type = "customer",
            serial = "CUST-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = null,
            creator = null,
            data = mapOf("name" to "홍길동", "email" to "test@example.com"),
        )
        val saved = doc.copy(
            id = UUID.randomUUID(),
            createDateTime = Instant.now(),
            creator = "user-1",
        )
        every { repo.saveAll(workspace, listOf(doc)) } returns Flux.just(saved)

        When("save를 호출하면") {
            val result = service.save(workspace, listOf(doc))

            Then("저장된 문서가 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id shouldBe saved.id }
                    .verifyComplete()
            }
            Then("DOCUMENT_CREATED 이벤트가 발행된다") {
                verify { publisher.publishCreated(workspace, saved) }
            }
        }
    }

    Given("문서 패치 요청이 주어졌을 때") {
        val docId = UUID.randomUUID()
        val patch = DocumentPatch(
            id = docId,
            rev = 1,
            data = mapOf("phone" to "010-5678"),
        )
        val patched = Document(
            id = docId,
            type = "customer",
            serial = "CUST-003",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.now(),
            creator = "user-1",
            data = mapOf("name" to "홍길동", "phone" to "010-5678"),
        )
        every { repo.patchAll(workspace, listOf(patch)) } returns Flux.just(patched)

        When("patch를 호출하면") {
            val result = service.patch(workspace, listOf(patch))

            Then("패치된 문서가 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.data["phone"] shouldBe "010-5678" }
                    .verifyComplete()
            }
            Then("DOCUMENT_CREATED 이벤트가 발행된다") {
                verify { publisher.publishCreated(workspace, patched) }
            }
        }
    }

    Given("문서 삭제 요청이 주어졌을 때") {
        val doc = Document(
            id = UUID.randomUUID(),
            type = "customer",
            serial = "CUST-002",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.now(),
            creator = "user-1",
            data = emptyMap(),
        )
        every { repo.deleteAll(workspace, listOf(doc)) } returns Mono.empty()

        When("delete를 호출하면") {
            val result = service.delete(workspace, listOf(doc))

            Then("성공적으로 완료된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
            Then("DOCUMENT_DELETED 이벤트가 발행된다") {
                verify { publisher.publishDeleted(workspace, doc) }
            }
        }
    }
})
