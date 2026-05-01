package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

/**
 * 워크스페이스 R2DBC 엔티티.
 *
 * **감사 컬럼 주입 전략 (2026-04-18):**
 * - `created_at` · `last_modified_at` 은 R2DBC auditing(`@CreatedDate`/`@LastModifiedDate`)
 *   이 정상 주입하므로 `lateinit var` 유지.
 * - `created_by` · `last_modified_by` 는 `@CreatedBy`/`@LastModifiedBy` 를 쓰지 않고
 *   **호출 측(어댑터) 이 명시 주입** 한다. 이유는 [dev.sayaya.handbook.usecase.WorkspaceRepository]
 *   KDoc 참조.
 */
@Table("workspace")
data class R2dbcWorkspaceEntity(
    @Id val id: UUID,
    var name: String,
    var description: String?,
    @Column("created_by") var createdBy: UUID,
    @Column("last_modified_by") var lastModifiedBy: UUID,
    @Version var version: Long? = null,
) {
    @CreatedDate @Column("created_at") lateinit var createdAt: Instant
    @LastModifiedDate @Column("last_modified_at") lateinit var lastModifiedAt: Instant
}
