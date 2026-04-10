package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class DocumentServiceTest : BehaviorSpec({
    val repo = mockk<DocumentRepository>()
    val service = DocumentService(repo)
    val workspace = UUID.randomUUID()

    Given("문서 검색 요청이 주어졌을 때") {
        val param = Search(page = 0, limit = 10, sortBy = null, asc = null)
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
        every { repo.search(workspace, param) } returns Mono.just(PageImpl(listOf(doc)))

        When("search를 호출하면") {
            val result = service.search(workspace, param)

            Then("페이지 결과가 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.totalElements shouldBe 1
                        it.content[0].serial shouldBe "CUST-001"
                    }
                    .verifyComplete()
            }
        }
    }

    Given("문서 단건 조회 요청이 주어졌을 때") {
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
        val date = Instant.parse("2026-06-15T00:00:00Z")
        every { repo.find(workspace, "customer", "CUST-001", date) } returns Mono.just(doc)

        When("find를 호출하면") {
            val result = service.find(workspace, "customer", "CUST-001", date)

            Then("문서가 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.serial shouldBe "CUST-001" }
                    .verifyComplete()
            }
        }
    }
})
