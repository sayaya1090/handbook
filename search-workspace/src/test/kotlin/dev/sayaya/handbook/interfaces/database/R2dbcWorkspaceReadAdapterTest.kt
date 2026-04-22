package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Workspace
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.BiFunction

class R2dbcWorkspaceReadAdapterTest : DescribeSpec({
    val template = mockk<R2dbcEntityTemplate>()
    val databaseClient = mockk<DatabaseClient>()
    val adapter = R2dbcWorkspaceReadAdapter(template, databaseClient)

    describe("R2dbcWorkspaceReadAdapter") {
        it("findAll: 전체 워크스페이스를 조회하고 도메인으로 매핑한다") {
            val entity = R2dbcWorkspaceEntity(UUID.randomUUID(), "alpha", "desc")
            // fluent API 모킹
            every { template.select(R2dbcWorkspaceEntity::class.java) } returns mockk {
                every { all() } returns Flux.just(entity)
            }
            
            val result = adapter.findAll().collectList().block()!!
            result[0].id shouldBe entity.id
            result[0].name shouldBe "alpha"
            result[0].description shouldBe "desc"
        }
        it("findById: ID로 단건 조회하고 도메인으로 매핑한다") {
            val id = UUID.randomUUID()
            val entity = R2dbcWorkspaceEntity(id, "beta", null)
            every { template.selectOne(any(), R2dbcWorkspaceEntity::class.java) } returns Mono.just(entity)
            
            val result = adapter.findById(id).block()!!
            result.id shouldBe id
            result.name shouldBe "beta"
            result.description shouldBe null
        }
        it("findByUserSub: SQL 쿼리 결과를 도메인으로 매핑한다") {
            val sub = UUID.randomUUID()
            val spec = mockk<DatabaseClient.GenericExecuteSpec>()
            val fetchSpec = mockk<org.springframework.r2dbc.core.RowsFetchSpec<Workspace>>()
            val mapperSlot = slot<BiFunction<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, Workspace>>()
            
            every { databaseClient.sql(any<String>()) } returns spec
            every { spec.bind("sub", sub) } returns spec
            every { spec.map(capture(mapperSlot)) } returns fetchSpec
            every { fetchSpec.all() } returns Flux.empty()
            
            adapter.findByUserSub(sub).collectList().block()
            
            val row = mockk<io.r2dbc.spi.Row>()
            val id = UUID.randomUUID()
            every { row.get("id", UUID::class.java) } returns id
            every { row.get("name", String::class.java) } returns "gamma"
            every { row.get("description", String::class.java) } returns "delta"
            
            val result = mapperSlot.captured.apply(row, mockk())
            result.id shouldBe id
            result.name shouldBe "gamma"
            result.description shouldBe "delta"
        }
    }
})
