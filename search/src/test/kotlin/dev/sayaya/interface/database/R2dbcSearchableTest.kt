package dev.sayaya.`interface`.database

import dev.sayaya.domain.Search
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.data.domain.*
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.sql.SqlIdentifier
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@Suppress("ReactiveStreamsUnusedPublisher")
internal class R2dbcSearchableTest : ShouldSpec({
    extensions(SpringExtension)
    context("search 메서드 동작 확인") {
        should("검색 결과가 있는 경우 올바른 페이지 객체를 반환한다") {
            val template = mockk<R2dbcEntityTemplate>()
            val searchable = TestR2dbcSearchable(template)
            // Mock count and data
            every { template.count(any<Query>(), clazz) } returns Mono.just(100L)
            every { template.select(clazz).from(from).matching(any<Query>()).all().collectList() } returns Mono.just(
                listOf(
                    TestEntity(1, "data1"),
                    TestEntity(2, "data2")
                )
            )
            // Run search
            val search = Search(page = 0, limit = 10, sortBy = "id", asc = true, filters = filters)

            // Assert
            searchable.search(search).let(StepVerifier::create).assertNext { page ->
                page.totalElements shouldBe 100
                page.content shouldBe listOf(
                    TestEntity(1, "data1"),
                    TestEntity(2, "data2")
                )
            }.verifyComplete()

            // Verify interactions
            verify { template.count(any<Query>(), clazz) }
            verify { template.select(clazz).from(from).matching(any<Query>()).all() }
        }

        should("검색 결과가 없는 경우 아무것도 반환하지 않는다") {
            val template: R2dbcEntityTemplate = mockk()
            val searchable = TestR2dbcSearchable(template)
            // Mock count and data
            every { template.count(any<Query>(), clazz) } returns Mono.just(0L)
            every { template.select(clazz).from(from).matching(any<Query>()).all().collectList() } returns Mono.just(
                emptyList()
            )
            // Run search
            val search = Search(page = 0, limit = 10, sortBy = "id", asc = true, filters = filters)

            // Assert
            searchable.search(search).let(StepVerifier::create).verifyComplete()

            // Verify interactions
            verify { template.count(any<Query>(), clazz) }
            verify { template.select(clazz).from(from).matching(any<Query>()).all() }
        }
    }
}) {
    companion object {
        data class TestEntity(val id: Int, val data: String)
        val from: SqlIdentifier = SqlIdentifier.unquoted("test_table")
        val clazz = TestEntity::class.java
        val filters = listOf("key1" to "value1", "key2" to "value2")

        class TestR2dbcSearchable(private val template: R2dbcEntityTemplate) : R2dbcSearchable<TestEntity, TestEntity> {
            override fun R2dbcEntityTemplate.predicate(key: String, value: String): Criteria {
                return Criteria.where(key).`is`(value)
            }
            override fun search(param: Search): Mono<Page<TestEntity>> {
                val pageNumber = param.page
                val pageSize = param.limit
                val sortBy = param.sortBy?.let(::property) ?: "create_at"
                val sort = (param.asc?.let { if(it) Sort.Order.asc(sortBy) else Sort.Order.desc(sortBy) } ?: Sort.Order.desc(sortBy)).let { Sort.by(it) }
                val pageable = PageRequest.of(pageNumber, pageSize, sort)
                return template.search(from, param.filters, clazz, pageable).mapNotNull { page ->
                    if(page.content.isEmpty()) null
                    else PageImpl(page.content.map(::map), page.pageable, page.totalElements)
                }
            }
            private fun map(entity: TestEntity): TestEntity = entity
            private fun property(name: String): String? = when(name) {
                "id" -> "id"
                else -> null
            }
        }
    }
}