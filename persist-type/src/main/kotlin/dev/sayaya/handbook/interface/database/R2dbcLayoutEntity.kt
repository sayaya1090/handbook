package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.time.Instant
import java.util.*

@Table("layout")
data class R2dbcLayoutEntity (
    val workspace: UUID,
    val id: UUID,
    @Column("effective_at") var effectDateTime: Instant,
    @Column("expire_at") var expireDateTime: Instant
) {
    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createBy: UUID
    @Transient @Id val pk: R2dbcLayoutId = R2dbcLayoutId(workspace, id)

    companion object {
        data class R2dbcLayoutId (val workspace: UUID, val layout: UUID) : Serializable
        fun of(workspace: UUID, id: UUID = Ulid.fast().toUuid(), effectDateTime: Instant, expireDateTime: Instant) = R2dbcLayoutEntity(
            workspace = workspace,
            id = id,
            effectDateTime = effectDateTime,
            expireDateTime = expireDateTime
        )
    }
}