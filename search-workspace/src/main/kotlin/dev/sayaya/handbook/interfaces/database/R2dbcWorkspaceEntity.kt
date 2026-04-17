package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

/**
 * persist-workspace 가 소유한 `workspace` 테이블의 **읽기 전용 투영**.
 *
 * **책임:** 조회에 필요한 최소 필드(id, name, description)만 포함한다.
 * `@Version`, 감사 컬럼(created_at/by 등) 은 search 에서 불필요하므로 매핑하지 않는다.
 *
 * **주의:** 동일 테이블을 바라보지만 쓰기 권한 없음 — DB 세션이
 * `default_transaction_read_only=on` 이라 write 시 PostgreSQL 이 거부한다.
 */
@Table("workspace")
data class R2dbcWorkspaceEntity(
    @Id val id: UUID,
    val name: String,
    val description: String?,
)
