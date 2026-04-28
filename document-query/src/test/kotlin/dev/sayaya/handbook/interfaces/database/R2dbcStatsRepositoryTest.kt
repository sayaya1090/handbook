package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.usecase.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*
import java.util.function.BiFunction

class R2dbcStatsRepositoryTest : DescribeSpec({
    val databaseClient = mockk<DatabaseClient>()
    val repository = R2dbcStatsRepository(databaseClient)

    describe("R2dbcStatsRepository Mapping") {
        val workspace = UUID.randomUUID()
        val spec = mockk<DatabaseClient.GenericExecuteSpec>(relaxed = true)
        val fetchSpec = mockk<org.springframework.r2dbc.core.RowsFetchSpec<*>>(relaxed = true)
        val row = mockk<io.r2dbc.spi.Row>()
        
        every { databaseClient.sql(any<String>()) } returns spec
        every { spec.bind(any<String>(), any()) } returns spec

        it("timeline mapping") {
            val mapperSlot = slot<BiFunction<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, TimelineEntry>>()
            val ts = mockk<org.springframework.r2dbc.core.RowsFetchSpec<TimelineEntry>>()
            every { spec.map(capture(mapperSlot)) } returns ts
            every { ts.all() } returns Flux.empty()
            every { row.get("bucket", Instant::class.java) } returns Instant.parse("2023-01-01T00:00:00Z")
            every { row.get("document_count", java.lang.Long::class.java) } returns 10L as java.lang.Long
            
            repository.timeline(workspace, Instant.now(), Instant.now(), 1).subscribe()
            val result = mapperSlot.captured.apply(row, mockk())
            result.date shouldBe "2023-01-01"
            result.documentCount shouldBe 10L
        }
        it("summary mapping") {
            val mapperSlot = slot<BiFunction<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, WorkspaceSummary>>()
            val ts = mockk<org.springframework.r2dbc.core.RowsFetchSpec<WorkspaceSummary>>()
            every { spec.map(capture(mapperSlot)) } returns ts
            every { ts.one() } returns Mono.empty()
            every { row.get("type_count", java.lang.Long::class.java) } returns 1L as java.lang.Long
            every { row.get("document_count", java.lang.Long::class.java) } returns 2L as java.lang.Long
            every { row.get("user_count", java.lang.Long::class.java) } returns 3L as java.lang.Long
            
            repository.summary(workspace).subscribe()
            val result = mapperSlot.captured.apply(row, mockk())
            result.typeCount shouldBe 1L
            result.documentCount shouldBe 2L
            result.userCount shouldBe 3L
        }
        it("summary: 결과가 없을 때 기본값을 반환한다") {
            val ts = mockk<org.springframework.r2dbc.core.RowsFetchSpec<WorkspaceSummary>>()
            every { spec.map(any<BiFunction<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, WorkspaceSummary>>()) } returns ts
            every { ts.one() } returns Mono.empty()
            
            repository.summary(workspace).block() shouldBe WorkspaceSummary(0, 0, 0)
        }
        it("qualityIssues mapping") {
            val mapperSlot = slot<BiFunction<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, QualityIssueEntry>>()
            val ts = mockk<org.springframework.r2dbc.core.RowsFetchSpec<QualityIssueEntry>>()
            every { spec.map(capture(mapperSlot)) } returns ts
            every { ts.all() } returns Flux.empty()
            every { row.get("document_id", String::class.java) } returns "d1"
            every { row.get("serial", String::class.java) } returns "s1"
            every { row.get("type", String::class.java) } returns "t1"
            every { row.get("issue", String::class.java) } returns "i1"
            every { row.get("severity", String::class.java) } returns "error"
            
            repository.qualityIssues(workspace).subscribe()
            val result = mapperSlot.captured.apply(row, mockk())
            result.documentId shouldBe "d1"
            result.severity shouldBe "error"
        }
        it("agentActivity mapping") {
            val mapperSlot = slot<BiFunction<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, AgentActivityEntry>>()
            val ts = mockk<org.springframework.r2dbc.core.RowsFetchSpec<AgentActivityEntry>>()
            every { spec.map(capture(mapperSlot)) } returns ts
            every { ts.all() } returns Flux.empty()
            every { row.get("agent_id", String::class.java) } returns "a1"
            every { row.get("action", String::class.java) } returns "act"
            every { row.get("target", String::class.java) } returns "tar"
            every { row.get("last_activity", String::class.java) } returns "2023-01-01"
            every { row.get("count", java.lang.Long::class.java) } returns 5L as java.lang.Long
            
            repository.agentActivity(workspace).subscribe()
            val result = mapperSlot.captured.apply(row, mockk())
            result.agentId shouldBe "a1"
            result.count shouldBe 5L
        }
    }
})
