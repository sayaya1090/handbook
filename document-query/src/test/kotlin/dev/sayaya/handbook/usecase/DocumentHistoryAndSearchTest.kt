package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class DocumentHistoryAndSearchTest : BehaviorSpec({
    val repo = mockk<DocumentSearchRepository>()
    val service = DocumentSearchService(repo)
    val workspace = UUID.randomUUID()

    Given("문서 이력 조회 요청이 주어졌을 때") {
        val doc1 = Document(
            id = UUID.randomUUID(),
            type = "customer",
            serial = "CUST-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-06-30T23:59:59Z"),
            createDateTime = Instant.now(),
            creator = "user-1",
            data = mapOf("name" to "홍길동"),
        )
        val doc2 = Document(
            id = UUID.randomUUID(),
            type = "customer",
            serial = "CUST-001",
            effectDateTime = Instant.parse("2026-07-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.now(),
            creator = "user-1",
            data = mapOf("name" to "홍길순"),
        )
        every { repo.findHistory(workspace, "customer", "CUST-001") } returns Flux.just(doc2, doc1)

        When("findHistory를 호출하면") {
            val result = service.findHistory(workspace, "customer", "CUST-001")

            Then("시간 역순으로 문서 스냅샷이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.data["name"] shouldBe "홍길순" }
                    .assertNext { it.data["name"] shouldBe "홍길동" }
                    .verifyComplete()
            }
        }
    }

    Given("전문 검색 요청이 주어졌을 때") {
        val doc = Document(
            id = UUID.randomUUID(),
            type = "customer",
            serial = "CUST-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.now(),
            creator = "user-1",
            data = mapOf("name" to "홍길동"),
        )
        every { repo.fullTextSearch(workspace, "홍길동", 0, 50) } returns Mono.just(PageImpl(listOf(doc)))

        When("fullTextSearch를 호출하면") {
            val result = service.fullTextSearch(workspace, "홍길동", 0, 50)

            Then("매칭된 문서가 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.totalElements shouldBe 1
                        it.content[0].data["name"] shouldBe "홍길동"
                    }
                    .verifyComplete()
            }
        }
    }

    Given("빈 검색어로 전문 검색을 요청할 때") {
        When("fullTextSearch를 빈 문자열로 호출하면") {
            val result = service.fullTextSearch(workspace, "", 0, 50)

            Then("빈 페이지가 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.totalElements shouldBe 0 }
                    .verifyComplete()
            }
        }

        When("fullTextSearch를 공백 문자열로 호출하면") {
            val result = service.fullTextSearch(workspace, "   ", 0, 50)

            Then("빈 페이지가 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.totalElements shouldBe 0 }
                    .verifyComplete()
            }
        }
    }
})
