package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.usecase.AgentActivityEntry
import dev.sayaya.handbook.usecase.DistributionEntry
import dev.sayaya.handbook.usecase.QualityIssueEntry
import dev.sayaya.handbook.usecase.StatsRepository
import dev.sayaya.handbook.usecase.TimelineEntry
import dev.sayaya.handbook.usecase.WorkspaceSummary
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * [StatsRepository] 포트의 R2DBC 어댑터.
 *
 * **책임:** DatabaseClient를 사용하여 documents 테이블에서 GROUP BY 기반 통계 쿼리를 실행한다.
 *
 * **의존관계:**
 * - [DatabaseClient] — R2DBC SQL 실행
 *
 * **주의:** 타임라인 쿼리는 date_trunc과 interval 기반으로 그룹화하며,
 * 분포 쿼리는 현재 유효한 문서만 대상으로 한다.
 */
class R2dbcStatsRepository(
    private val databaseClient: DatabaseClient,
) : StatsRepository {

    override fun timeline(workspace: UUID, from: Instant, to: Instant, intervalDays: Long): Flux<TimelineEntry> {
        val sql = """
            SELECT date_trunc('day', create_date_time) AS bucket,
                   COUNT(*) AS document_count
            FROM documents
            WHERE workspace = :workspace
              AND create_date_time >= :from
              AND create_date_time < :to
            GROUP BY bucket
            ORDER BY bucket
        """.trimIndent()
        return databaseClient.sql(sql)
            .bind("workspace", workspace)
            .bind("from", from)
            .bind("to", to)
            .map { row, _ ->
                val bucket = row.get("bucket", Instant::class.java)!!
                TimelineEntry(
                    date = DateTimeFormatter.ISO_LOCAL_DATE.format(bucket.atOffset(ZoneOffset.UTC)),
                    documentCount = row.get("document_count", java.lang.Long::class.java)?.toLong() ?: 0L,
                )
            }
            .all()
    }

    override fun distribution(workspace: UUID): Flux<DistributionEntry> {
        val sql = """
            SELECT type, COUNT(*) AS count
            FROM documents
            WHERE workspace = :workspace
              AND effect_date_time <= NOW()
              AND expire_date_time > NOW()
            GROUP BY type
            ORDER BY count DESC
        """.trimIndent()
        return databaseClient.sql(sql)
            .bind("workspace", workspace)
            .map { row, _ ->
                DistributionEntry(
                    type = row.get("type", String::class.java)!!,
                    count = row.get("count", java.lang.Long::class.java)?.toLong() ?: 0L,
                )
            }
            .all()
    }

    override fun summary(workspace: UUID): Mono<WorkspaceSummary> {
        val sql = """
            SELECT
                (SELECT COUNT(DISTINCT type) FROM documents WHERE workspace = :workspace AND effect_date_time <= NOW() AND expire_date_time > NOW()) AS type_count,
                (SELECT COUNT(*) FROM documents WHERE workspace = :workspace AND effect_date_time <= NOW() AND expire_date_time > NOW()) AS document_count,
                (SELECT COUNT(DISTINCT gm.member) FROM group_member gm WHERE gm.workspace = :workspace) AS user_count
        """.trimIndent()
        return databaseClient.sql(sql)
            .bind("workspace", workspace)
            .map { row, _ ->
                WorkspaceSummary(
                    typeCount = row.get("type_count", java.lang.Long::class.java)?.toLong() ?: 0L,
                    documentCount = row.get("document_count", java.lang.Long::class.java)?.toLong() ?: 0L,
                    userCount = row.get("user_count", java.lang.Long::class.java)?.toLong() ?: 0L,
                )
            }
            .one()
            .defaultIfEmpty(WorkspaceSummary(0, 0, 0))
    }

    override fun qualityIssues(workspace: UUID): Flux<QualityIssueEntry> {
        val sql = """
            SELECT d.id AS document_id, d.serial, d.type, v.message AS issue, v.severity
            FROM documents d
            JOIN validation_results v ON d.id = v.document_id AND d.workspace = v.workspace
            WHERE d.workspace = :workspace
              AND d.effect_date_time <= NOW()
              AND d.expire_date_time > NOW()
              AND v.severity IN ('warning', 'error')
            ORDER BY v.severity DESC, d.serial
            LIMIT 100
        """.trimIndent()
        return databaseClient.sql(sql)
            .bind("workspace", workspace)
            .map { row, _ ->
                QualityIssueEntry(
                    documentId = row.get("document_id", String::class.java) ?: "",
                    serial = row.get("serial", String::class.java) ?: "",
                    type = row.get("type", String::class.java) ?: "",
                    issue = row.get("issue", String::class.java) ?: "",
                    severity = row.get("severity", String::class.java) ?: "warning",
                )
            }
            .all()
    }

    override fun agentActivity(workspace: UUID): Flux<AgentActivityEntry> {
        val sql = """
            SELECT created_by AS agent_id,
                   'document_change' AS action,
                   type AS target,
                   MAX(create_date_time)::text AS last_activity,
                   COUNT(*) AS count
            FROM documents
            WHERE workspace = :workspace
              AND create_date_time >= NOW() - INTERVAL '30 days'
            GROUP BY created_by, type
            ORDER BY MAX(create_date_time) DESC
            LIMIT 50
        """.trimIndent()
        return databaseClient.sql(sql)
            .bind("workspace", workspace)
            .map { row, _ ->
                AgentActivityEntry(
                    agentId = row.get("agent_id", String::class.java) ?: "",
                    action = row.get("action", String::class.java) ?: "",
                    target = row.get("target", String::class.java) ?: "",
                    timestamp = row.get("last_activity", String::class.java) ?: "",
                    count = row.get("count", java.lang.Long::class.java)?.toLong() ?: 0L,
                )
            }
            .all()
    }
}
