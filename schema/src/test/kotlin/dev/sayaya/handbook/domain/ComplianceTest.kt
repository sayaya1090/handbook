package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import java.util.*

class ComplianceTest : DescribeSpec({

    val now = Instant.now()
    val docId = UUID.randomUUID()

    describe("Compliance는") {

        describe("호환(compatible=true)인 경우") {
            it("violations가 비어있으면 성공적으로 생성된다") {
                shouldNotThrow<Exception> {
                    Compliance(docId, "customer", "1.0", true, emptyMap(), now)
                }
            }
            it("violations가 있으면 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> {
                    Compliance(docId, "customer", "1.0", true, mapOf("name" to "required"), now)
                } shouldHaveMessage "Compatible compliance must have no violations."
            }
        }

        describe("비호환(compatible=false)인 경우") {
            it("violations가 있으면 성공적으로 생성된다") {
                val compliance = Compliance(
                    docId, "customer", "2.0", false,
                    mapOf("email" to "Regex pattern mismatch", "age" to "Value out of range"),
                    now
                )
                compliance.compatible shouldBe false
                compliance.violations.size shouldBe 2
            }
            it("violations가 비어있으면 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> {
                    Compliance(docId, "customer", "2.0", false, emptyMap(), now)
                } shouldHaveMessage "Incompatible compliance must have at least one violation."
            }
        }

        describe("속성을 올바르게 저장한다") {
            it("모든 필드가 정확히 기록된다") {
                val violations = mapOf("phone" to "Invalid format")
                val compliance = Compliance(docId, "order", "3.1", false, violations, now)

                compliance.documentId shouldBe docId
                compliance.typeId shouldBe "order"
                compliance.typeVersion shouldBe "3.1"
                compliance.compatible shouldBe false
                compliance.violations shouldBe violations
                compliance.verifiedAt shouldBe now
            }
        }
    }
})
