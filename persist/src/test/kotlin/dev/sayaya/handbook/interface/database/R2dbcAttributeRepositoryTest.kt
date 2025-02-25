package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

@DataR2dbcTest(properties = [
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
])
class R2dbcAttributeRepositoryTest @Autowired constructor(
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
    should("Type에 연결된 Attribute를 검색한다") {
        // Given: 미리 정의된 Type ID
        val type1Id = UUID.fromString("1ba494f9-387e-44a9-b211-8008299d7773")
        val type2Id = UUID.fromString("94c220f1-7576-4d3b-96ff-6128be479f34")
        val type3Id = UUID.fromString("54aa4cd9-d12a-4015-886d-70c40fd0049b")

        // When: 특정 Type과 연관된 Attribute를 검색
        val type1Result = repository.findByType(type1Id).collectList().map { list ->
            list.sortedBy { it.name }
        }.flatMapMany { Flux.fromIterable(it) }
        val type2Result = repository.findByType(type2Id).collectList().map { list ->
            list.sortedBy { it.name }
        }.flatMapMany { Flux.fromIterable(it) }
        val type3Result = repository.findByType(type3Id).collectList().map { list ->
            list.sortedBy { it.name }
        }.flatMapMany { Flux.fromIterable(it) }

        // Then: 반환된 Attribute들을 검증
        StepVerifier.create(type1Result).expectNextMatches { attr ->
            attr is Attribute.Companion.ValueAttribute &&
            attr.name == "common_attr" &&
            attr.description == "Common Attribute in Root" &&
            attr.nullable
        }.verifyComplete()
        StepVerifier.create(type2Result).expectNextMatches { attr ->
            attr is Attribute.Companion.ValueAttribute &&
            attr.name == "common_attr" &&
            attr.description == "Overwritten Attribute in Child 1" &&
            attr.nullable
        }.expectNextMatches { attr ->
            attr is Attribute.Companion.ArrayAttribute &&
            attr.name == "unique_attr" &&
            attr.description == "Unique Attribute in Child 1" &&
            attr.nullable.not() &&
            attr.valueType == AttributeType.Value
        }.verifyComplete()
        StepVerifier.create(type3Result).expectNextMatches { attr ->
            attr is Attribute.Companion.DocumentAttribute &&
            attr.name == "exclusive_attr" &&
            attr.description == "Exclusive Attribute in Child 2" &&
            attr.nullable.not() &&
            attr.referenceType == "type_1"
        }.verifyComplete()
    }
    should("save 메서드를 통해 Attribute를 삽입, 업데이트 및 삭제한다") {
        // Given: 기존 Type과 연결된 Attribute들
        val typeId = UUID.fromString("94c220f1-7576-4d3b-96ff-6128be479f34")
        val type = mockk<R2dbcTypeEntity>().apply {
            every { id } returns typeId
        }

        // 새로운 Attribute들 (삽입/업데이트/삭제 대상 포함)
        val newAttributes = listOf(
            Attribute.Companion.ValueAttribute("common_attr","Updated Attribute in Child 1",false, false), // 업데이트 대상
            Attribute.Companion.FileAttribute("new_file_attr","New File Attribute", setOf("pdf", "docx"),false, false), // 삽입 대상
            Attribute.Companion.DocumentAttribute("document_attr","Exclusive Attribute in Child 2","type_1", false, false) // 삽입 대상
        )

        // When: save 메서드를 호출하여 동기화 처리
        val result = repository.save(type, newAttributes)

        // Then: 삽입, 업데이트, 삭제된 Attribute를 검증
        StepVerifier.create(result).consumeNextWith { savedAttributes ->
            // 최종 저장된 Attribute를 검증
            val savedAttributeNames = savedAttributes.map { it.name }
            val updatedAttribute = savedAttributes.find { it.name == "common_attr" }
            val newFileAttribute = savedAttributes.find { it.name == "new_file_attr" }
            val newDocumentAttribute = savedAttributes.find { it.name == "document_attr" }

            // 검증: 최종 데이터에 포함된 Attribute
            savedAttributeNames.contains("common_attr") // 업데이트된 속성 포함
            savedAttributeNames.contains("new_file_attr") // 삽입된 속성 포함
            savedAttributeNames.contains("document_attr") // 삽입된 속성 포함

            // 검증: 업데이트된 Attribute
            assert(
                updatedAttribute is Attribute.Companion.ValueAttribute &&
                updatedAttribute.description == "Updated Attribute in Child 1" &&
                updatedAttribute.nullable.not()
            )

            // 검증: 삽입된 File Attribute
            assert(newFileAttribute is Attribute.Companion.FileAttribute &&
                   newFileAttribute.extensions.containsAll(listOf("pdf", "docx")))

            // 검증: 삽입된 Document Attribute
            assert(newDocumentAttribute is Attribute.Companion.DocumentAttribute &&
                   newDocumentAttribute.referenceType == "type_1")

            // 삭제된 Attribute 검증
            assert(savedAttributeNames.indexOf("unique_attr") == -1) // "unique_attr"가 삭제되었는지 확인.
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