package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant
import java.util.*

class AuditLogTest : DescribeSpec({

    val now = Instant.now()
    val ws = UUID.randomUUID()
    val user = UUID.randomUUID()

    describe("AuditLog는") {

        describe("생성 시") {
            it("유효한 값으로 생성된다") {
                shouldNotThrow<Exception> {
                    AuditLog(UUID.randomUUID(), ws, user, "CREATE", "Type", "customer", null, now)
                }
            }
            it("detail이 있어도 생성된다") {
                val log = AuditLog(UUID.randomUUID(), ws, user, "UPDATE", "Document", "C-001",
                    mapOf("field" to "email", "oldValue" to "a@b.com", "newValue" to "x@y.com"), now)
                log.detail!!.size shouldBe 3
            }
            it("빈 action은 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    AuditLog(UUID.randomUUID(), ws, user, "", "Type", "customer", null, now)
                }
            }
            it("빈 resourceType은 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    AuditLog(UUID.randomUUID(), ws, user, "CREATE", "", "customer", null, now)
                }
            }
            it("빈 resourceId는 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    AuditLog(UUID.randomUUID(), ws, user, "CREATE", "Type", "", null, now)
                }
            }
        }

        describe("동등성은 ID 기반이다") {
            val id = UUID.randomUUID()
            val log1 = AuditLog(id, ws, user, "CREATE", "Type", "customer", null, now)
            val log2 = AuditLog(id, ws, user, "DELETE", "Document", "D-001", null, now)

            it("같은 ID면 동일하다") {
                log1 shouldBe log2
            }
            it("다른 ID면 다르다") {
                val log3 = AuditLog(UUID.randomUUID(), ws, user, "CREATE", "Type", "customer", null, now)
                log1 shouldNotBe log3
            }
            it("hashCode도 ID 기반이다") {
                log1.hashCode() shouldBe log2.hashCode()
            }
        }
    }
})
