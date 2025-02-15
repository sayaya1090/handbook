package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.Type
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
            INSERT INTO "user" (id, last_modified_at, created_at, last_login_at, name) 
            VALUES ('system', NOW(), NOW(), null, 'system');
            
            INSERT INTO type (id, last_modified_at, created_at, description, created_by, last_modified_by, parent, primitive) 
            VALUES ('type_1', NOW(), NOW(), 'type_1', 'system', 'system', null, false);
            INSERT INTO type (id, last_modified_at, created_at, description, created_by, last_modified_by, parent, primitive) 
            VALUES ('type_2', NOW(), NOW(), 'type_2', 'system', 'system', 'type_1', false);
            INSERT INTO type (id, last_modified_at, created_at, description, created_by, last_modified_by, parent, primitive) 
            VALUES ('type_3', NOW(), NOW(), 'type_3', 'system', 'system', 'type_2', false);

            INSERT INTO attribute (attribute_type, name, type, description, nullable, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) 
            VALUES ('Value', 'value_attr', 'type_2', 'Overwritten Attribute in Child 1', true, null, null, null, null, null, null);
            INSERT INTO attribute (attribute_type, name, type, description, nullable, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) 
            VALUES ('Map', 'map_attr', 'type_1', 'Common Attribute in Root', true, null, null, 'Array', null, 'Value', null);
            INSERT INTO attribute (attribute_type, name, type, description, nullable, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) 
            VALUES ('Array', 'array_attr', 'type_2', 'Unique Attribute in Child 1', false, null, null, null, null, 'Value', null);
            INSERT INTO attribute (attribute_type, name, type, description, nullable, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) 
            VALUES ('Document', 'document_attr', 'type_3', 'Exclusive Attribute in Child 2', false, null, null, null, null, null, 'type_1');
            INSERT INTO attribute (attribute_type, name, type, description, nullable, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) 
            VALUES ('File', 'file_attr', 'type_3', 'Exclusive Attribute in Child 2', false, null, 'png,jpg,jpeg', null, null, null, null);
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    should("Type으로 Attribute를 검색한다") {
        // Given: 미리 정의된 Type ID
        val type1Id = "type_1"
        val type2Id = "type_2"
        val type3Id = "type_3"

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
            attr is Attribute.Companion.MapAttribute &&
            attr.name == "map_attr" &&
            attr.description == "Common Attribute in Root" &&
            attr.nullable &&
            attr.keyType == AttributeType.Array &&
            attr.valueType == AttributeType.Value
        }.verifyComplete()

        StepVerifier.create(type2Result).expectNextMatches { attr ->
            attr is Attribute.Companion.ArrayAttribute &&
            attr.name == "array_attr" &&
            attr.description == "Unique Attribute in Child 1" &&
            attr.nullable.not() &&
            attr.valueType == AttributeType.Value
        }.expectNextMatches { attr ->
            attr is Attribute.Companion.ValueAttribute &&
            attr.name == "value_attr" &&
            attr.description == "Overwritten Attribute in Child 1" &&
            attr.nullable
        }.verifyComplete()

        StepVerifier.create(type3Result).expectNextMatches { attr ->
            attr is Attribute.Companion.DocumentAttribute &&
            attr.name == "document_attr" &&
            attr.description == "Exclusive Attribute in Child 2" &&
            attr.nullable.not() &&
            attr.referenceType == "type_1"
        }.expectNextMatches { attr ->
            attr is Attribute.Companion.FileAttribute &&
            attr.name == "file_attr" &&
            attr.description == "Exclusive Attribute in Child 2" &&
            attr.nullable.not() &&
            attr.extensions.containsAll(listOf("png", "jpg", "jpeg"))
        }.verifyComplete()
    }
    should("save 메서드를 통해 Attribute를 삽입, 업데이트 및 삭제한다") {
        // Given: 기존 Type과 연결된 Attribute들
        val typeId = "type_2"
        val type = mockk<Type>().apply {
            every { id } returns typeId
        }

        // 새로운 Attribute들 (삽입/업데이트/삭제 대상 포함)
        val newAttributes = listOf(
            Attribute.Companion.ValueAttribute("value_attr","Updated Attribute in Child 1",false), // 업데이트 대상
            Attribute.Companion.FileAttribute("new_file_attr","New File Attribute", setOf("pdf", "docx"),false), // 삽입 대상
            Attribute.Companion.DocumentAttribute("document_attr","Exclusive Attribute in Child 2","type_1", false) // 삽입 대상
        )

        // When: save 메서드를 호출하여 동기화 처리
        val result = repository.save(type, newAttributes)

        // Then: 삽입, 업데이트, 삭제된 Attribute를 검증
        StepVerifier.create(result).consumeNextWith { savedAttributes ->
            // 최종 저장된 Attribute를 검증
            val savedAttributeNames = savedAttributes.map { it.name }
            val updatedAttribute = savedAttributes.find { it.name == "value_attr" }
            val newFileAttribute = savedAttributes.find { it.name == "new_file_attr" }
            val newDocumentAttribute = savedAttributes.find { it.name == "document_attr" }

            // 검증: 최종 데이터에 포함된 Attribute
            savedAttributeNames.contains("value_attr") // 업데이트된 속성 포함
            savedAttributeNames.contains("new_file_attr") // 삽입된 속성 포함
            savedAttributeNames.contains("document_attr") // 삽입된 속성 포함

            // 검증: 업데이트된 Attribute
            assert(updatedAttribute is Attribute.Companion.ValueAttribute &&
                   updatedAttribute.description == "Updated Attribute in Child 1" &&
                   updatedAttribute.nullable == false)

            // 검증: 삽입된 File Attribute
            assert(newFileAttribute is Attribute.Companion.FileAttribute &&
                   newFileAttribute.extensions.containsAll(listOf("pdf", "docx")))

            // 검증: 삽입된 Document Attribute
            assert(newDocumentAttribute is Attribute.Companion.DocumentAttribute &&
                   newDocumentAttribute.referenceType == "type_1")

            // 삭제된 Attribute 검증
            assert(savedAttributeNames.indexOf("array_attr") == -1) // "array_attr"가 삭제되었는지 확인.

        }.verifyComplete()
    }
}) {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            Database.registerDynamicProperties(registry)
        }
    }
}