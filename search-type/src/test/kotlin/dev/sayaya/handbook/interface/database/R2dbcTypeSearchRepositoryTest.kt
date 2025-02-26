package dev.sayaya.handbook.`interface`.database

import dev.sayaya.domain.Search
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@DataR2dbcTest
class R2dbcTypeSearchRepositoryTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    val attributeRepo = mockk<R2dbcAttributeRepository>()
    val repository = R2dbcTypeSearchRepository(template, attributeRepo)

    beforeSpec {
        // 테스트 데이터 삽입
        databaseClient.sql("""
            INSERT INTO public.type (id, name, created_at, effective_at, expire_at, last, description, version, parent, primitive) VALUES 
            ('1ba494f9-387e-44a9-b211-8008299d7773', 'Type1', NOW(), '1970-01-01', '2999-12-31', true, 'Description1', 'v1', null, true),
            ('94c220f1-7576-4d3b-96ff-6128be479f34', 'Type2', NOW(), '1970-01-01', '2999-12-31', true, 'Description2', 'v1', 'Type1', true);
        """.trimIndent()).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }

    should("검색 조건에 따라 Type 데이터를 반환한다") {
        // Given: 검색 파라미터 설정
        val param = Search(
            filters = listOf("name" to "Type1"),
            page = 0,
            limit = 10,
            asc = true,
            sortBy = "created_at"
        )

        // Mock: AttributeRepository 동작 설정
        val typeId = UUID.fromString("1ba494f9-387e-44a9-b211-8008299d7773")
        val mockAttributes = mapOf(
            typeId to listOf(
                Attribute.Companion.ValueAttribute("attr1", "description1", true, false)
            )
        )
        every { attributeRepo.findAllByTypeIds(any()) } returns Mono.just(mockAttributes)

        // When: search() 호출
        val result = repository.search(param)

        // Then: 검색 결과 검증
        StepVerifier.create(result)
            .assertNext { page ->
                assert(page.content.size == 1)
                val type = page.content.first()
                assert(type.id == "Type1")
                assert(type.parent == null)
                assert(type.version == "v1")
                assert(type.attributes.size == 1)
                val attr1 = type.attributes.first() as? Attribute.Companion.ValueAttribute
                assert(attr1 != null && attr1.name == "attr1" && attr1.description == "description1" && attr1.nullable)
            }
            .verifyComplete()
    }

    should("검색 결과가 없으면 빈 결과를 반환한다") {
        // Given: 검색 파라미터 설정
        val param = Search(
            filters = listOf("name" to "NonexistentType"),
            page = 0,
            limit = 10,
            asc = true,
            sortBy = "created_at"
        )

        // Mock: AttributeRepository 없는 경우
        every { attributeRepo.findAllByTypeIds(any()) } returns Mono.just(emptyMap())

        // When: search() 호출
        val result = repository.search(param)

        // Then: 빈 결과 검증
        StepVerifier.create(result)
            .verifyComplete()
    }

    should("필터가 없을 때 기본 동작으로 검색한다") {
        // Given: 필터가 비어 있는 검색 파라미터 설정
        val param = Search(
            filters = emptyList(),
            page = 0,
            limit = 10,
            asc = true,
            sortBy = "created_at"
        )

        // Mock: AttributeRepository 동작 설정
        val typeId1 = UUID.fromString("1ba494f9-387e-44a9-b211-8008299d7773")
        val typeId2 = UUID.fromString("94c220f1-7576-4d3b-96ff-6128be479f34")
        val mockAttributes = mapOf(
            typeId1 to listOf(Attribute.Companion.ValueAttribute("attr1", "desc", true, false)),
            typeId2 to listOf(Attribute.Companion.ValueAttribute("attr2", "desc", false, false))
        )
        every { attributeRepo.findAllByTypeIds(any()) } returns Mono.just(mockAttributes)

        // When: search() 호출
        val result = repository.search(param)

        // Then: 모든 Type 검색 결과 검증
        StepVerifier.create(result)
            .assertNext { page ->
                assert(page.content.size == 2)
                val type1 = page.content.first { it.id == "Type1" }
                val type2 = page.content.first { it.id == "Type2" }
                assert(type1.attributes.size == 1)
                assert(type2.attributes.size == 1)
            }
            .verifyComplete()
    }
}) {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            Database().registerDynamicProperties(registry)
        }
    }
}