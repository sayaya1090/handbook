package dev.sayaya.handbook.`interface`.database

import org.springframework.data.relational.core.mapping.Column

interface EntityPageable {
    @get:Column("count") val count: Long
}