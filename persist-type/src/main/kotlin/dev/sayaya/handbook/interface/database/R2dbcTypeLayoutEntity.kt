package dev.sayaya.handbook.`interface`.database

import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("layout_type")
data class R2dbcTypeLayoutEntity (
    val workspace: UUID,
    val layout: UUID,
    val type: String,
    val version: String,
    val x: UShort,
    val y: UShort,
    val width: UShort,
    val height: UShort
)