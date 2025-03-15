package dev.sayaya.handbook.client.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.util.*

class TypeTest : StringSpec({
    "유효한 Type 객체 생성" {
        val attributes = listOf(Attribute()) // 유효한 Attribute 리스트
        val type = Type(
            "test-id",              // id
            "1.0",                  // version
            Date(1672531200000L),   // effectDateTime
            Date(1672534800000L),   // expireDateTime
            "A valid Type object",  // description
            true,                   // primitive
            attributes,             // attributes
            "parent-id"             // parent
        )

        type.id() shouldBe "test-id"
        type.version() shouldBe "1.0"
        type.effectDateTime() shouldBe Date(1672531200000L)
        type.expireDateTime() shouldBe Date(1672534800000L)
        type.description() shouldBe "A valid Type object"
        type.primitive() shouldBe true
        type.parent() shouldBe "parent-id"
        type.attributes().shouldNotBeEmpty()
    }

    "id가 null일 경우 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type(
                null,                  // id
                "1.0",                 // version
                Date(1672531200000L),   // effectDateTime
                Date(1672534800000L),   // expireDateTime
                "Type with null id",   // description
                false,                 // primitive
                listOf(Attribute()),   // attributes
                null                   // parent
            )
        }

        exception.shouldHaveMessage("id must not be null")
    }

    "version이 null일 경우 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type(
                "test-id",              // id
                null,                   // version
                Date(1672531200000L),   // effectDateTime
                Date(1672534800000L),   // expireDateTime
                "Type with null version", // description
                false,                  // primitive
                listOf(Attribute()),    // attributes
                null                    // parent
            )
        }

        exception.shouldHaveMessage("version must not be null")
    }

    "attributes가 null일 경우 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type(
                "test-id",              // id
                "1.0",                  // version
                Date(1672531200000L),   // effectDateTime
                Date(1672534800000L),   // expireDateTime
                "Type with null attributes", // description
                false,                  // primitive
                null,                   // attributes
                null                    // parent
            )
        }

        exception.shouldHaveMessage("attributes must not be null")
    }

    "종료 시간이 시작 시간보다 빠를 경우 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type(
                "test-id",              // id
                "1.0",                  // version
                Date(1672534800000L),   // effectDateTime
                Date(1672531200000L),   // expireDateTime (잘못된 시간)
                "Invalid date range",   // description
                false,                  // primitive
                listOf(Attribute()),    // attributes
                null                    // parent
            )
        }

        exception.shouldHaveMessage("Expire date time must be after effect date time")
    }

    "Type의 부모-자식 관계 설정" {
        val parentAttributes = listOf(Attribute())
        val childAttributes = listOf(Attribute())

        val parentType = Type(
            "parent-id",             // id
            "1.0",                   // version
            Date(1672531200000L),   // effectDateTime
            Date(1672534800000L),   // expireDateTime
            "Parent Type",           // description
            true,                    // primitive
            parentAttributes,        // attributes
            null                     // parent
        )

        val childType = Type(
            "child-id",              // id
            "1.0",                   // version
            Date(1672534800000L),   // effectDateTime
            Date(1672538400000L),   // expireDateTime
            "Child Type",            // description
            false,                   // primitive
            childAttributes,         // attributes
            parentType.id()          // 부모 id 사용
        )

        parentType.id() shouldBe "parent-id"
        childType.parent() shouldBe parentType.id()
    }
})