package dev.sayaya.handbook.domain.event

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Type
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import java.util.*

class EventTest : DescribeSpec({

    val now = Instant.now()
    val workspace = UUID.randomUUID()

    fun sampleDocument() = Document(
        id = UUID.randomUUID(), type = "customer", serial = "C-001",
        effectDateTime = now, expireDateTime = now.plusSeconds(3600),
        createDateTime = now, creator = "user-1", data = mapOf("name" to "Alice")
    )

    fun sampleType() = Type(
        id = "customer", version = "1.0",
        effectDateTime = now, expireDateTime = now.plusSeconds(3600),
        description = "고객", primitive = false
    )

    describe("DocumentEvent는") {
        it("DOCUMENT_CREATED로 생성할 수 있다") {
            shouldNotThrow<Exception> {
                DocumentEvent(UUID.randomUUID(), workspace, Event.EventType.DOCUMENT_CREATED, sampleDocument())
            }
        }
        it("DOCUMENT_DELETED로 생성할 수 있다") {
            shouldNotThrow<Exception> {
                DocumentEvent(UUID.randomUUID(), workspace, Event.EventType.DOCUMENT_DELETED, sampleDocument())
            }
        }
        it("허용되지 않는 이벤트 타입으로 생성 시 예외를 발생시킨다") {
            shouldThrow<IllegalArgumentException> {
                DocumentEvent(UUID.randomUUID(), workspace, Event.EventType.TYPE_CREATED, sampleDocument())
            }.message shouldBe "Invalid event type for DocumentEvent: TYPE_CREATED. Allowed: [DOCUMENT_CREATED, DOCUMENT_DELETED]"
        }
    }

    describe("TypeEvent는") {
        it("TYPE_CREATED로 생성할 수 있다") {
            shouldNotThrow<Exception> {
                TypeEvent(UUID.randomUUID(), workspace, Event.EventType.TYPE_CREATED, sampleType())
            }
        }
        it("TYPE_DELETED로 생성할 수 있다") {
            shouldNotThrow<Exception> {
                TypeEvent(UUID.randomUUID(), workspace, Event.EventType.TYPE_DELETED, sampleType())
            }
        }
        it("허용되지 않는 이벤트 타입으로 생성 시 예외를 발생시킨다") {
            shouldThrow<IllegalArgumentException> {
                TypeEvent(UUID.randomUUID(), workspace, Event.EventType.DOCUMENT_CREATED, sampleType())
            }.message shouldBe "Invalid event type for TypeEvent: DOCUMENT_CREATED. Allowed: [TYPE_CREATED, TYPE_DELETED]"
        }
    }

    describe("ValidationEvent는") {
        it("VALIDATION_REQUESTED로 생성할 수 있다") {
            shouldNotThrow<Exception> {
                ValidationEvent(
                    UUID.randomUUID(), workspace, Event.EventType.VALIDATION_REQUESTED,
                    ValidationPayload(typeId = "customer", typeVersion = "1.0")
                )
            }
        }
        it("documentId 없이 타입 단위 검증을 요청할 수 있다") {
            val event = ValidationEvent(
                UUID.randomUUID(), workspace, Event.EventType.VALIDATION_REQUESTED,
                ValidationPayload(typeId = "customer")
            )
            event.payload.documentId shouldBe null
            event.payload.typeVersion shouldBe null
        }
        it("허용되지 않는 이벤트 타입으로 생성 시 예외를 발생시킨다") {
            shouldThrow<IllegalArgumentException> {
                ValidationEvent(
                    UUID.randomUUID(), workspace, Event.EventType.DOCUMENT_CREATED,
                    ValidationPayload(typeId = "customer")
                )
            } shouldHaveMessage "Invalid event type for ValidationEvent: DOCUMENT_CREATED. Must be VALIDATION_REQUESTED"
        }
    }
})
