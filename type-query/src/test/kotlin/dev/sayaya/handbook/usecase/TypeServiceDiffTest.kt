package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.Type
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class TypeServiceDiffTest : BehaviorSpec({
    val repo = mockk<TypeSearchRepository>()
    val service = TypeSearchService(repo)
    val workspace = UUID.randomUUID()

    val baseAttributes = listOf(
        Attribute(
            name = "name",
            order = 0,
            description = "이름",
            type = AttributeType.Text(),
            nullable = false,
            inherited = false,
        ),
        Attribute(
            name = "age",
            order = 1,
            description = "나이",
            type = AttributeType.Number(min = 0, max = 200),
            nullable = true,
            inherited = false,
        ),
    )

    val v1 = Type(
        id = "customer",
        version = "1.0",
        effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
        expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
        description = "고객 타입",
        primitive = false,
        attributes = baseAttributes,
        parent = null,
    )

    Given("속성이 추가된 버전 간 diff") {
        val v2 = Type(
            id = "customer",
            version = "2.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2027-12-31T23:59:59Z"),
            description = "고객 타입",
            primitive = false,
            attributes = baseAttributes + Attribute(
                name = "email",
                order = 2,
                description = "이메일",
                type = AttributeType.Text(),
                nullable = true,
                inherited = false,
            ),
        )
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)
        every { repo.findByIdAndVersion(workspace, "customer", "2.0") } returns Mono.just(v2)

        When("diff를 호출하면") {
            Then("추가된 속성이 반환된다") {
                StepVerifier.create(service.diff(workspace, "customer", "1.0", "2.0"))
                    .assertNext { result ->
                        result.added shouldContain "email"
                        result.removed.size shouldBe 0
                    }
                    .verifyComplete()
            }
        }
    }

    Given("속성이 삭제된 버전 간 diff") {
        val v2 = Type(
            id = "customer",
            version = "2.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2027-12-31T23:59:59Z"),
            description = "고객 타입",
            primitive = false,
            attributes = listOf(baseAttributes[0]), // age 삭제
        )
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)
        every { repo.findByIdAndVersion(workspace, "customer", "2.0") } returns Mono.just(v2)

        When("diff를 호출하면") {
            Then("삭제된 속성이 반환된다") {
                StepVerifier.create(service.diff(workspace, "customer", "1.0", "2.0"))
                    .assertNext { result ->
                        result.removed shouldContain "age"
                        result.added.size shouldBe 0
                    }
                    .verifyComplete()
            }
        }
    }

    Given("description이 변경된 버전 간 diff") {
        val v2 = Type(
            id = "customer",
            version = "2.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2027-12-31T23:59:59Z"),
            description = "변경된 설명",
            primitive = false,
            attributes = baseAttributes,
        )
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)
        every { repo.findByIdAndVersion(workspace, "customer", "2.0") } returns Mono.just(v2)

        When("diff를 호출하면") {
            Then("description 변경이 포함된다") {
                StepVerifier.create(service.diff(workspace, "customer", "1.0", "2.0"))
                    .assertNext { result ->
                        result.changes.any { it.contains("description") } shouldBe true
                    }
                    .verifyComplete()
            }
        }
    }

    Given("parent가 변경된 버전 간 diff") {
        val v2 = Type(
            id = "customer",
            version = "2.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2027-12-31T23:59:59Z"),
            description = "고객 타입",
            primitive = false,
            attributes = baseAttributes,
            parent = "person",
        )
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)
        every { repo.findByIdAndVersion(workspace, "customer", "2.0") } returns Mono.just(v2)

        When("diff를 호출하면") {
            Then("parent 변경이 포함된다") {
                StepVerifier.create(service.diff(workspace, "customer", "1.0", "2.0"))
                    .assertNext { result ->
                        result.changes.any { it.contains("parent") } shouldBe true
                    }
                    .verifyComplete()
            }
        }
    }

    Given("속성의 nullable이 변경된 버전 간 diff") {
        val modifiedAttributes = listOf(
            baseAttributes[0],
            Attribute(
                name = "age",
                order = 1,
                description = "나이",
                type = AttributeType.Number(min = 0, max = 200),
                nullable = false, // true -> false
                inherited = false,
            ),
        )
        val v2 = Type(
            id = "customer",
            version = "2.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2027-12-31T23:59:59Z"),
            description = "고객 타입",
            primitive = false,
            attributes = modifiedAttributes,
        )
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)
        every { repo.findByIdAndVersion(workspace, "customer", "2.0") } returns Mono.just(v2)

        When("diff를 호출하면") {
            Then("nullable 변경이 포함된다") {
                StepVerifier.create(service.diff(workspace, "customer", "1.0", "2.0"))
                    .assertNext { result ->
                        result.changes.any { it.contains("age") && it.contains("nullable") } shouldBe true
                    }
                    .verifyComplete()
            }
        }
    }

    Given("속성의 order가 변경된 버전 간 diff") {
        val modifiedAttributes = listOf(
            Attribute(
                name = "name",
                order = 1, // 0 -> 1
                description = "이름",
                type = AttributeType.Text(),
                nullable = false,
                inherited = false,
            ),
            Attribute(
                name = "age",
                order = 0, // 1 -> 0
                description = "나이",
                type = AttributeType.Number(min = 0, max = 200),
                nullable = true,
                inherited = false,
            ),
        )
        val v2 = Type(
            id = "customer",
            version = "2.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2027-12-31T23:59:59Z"),
            description = "고객 타입",
            primitive = false,
            attributes = modifiedAttributes,
        )
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)
        every { repo.findByIdAndVersion(workspace, "customer", "2.0") } returns Mono.just(v2)

        When("diff를 호출하면") {
            Then("order 변경이 포함된다") {
                StepVerifier.create(service.diff(workspace, "customer", "1.0", "2.0"))
                    .assertNext { result ->
                        result.changes.any { it.contains("order") } shouldBe true
                    }
                    .verifyComplete()
            }
        }
    }

    Given("변경 없는 동일 버전 간 diff") {
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)
        every { repo.findByIdAndVersion(workspace, "customer", "1.0") } returns Mono.just(v1)

        When("diff를 호출하면") {
            Then("변경사항이 없다") {
                StepVerifier.create(service.diff(workspace, "customer", "1.0", "1.0"))
                    .assertNext { result ->
                        result.changes.size shouldBe 0
                        result.added.size shouldBe 0
                        result.removed.size shouldBe 0
                    }
                    .verifyComplete()
            }
        }
    }
})
