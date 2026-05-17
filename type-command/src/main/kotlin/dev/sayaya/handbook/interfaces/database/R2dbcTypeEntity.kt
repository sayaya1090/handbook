package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.Instant
import java.util.*

/**
 * types 테이블에 매핑되는 R2DBC 엔티티.
 *
 * **책임:** 타입 도메인 객체와 DB 행 간의 양방향 변환을 담당한다.
 * 속성(attributes)은 별도 테이블([R2dbcAttributeEntity])에 저장되므로,
 * [toDomain] 호출 시 속성 리스트를 외부에서 주입받는다.
 *
 * **주의:** PK는 (id, version, workspace) 복합키이다. Spring Data R2DBC의 한계를 극복하기 위해
 * Persistable을 구현하여 isNew 상태를 직접 관리한다.
 */
@Table("types")
data class R2dbcTypeEntity(
    @Id private val id: String,
    val version: String,
    val workspace: UUID,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    val description: String?,
    val primitive: Boolean,
    val parent: String?,
    @Version val rev: Long? = null,
) : Persistable<String> {
    @Transient var isNewRecord: Boolean = false
    override fun getId(): String = id
    override fun isNew(): Boolean = isNewRecord || rev == null

    fun toDomain(attributes: List<Attribute> = emptyList()): Type {
        val type = Type.create(
            id,
            version,
            effectDateTime.toEpochMilli().toDouble(),
            expireDateTime.toEpochMilli().toDouble()
        ).description(description).primitive(primitive).attributes(attributes.toTypedArray())
        parent?.let { type.parent(it) }
        rev?.let { type.rev(it.toDouble()) }
        return type
    }

    companion object {
        fun fromDomain(workspace: UUID, type: Type): R2dbcTypeEntity {
            val isNew = type.rev() == -1.0
            return R2dbcTypeEntity(
                id = type.id(),
                version = type.version(),
                workspace = workspace,
                effectDateTime = Instant.ofEpochMilli(type.effectDateTime().toLong()),
                expireDateTime = Instant.ofEpochMilli(type.expireDateTime().toLong()),
                description = type.description(),
                primitive = type.primitive(),
                parent = type.parent(),
                rev = if (isNew) null else type.rev().toLong(),
            ).apply { this.isNewRecord = isNew }
        }
    }
}
