package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import java.time.Instant

@JvmRecord
data class TypeParam (
    private val id: String,
    private val version: String,
    private val effectDateTime: Instant,
    private val expireDateTime: Instant,
    private val description: String?,
    private val primitive: Boolean,
    private val attributes: List<Attribute> = emptyList(),
    private val parent: String? = null,
    private val x: UShort,
    private val y: UShort,
    private val width: UShort,
    private val height: UShort,

    val type: Type = Type(id, version, effectDateTime, expireDateTime, description, primitive, attributes, parent, x, y, width, height),
    val delete: Boolean
)