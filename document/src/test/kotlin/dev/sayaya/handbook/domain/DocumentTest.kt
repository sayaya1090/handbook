package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import java.util.*
import jsinterop.base.JsPropertyMap

class DocumentTest : DescribeSpec({

    val now: Double = Instant.now().toEpochMilli().toDouble()

    describe("Document 클래스는") {

        describe("생성될 때") {
            context("올바른 값이 주어지면") {
                it("ID가 없는(영속화 전) 객체를 성공적으로 생성한다") {
                    shouldNotThrow<Exception> {
                        Document.create(null, "type", "serial-1", now, now + 1000.0, 0.0, null, null)
                    }
                }
                it("ID가 있는(영속화 후) 객체를 성공적으로 생성한다") {
                    shouldNotThrow<Exception> {
                        Document.create(UUID.randomUUID().toString(), "type", "serial-1", now, now + 1000.0, now, "creator", null)
                    }
                }
            }

            context("유효성 검증이") {
                it("잘못된 serial 형식을 거부한다") {
                    shouldThrow<IllegalArgumentException> {
                        Document.create(null, "type", "serial 123!", now, now + 1000.0, 0.0, null, null)
                    } shouldHaveMessage "Document serial must be alphanumeric and may include hyphens and underscores."
                }
                it("만료일이 발효일보다 빠르면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Document.create(null, "type", "serial-1", now, now - 1000.0, 0.0, null, null)
                    } shouldHaveMessage "Expire date time must be after effect date time"
                }
                it("ID가 있는데 생성일이 없으면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Document.create(UUID.randomUUID().toString(), "type", "serial-1", now, now + 1000.0, 0.0, "creator", null)
                    } shouldHaveMessage "If id is not null, createDateTime and creator must be not null"
                }
                it("ID가 있는데 생성자가 없으면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Document.create(UUID.randomUUID().toString(), "type", "serial-1", now, now + 1000.0, now, null, null)
                    } shouldHaveMessage "If id is not null, createDateTime and creator must be not null"
                }
            }
        }

        describe("동등성을 비교할 때") {
            val id = UUID.randomUUID().toString()

            it("ID가 있는 두 객체는 id가 같으면 같은 객체로 판단한다") {
                val doc1 = Document.create(id, "t1", "s1", now, now + 1000.0, now, "c1", null)
                val doc2 = Document.create(id, "t2", "s2", now, now + 2000.0, now, "c2", null)
                doc1 shouldBe doc2
                doc1.hashCode() shouldBe doc2.hashCode()
            }
            it("ID가 있는 두 객체는 id가 다르면 다른 객체로 판단한다") {
                val doc1 = Document.create(UUID.randomUUID().toString(), "t", "s", now, now + 1000.0, now, "c", null)
                val doc2 = Document.create(UUID.randomUUID().toString(), "t", "s", now, now + 1000.0, now, "c", null)
                doc1 shouldNotBe doc2
            }
            it("ID가 없는 두 객체는 다른 속성이 같아도 다른 객체로 판단한다") {
                val doc1 = Document.create(null, "t", "s", now, now + 1000.0, 0.0, null, null)
                val doc2 = Document.create(null, "t", "s", now, now + 1000.0, 0.0, null, null)
                doc1 shouldNotBe doc2
            }
            it("ID가 있는 객체와 없는 객체는 항상 다르다") {
                val doc1 = Document.create(id, "t", "s", now, now + 1000.0, now, "c", null)
                val doc2 = Document.create(null, "t", "s", now, now + 1000.0, 0.0, null, null)
                doc1 shouldNotBe doc2
            }
            it("ID가 없는 객체의 hashCode는 예외 없이 값을 반환한다") {
                val doc = Document.create(null, "t", "s", now, now + 1000.0, 0.0, null, null)
                shouldNotThrow<Exception> {
                    doc.hashCode()
                }
            }
            it("엣지 케이스(자기 자신, null, 다른 타입)를 올바르게 처리한다") {
                val doc = Document.create(id, "t", "s", now, now + 1000.0, now, "c", null)
                (doc == doc) shouldBe true
                (doc.equals(null)) shouldBe false
                (doc.equals("some string")) shouldBe false
            }
        }
    }
})
