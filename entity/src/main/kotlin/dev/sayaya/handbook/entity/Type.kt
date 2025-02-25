package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.time.Instant
import java.util.*

// 같은 Type, version에 대해서는 last=true인 데이터가 유일해야 한다
@Table(name = "type", indexes=[
    Index(columnList = "name, version, last"),
    Index(columnList = "name, last, effective_at, expire_at")
]) @Entity
internal class Type {
    @Id lateinit var id: UUID
    @Column(length = 64) lateinit var name: String
    lateinit var version: String
    @Column(length = 64) var parent: String? = null
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(name = "effective_at", nullable = false) lateinit var effectDateTime: Instant
    @Column(name = "expire_at", nullable = false) lateinit var expireDateTime: Instant
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
    @Column(nullable = false) var primitive: Boolean = false
    @Column(name="\"last\"", nullable = false, columnDefinition = "boolean DEFAULT true") var last: Boolean = true
    companion object {
        fun of(id: UUID=UUID.randomUUID(), user: User, type: String, version: String, parent: String?, effectDateTime: Instant, expireDateTime: Instant) = Type().apply {
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