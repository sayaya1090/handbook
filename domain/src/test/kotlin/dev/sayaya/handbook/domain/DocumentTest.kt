package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import java.util.*

class DocumentTest : DescribeSpec({

    val now: Instant = Instant.now()
    val validData = mapOf("key" to "value")

    describe("Document 클래스는") {

        describe("생성될 때") {
            context("올바른 값이 주어지면") {
                it("ID가 없는(영속화 전) 객체를 성공적으로 생성한다") {
                    shouldNotThrow<Exception> {
                        Document(null, "type", "serial-1", now, now.plusSeconds(1), null, null, validData)
                    }
                }
                it("ID가 있는(영속화 후) 객체를 성공적으로 생성한다") {
                    shouldNotThrow<Exception> {
                        Document(UUID.randomUUID(), "type", "serial-1", now, now.plusSeconds(1), now, "creator", validData)
                    }
                }
            }

            context("유효성 검증이") {
                it("잘못된 serial 형식을 거부한다") {
                    shouldThrow<IllegalArgumentException> {
                        Document(null, "type", "serial 123!", now, now.plusSeconds(1), null, null, validData)
                    } shouldHaveMessage "Document serial must be alphanumeric and may include hyphens and underscores."
                }
                it("만료일이 발효일보다 빠르면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Document(null, "type", "serial-1", now, now.minusSeconds(1), null, null, validData)
                    } shouldHaveMessage "Expire date time must be after effect date time"
                }
                it("ID가 있는데 생성일이 없으면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Document(UUID.randomUUID(), "type", "serial-1", now, now.plusSeconds(1), null, "creator", validData)
                    } shouldHaveMessage "If id is not null, createDateTime and creator must be not null"
                }
                it("ID가 있는데 생성자가 없으면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Document(UUID.randomUUID(), "type", "serial-1", now, now.plusSeconds(1), now, null, validData)
                    } shouldHaveMessage "If id is not null, createDateTime and creator must be not null"
                }
            }
        }

        describe("동등성을 비교할 때") {
            val id = UUID.randomUUID()

            it("ID가 있는 두 객체는 id가 같으면 같은 객체로 판단한다") {
                val doc1 = Document(id, "t1", "s1", now, now.plusSeconds(1), now, "c1", validData)
                val doc2 = Document(id, "t2", "s2", now, now.plusSeconds(2), now, "c2", validData)
                doc1 shouldBe doc2
                doc1.hashCode() shouldBe doc2.hashCode()
            }
            it("ID가 있는 두 객체는 id가 다르면 다른 객체로 판단한다") {
                val doc1 = Document(UUID.randomUUID(), "t", "s", now, now.plusSeconds(1), now, "c", validData)
                val doc2 = Document(UUID.randomUUID(), "t", "s", now, now.plusSeconds(1), now, "c", validData)
                doc1 shouldNotBe doc2
            }
            it("ID가 없는 두 객체는 다른 속성이 같아도 다른 객체로 판단한다") {
                val doc1 = Document(null, "t", "s", now, now.plusSeconds(1), null, null, validData)
                val doc2 = Document(null, "t", "s", now, now.plusSeconds(1), null, null, validData)
                doc1 shouldNotBe doc2
            }
            it("ID가 있는 객체와 없는 객체는 항상 다르다") {
                val doc1 = Document(id, "t", "s", now, now.plusSeconds(1), now, "c", validData)
                val doc2 = Document(null, "t", "s", now, now.plusSeconds(1), null, null, validData)
                doc1 shouldNotBe doc2
            }
            it("ID가 없는 객체의 hashCode는 예외 없이 값을 반환한다") {
                val doc = Document(null, "t", "s", now, now.plusSeconds(1), null, null, validData)
                shouldNotThrow<Exception> {
                    doc.hashCode()
                }
            }
            it("엣지 케이스(자기 자신, null, 다른 타입)를 올바르게 처리한다") {
                val doc = Document(id, "t", "s", now, now.plusSeconds(1), now, "c", validData)
                (doc == doc) shouldBe true
                (doc.equals(null)) shouldBe false
                (doc.equals("some string")) shouldBe false
            }
        }
    }
})