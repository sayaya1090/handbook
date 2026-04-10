package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.State
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

/**
 * users 테이블에 매핑되는 R2DBC 엔티티.
 *
 * **책임:** 사용자 도메인 객체와 DB 행 간의 변환을 담당한다.
 * [Persistable]을 구현하여 신규 삽입과 기존 업데이트를 구분한다.
 *
 * **주의:** [new] 플래그를 true로 설정해야 INSERT가 실행된다.
 * 기본값 false이면 Spring Data는 UPDATE를 시도하므로, 신규 사용자 생성 시 반드시 `apply { new = true }`를 호출해야 한다.
 */
@Table("users")
data class R2dbcUserEntity(
    @Id private val id: UUID,
    val provider: String,
    val account: String,
    var name: String,
    var state: State = State.ACTIVATED,
    @CreatedDate @Column("created_at") var createDateTime: LocalDateTime? = null,
    @Column("last_login_at") var lastLoginDateTime: LocalDateTime? = null,
    @LastModifiedDate @Column("last_modified_at") var lastModifyDateTime: LocalDateTime? = null,
) : Persistable<UUID> {
    @org.springframework.data.annotation.Transient
    @JvmField
    var new: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = new
}
