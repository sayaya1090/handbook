package dev.sayaya.handbook.`interface`.database

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("type")
data class R2dbcTypeEntity (
    private val id: String,
    var primitive: Boolean
): Persistable<String> {
    var description: String? = null
    var parent: String? = null
    @CreatedDate lateinit var createdAt: LocalDateTime
    @CreatedBy lateinit var createdBy: String
    @LastModifiedDate lateinit var lastModifiedAt: LocalDateTime
    @LastModifiedBy lateinit var lastModifiedBy: String
    override fun getId(): String = id
    override fun isNew(): Boolean = this::createdAt.isInitialized.not()
}