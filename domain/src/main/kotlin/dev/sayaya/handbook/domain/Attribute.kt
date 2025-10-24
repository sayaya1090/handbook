package dev.sayaya.handbook.domain

import java.io.Serializable

/**
 * 'Type'을 구성하는 개별 속성을 정의하는 값 객체(Value Object).
 *
 * 이 객체는 고유한 ID를 갖지 않으며, 모든 프로퍼티의 값들이 모여 하나의 '속성'을 정의합니다.
 *
 * @property name 속성의 이름 (e.g., "title", "author")
 * @property order GUI 등에서 표시될 순서
 * @property description 속성에 대한 설명
 * @property type 이 속성의 데이터 타입 (e.g., Text, Number, Document 등)
 * @property nullable 이 속성의 값이 null을 허용하는지 여부
 * @property inherited 이 속성이 부모 'Type'으로부터 상속되었는지 여부
 */
data class Attribute (
    val name: String,
    val order: Short,
    val description: String?,
    val type: AttributeType,
    val nullable: Boolean,
    val inherited: Boolean
) : Serializable {
    init {
        require(name.isNotBlank()) { "Attribute name cannot be blank." }
        require(name.matches(Regex("^[a-zA-Z0-9_-]+$"))) { "Attribute name can only contain alphanumerics, hyphens, and underscores." }
    }
}