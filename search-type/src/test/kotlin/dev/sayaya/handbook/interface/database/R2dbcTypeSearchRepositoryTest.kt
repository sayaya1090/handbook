package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Search
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

@DataR2dbcTest(properties = [
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
])
internal class R2dbcTypeSearchRepositoryTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    val attributeRepo = mockk<R2dbcAttributeRepository>()
    val repository = R2dbcTypeSearchRepository(template, attributeRepo)
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    beforeSpec {
        // 테스트 데이터 삽입
        databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name, provider, account) VALUES ('93951bc3-be1e-4fc8-865f-d6376ac3e87b', NOW(), NOW(), null, 'system', 'handbook', 'system');
            
            INSERT INTO public.type (workspace, id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('398f6038-2192-417b-914a-f74e4bf52451', '1ba494f9-387e-44a9-b211-8008299d7773', '2025-02-16 18:13:23.066000 +00:00', 'type_1', '1970-01-01 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', true, 'type_1', null, true, 't1-v1', '93951bc3-be1e-4fc8-865f-d6376ac3e87b');
            INSERT INTO public.type (workspace, id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('398f6038-2192-417b-914a-f74e4bf52451', '94c220f1-7576-4d3b-96ff-6128be479f34', '2025-02-16 18:13:23.066000 +00:00', 'type_2', '1970-01-01 00:00:00.000000 +00:00', '1999-12-31 00:00:00.000000 +00:00', true, 'type_2', 'type_1', true, 't2-v1', '93951bc3-be1e-4fc8-865f-d6376ac3e87b');
            INSERT INTO public.type (workspace, id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('398f6038-2192-417b-914a-f74e4bf52451', 'd4cedd54-6423-45a6-86ca-821eae9b3573', '2025-02-16 18:13:23.066000 +00:00', 'type_2', '1999-12-31 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', true, 'type_2', 'type_1', true, 't2-v2', '93951bc3-be1e-4fc8-865f-d6376ac3e87b');
            INSERT INTO public.type (workspace, id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('398f6038-2192-417b-914a-f74e4bf52451', '54aa4cd9-d12a-4015-886d-70c40fd0049b', '2025-02-16 18:13:23.066000 +00:00', 'type_3', '1970-01-01 00:00:00.000000 +00:00', '2005-12-31 00:00:00.000000 +00:00', true, 'type_3', 'type_2', true, 't3-v1', '93951bc3-be1e-4fc8-865f-d6376ac3e87b');
            INSERT INTO public.type (workspace, id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('398f6038-2192-417b-914a-f74e4bf52451', 'cd569d16-1f50-4cd1-85eb-74a763c98b5d', '2025-02-16 18:13:23.066000 +00:00', 'type_3', '2005-12-31 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', true, 'type_3', 'type_2', true, 't3-v2', '93951bc3-be1e-4fc8-865f-d6376ac3e87b');
        """.trimIndent()).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }

    should("검색 조건에 따라 Type 데이터를 반환한다") {
        // Given: 검색 파라미터 설정
        val param = Search(
            filters = listOf("name" to "type_1"),
            page = 0,
            limit = 10,
            asc = true,
            sortBy = "name"
        )

        // Mock: AttributeRepository 동작 설정
        val typeId = UUID.fromString("1ba494f9-387e-44a9-b211-8008299d7773")
        val mockAttributes = mapOf(
            typeId to listOf(
                Attribute.Companion.ValueAttribute("attr1", "description1", true, false)
            )
        )
        every { attributeRepo.findAllByTypeIds(workspace, any()) } returns Mono.just(mockAttributes)

        // When: search() 호출
        val result = repository.search(workspace, param)

        // Then: 검색 결과 검증
        StepVerifier.create(result).assertNext { page ->
            assert(page.content.size == 1)
            val type = page.content.first()
            assert(type.id == "type_1")
            assert(type.parent == null)
            assert(type.version == "t1-v1")
            assert(type.attributes.size == 1)
            val attr1 = type.attributes.first() as? Attribute.Companion.ValueAttribute
            assert(attr1 != null && attr1.name == "attr1" && attr1.description == "description1" && attr1.nullable)
        }.verifyComplete()
    }

    should("검색 결과가 없으면 빈 결과를 반환한다") {
        // Given: 검색 파라미터 설정
        val param = Search(
            filters = listOf("name" to "NonexistentType"),
            page = 0,
            limit = 10,
            asc = true,
            sortBy = "name"
        )

        // Mock: AttributeRepository 없는 경우
        every { attributeRepo.findAllByTypeIds(workspace, any()) } returns Mono.just(emptyMap())

        // When: search() 호출
        val result = repository.search(workspace, param)

        // Then: 빈 결과 검증
        StepVerifier.create(result).verifyComplete()
    }

    should("필터가 없을 때 기본 동작으로 검색한다") {
        // Given: 필터가 비어 있는 검색 파라미터 설정
        val param = Search(
            filters = emptyList(),
            page = 0,
            limit = 10,
            asc = true,
            sortBy = "name"
        )

        // Mock: AttributeRepository 동작 설정
        val typeId1 = UUID.fromString("1ba494f9-387e-44a9-b211-8008299d7773")
        val typeId2 = UUID.fromString("d4cedd54-6423-45a6-86ca-821eae9b3573")
        val typeId3 = UUID.fromString("cd569d16-1f50-4cd1-85eb-74a763c98b5d")
        val mockAttributes = mapOf(
            typeId1 to listOf(Attribute.Companion.ValueAttribute("attr1", "desc", true, false)),
            typeId2 to listOf(Attribute.Companion.ValueAttribute("attr2", "desc", false, false)),
            typeId3 to listOf(Attribute.Companion.ValueAttribute("attr3", "desc", false, false), Attribute.Companion.ValueAttribute("attr3-2", "desc", false, false)),
        )
        every { attributeRepo.findAllByTypeIds(workspace, any()) } returns Mono.just(mockAttributes)

        // When: search() 호출
        val result = repository.search(workspace, param)

        // Then: 모든 Type 검색 결과 검증
        StepVerifier.create(result).assertNext { page ->
            assert(page.content.size == 5) { "Expected 5 types, but found ${page.content.size}" }
            val typesById = page.content.associateBy { it.id }
            val type1 = typesById["type_1"] ?: error("type_1 not found")
            val type2 = typesById["type_2"] ?: error("type_2 not found")
            val type3 = typesById["type_3"] ?: error("type_3 not found")

            assert(type1.attributes.size == 1)
            assert(type2.attributes.size == 1)
            assert(type3.attributes.size == 2)
        }.verifyComplete()
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