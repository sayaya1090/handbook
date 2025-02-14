package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class AttributeTest : StringSpec({
    "ValueAttribute는 기본값 및 올바른 속성 타입을 반환한다" {
        val valueAttr = Attribute.Companion.ValueAttribute()
        valueAttr.name shouldBe Attribute.DEFAULT_NAME
        valueAttr.description shouldBe Attribute.DEFAULT_DESCRIPTION
        valueAttr.type shouldBe AttributeType.Value
    }

    "ArrayAttribute는 기본값 및 올바른 타입 속성을 설정한다" {
        val arrayAttr = Attribute.Companion.ArrayAttribute(valueType = AttributeType.Document)
        arrayAttr.name shouldBe Attribute.DEFAULT_NAME
        arrayAttr.description shouldBe Attribute.DEFAULT_DESCRIPTION
        arrayAttr.type shouldBe AttributeType.Array
        arrayAttr.valueType shouldBe AttributeType.Document
    }

    "MapAttribute는 적절한 keyType 및 valueType으로 초기화된다" {
        val mapAttr = Attribute.Companion.MapAttribute(
            keyType = AttributeType.Value,
            valueType = AttributeType.File
        )
        mapAttr.type shouldBe AttributeType.Map
        mapAttr.keyType shouldBe AttributeType.Value
        mapAttr.valueType shouldBe AttributeType.File
    }
    "DocumentAttribute는 ReferenceType과 올바른 속성 타입을 반환한다" {
        val referenceType = Type(
            id = "doc-type",
            parent = null,
            description = "A document type",
            attributes = emptyList()
        )
        val docAttr = Attribute.Companion.DocumentAttribute(
            name = "DocumentAttribute1",
            description = "Reference document",
            referenceType = referenceType
        )
        docAttr.type shouldBe AttributeType.Document
        docAttr.referenceType shouldBe referenceType
    }

    "FileAttribute는 확장자 유효성을 올바르게 검증한다" {
        // 정상적인 확장자
        val fileAttr = Attribute.Companion.FileAttribute(
            name = "MyFileAttr",
            extensions = setOf("txt", "pdf", "docx")
        )
        fileAttr.type shouldBe AttributeType.File
        fileAttr.extensions shouldContain "txt"

        // 잘못된 확장자
        val exception = shouldThrow<IllegalArgumentException> {
            Attribute.Companion.FileAttribute(
                name = "InvalidFileAttr",
                extensions = setOf("!invalid", "123$")
            )
        }
        exception shouldHaveMessage "FileAttribute extensions must contain only alphanumeric characters."
    }
})