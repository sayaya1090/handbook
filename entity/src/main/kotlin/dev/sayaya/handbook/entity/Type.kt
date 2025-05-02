package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.io.Serializable
import java.time.Instant
import java.util.*

// 같은 Type, version에 대해서는 last=true인 데이터가 유일해야 한다
// 이 테이블은 파티션이 적용되어 있어 createTable.sql로 초기화한다
@Table(name = "type", indexes=[
    Index(columnList = "workspace, name, version, last"),
    Index(columnList = "workspace, name, last, effective_at, expire_at"),
    Index(columnList = "workspace, last, effective_at, expire_at, created_at DESC"),
]) @Entity
@IdClass(Type.Companion.TypeId::class)
internal class Type {
    @Id @Column(name = "workspace") lateinit var workspace: UUID
    @Id @Column(name = "id") lateinit var id: UUID
    @Column(length = 64) lateinit var name: String
    lateinit var version: String
    @Column(length = 64) var parent: String? = null
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(name = "effective_at", nullable = false) lateinit var effectDateTime: Instant
    @Column(name = "expire_at", nullable = false) lateinit var expireDateTime: Instant
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
    @Column(nullable = false) var primitive: Boolean = false
    @Column(nullable = false, columnDefinition = "smallint") val x: Short = 0
    @Column(nullable = false, columnDefinition = "smallint") val y: Short = 0
    @Column(nullable = false, columnDefinition = "smallint") val width: Short = 0
    @Column(nullable = false, columnDefinition = "smallint") val height: Short = 0
    @Column(name="\"last\"", nullable = false, columnDefinition = "boolean DEFAULT true") var last: Boolean = true
    companion object {
        data class TypeId (
            val workspace: UUID = UUID.randomUUID(),
            val id: UUID = UUID.randomUUID(),
        ) : Serializable
        fun of(workspace: UUID, id: UUID=UUID.randomUUID(), user: User, type: String, version: String, parent: String?, effectDateTime: Instant, expireDateTime: Instant) = Type().apply {
            this.workspace = workspace
            this.id = id
            this.name = type
            this.version = version
            this.parent = parent
            createDateTime = Instant.now()
            createBy = user
            this.effectDateTime = effectDateTime
            this.expireDateTime = expireDateTime
        }
    }
}