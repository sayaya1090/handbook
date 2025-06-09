package dev.sayaya.handbook.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.io.Serializable
import java.time.Instant
import java.util.UUID

@Table(name = "document", indexes=[
    Index(columnList = "workspace, type, serial, created_at"),
    Index(columnList = "workspace, type, serial, last, effective_at, expire_at"),
    Index(columnList = "workspace, type, last, effective_at, expire_at, created_at DESC"),
    Index(columnList = "created_by"),
]) @Entity
@IdClass(Document.Companion.DocumentId::class)
internal class Document {
    @Id @ManyToOne @JoinColumn(name = "workspace", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var workspace: Workspace
    @Id @Column(name = "id") lateinit var id: UUID
    @Column(length = 64) lateinit var type: String
    @Column(length = 128) lateinit var serial: String
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(name = "effective_at", nullable = false) lateinit var effectDateTime: Instant
    @Column(name = "expire_at", nullable = false) lateinit var expireDateTime: Instant
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb", nullable = false)
    lateinit var data: Serializable

    @Column(name="\"last\"", nullable = false, columnDefinition = "boolean DEFAULT true") var last: Boolean = true
    companion object {
        data class DocumentId (
            val workspace: UUID = UUID.randomUUID(),
            val id: UUID = UUID.randomUUID(),
        ) : Serializable
        fun of(workspace: Workspace, id: UUID=UUID.randomUUID(), user: User, type: String, serial: String) = Document().apply {
            this.workspace = workspace
            this.id = id
            this.type = type
            this.serial = serial
            createDateTime = Instant.now()
            createBy = user
        }
    }
}