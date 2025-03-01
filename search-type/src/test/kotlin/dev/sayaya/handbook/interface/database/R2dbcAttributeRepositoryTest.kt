package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
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
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

@DataR2dbcTest(properties = [
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
])
internal class R2dbcAttributeRepositoryTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    val repository = R2dbcAttributeRepository(template)
    beforeSpec {
        databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name) VALUES ('system', NOW(), NOW(), null, 'system');
            
            INSERT INTO public.type (id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('1ba494f9-387e-44a9-b211-8008299d7773', '2025-02-16 18:13:23.066000 +00:00', 'type_1', '1970-01-01 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', true, 'type_1', null, true, 't1-v1', 'system');
            INSERT INTO public.type (id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('94c220f1-7576-4d3b-96ff-6128be479f34', '2025-02-16 18:13:23.066000 +00:00', 'type_2', '1970-01-01 00:00:00.000000 +00:00', '1999-12-31 00:00:00.000000 +00:00', true, 'type_2', 'type_1', true, 't2-v1', 'system');
            INSERT INTO public.type (id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('d4cedd54-6423-45a6-86ca-821eae9b3573', '2025-02-16 18:13:23.066000 +00:00', 'type_2', '1999-12-31 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', true, 'type_2', 'type_1', true, 't2-v2', 'system');
            INSERT INTO public.type (id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('54aa4cd9-d12a-4015-886d-70c40fd0049b', '2025-02-16 18:13:23.066000 +00:00', 'type_3', '1970-01-01 00:00:00.000000 +00:00', '2005-12-31 00:00:00.000000 +00:00', true, 'type_3', 'type_2', true, 't3-v1', 'system');
            INSERT INTO public.type (id, created_at, description, effective_at, expire_at, last, name, parent, primitive, version, created_by) VALUES ('cd569d16-1f50-4cd1-85eb-74a763c98b5d', '2025-02-16 18:13:23.066000 +00:00', 'type_3', '2005-12-31 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', true, 'type_3', 'type_2', true, 't3-v2', 'system');
            
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Value', 'common_attr', 'Common Attribute in Root', true, null, null, null, null, null, null, '1ba494f9-387e-44a9-b211-8008299d7773');
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Value', 'common_attr', 'Overwritten Attribute in Child 1', true, null, null, null, null, null, null, '94c220f1-7576-4d3b-96ff-6128be479f34');
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Array', 'unique_attr', 'Unique Attribute in Child 1', false, null, 'Value', null, null, null, null, '94c220f1-7576-4d3b-96ff-6128be479f34');
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Document', 'exclusive_attr', 'Exclusive Attribute in Child 2', false, null, null, 'type_1', null, null, null, '54aa4cd9-d12a-4015-886d-70c40fd0049b');
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Value', 'unique_attr', 'Changed Attribute in Child 1', false, null, 'Value', null, null, null, null, 'd4cedd54-6423-45a6-86ca-821eae9b3573');
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Document', 'exclusive_attr', 'Exclusive Attribute in Child 2', false, null, null, 'type_1', null, null, null, 'cd569d16-1f50-4cd1-85eb-74a763c98b5d');
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Map', 'exclusive_attr2', 'Added Attribute in Child 2', false, null, 'Value', 'type_1', null, 'Value', null, 'cd569d16-1f50-4cd1-85eb-74a763c98b5d');
            INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, reference_type, file_extensions, key_type, key_validators, type) VALUES ('Value', 'common_attr', 'Overwritten Attribute in Child 2', true, null, null, null, null, null, null, 'cd569d16-1f50-4cd1-85eb-74a763c98b5d');
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    should("검색 쿼리에 따라 데이터를 검색하고 매핑한다") {
        // Given: 검색 파라미터와 Mock 데이터들
        // Mock: AttributeRepository 동작 설정
        val typeIds = listOf(
            UUID.fromString("1ba494f9-387e-44a9-b211-8008299d7773"),
            UUID.fromString("94c220f1-7576-4d3b-96ff-6128be479f34"),
            UUID.fromString("cd569d16-1f50-4cd1-85eb-74a763c98b5d")
        )

        // When: 리포지토리 검색 수행
        val result = repository.findAllByTypeIds(typeIds)

        // Then: 반환된 결과 검증
        StepVerifier.create(result)
            .consumeNextWith { attributesMap ->
                // 검증: 각 Type에 올바른 Attribute가 매핑되었는지 확인
                val attributesForType1 = attributesMap[UUID.fromString("1ba494f9-387e-44a9-b211-8008299d7773")]
                val attributesForType2 = attributesMap[UUID.fromString("94c220f1-7576-4d3b-96ff-6128be479f34")]
                val attributesForType3 = attributesMap[UUID.fromString("cd569d16-1f50-4cd1-85eb-74a763c98b5d")]

                requireNotNull(attributesForType1) { "Attributes for Type1 should not be null" }
                requireNotNull(attributesForType2) { "Attributes for Type2 should not be null" }
                requireNotNull(attributesForType3) { "Attributes for Type3 should not be null" }

                // Type 1: 'Value' Attribute 존재 여부 확인
                assert(attributesForType1.size == 1)
                assert(attributesForType1.any { it is Attribute.Companion.ValueAttribute && it.name == "common_attr" })

                // Type 2: 'Array'와 'Value' Attribute 체크
                assert(attributesForType2.size == 2)
                assert(attributesForType2.any { it is Attribute.Companion.ArrayAttribute && it.name == "unique_attr" })
                assert(attributesForType2.any { it is Attribute.Companion.ValueAttribute && it.name == "common_attr" })

                // Type 3: 'Document'와 'Map' Attribute 체크
                assert(attributesForType3.size == 3)
                assert(attributesForType3.any { it is Attribute.Companion.DocumentAttribute && it.name == "exclusive_attr" })
                assert(attributesForType3.any { it is Attribute.Companion.MapAttribute && it.name == "exclusive_attr2" })
                assert(attributesForType3.any { it is Attribute.Companion.ValueAttribute && it.name == "common_attr" })
            }.verifyComplete()
    }
    should("빈 ID 목록에 대해 빈 결과를 반환한다") {
        // Given: 빈 Type ID 목록
        val emptyTypeIds = emptyList<UUID>()

        // When: findAllByTypeIds 호출
        val result = repository.findAllByTypeIds(emptyTypeIds)

        // Then: 빈 결과 검증
        StepVerifier.create(result).assertNext { attributesMap -> assert(attributesMap.isEmpty()) }.verifyComplete()
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