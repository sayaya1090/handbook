package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ShouldSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

@DataR2dbcTest(properties = [
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
])
internal class R2dbcTypeRepositoryTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    val objectMapper = ObjectMapper()
        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .registerModule(JavaTimeModule().addDeserializer(Instant::class.java, object : JsonDeserializer<Instant>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
                val epochMillis = p.longValue
                return Instant.ofEpochMilli(epochMillis)
            }
        })).registerModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    val repository = R2dbcTypeRepository(template, objectMapper)
    // 테스트에 사용할 고정된 Workspace UUID
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    val otherWorkspace = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee") // 다른 워크스페이스 테스트용
    val userId = UUID.fromString("93951bc3-be1e-4fc8-865f-d6376ac3e87b") // created_by 용

    val t1 = Instant.parse("2025-01-01T10:00:00Z") // 10:00
    val t2 = Instant.parse("2025-01-01T11:00:00Z") // 11:00
    val t3 = Instant.parse("2025-01-01T12:00:00Z") // 12:00
    val t4 = Instant.parse("2025-01-01T13:00:00Z") // 13:00
    val t5 = Instant.parse("2025-01-01T14:00:00Z") // 14:00

    // 테스트 데이터 ID 미리 정의 (검증 시 사용)
    val type1Id = UUID.randomUUID()
    val type2Id = UUID.randomUUID()
    val type3Id = UUID.randomUUID()
    val typeOtherId = UUID.randomUUID()
    val typeOldId = UUID.randomUUID()
    val type4Id = UUID.randomUUID()

    // 기대되는 Type 도메인 객체 정의 (toDomain 로직과 일치하게)
    // R2dbcTypeEntity의 기본값(x, y, width, height=0, parent=null 등)을 반영해야 함
    fun createExpectedType(name: String, version: String, effect: Instant, expire: Instant): Type {
        return Type(
            id = name, // R2dbcTypeEntity.name
            version = version,
            effectDateTime = effect,
            expireDateTime = expire,
            description = "",
            primitive = false,
            attributes = emptyList(),
            parent = null, // 기본값 null
            x = 0.toUShort(), // 기본값 0
            y = 0.toUShort(), // 기본값 0
            width = 100.toUShort(), // 기본값 100
            height = 200.toUShort() // 기본값 200
        )
    }

    val expectedType1 = createExpectedType("type1", "v1", t1, t3) // 10:00 ~ 12:00
    val expectedType2 = createExpectedType("type2", "v1", t2, t4) // 11:00 ~ 13:00
    val expectedType3 = createExpectedType("type3", "v1", t4, t5) // 13:00 ~ 14:00
    val expectedType4 = createExpectedType("type4_single_interval", "v1", t1, t2) // 10:00 ~ 11:00
    // type_old는 last=false 이므로 findByRange 결과에 포함되지 않음
    // type_other는 다른 workspace이므로 결과에 포함되지 않음

    beforeSpec {
        // 테스트 데이터 삽입
        // User 데이터 (Type 테이블의 created_by 외래키 충족을 위해 필요)
        val insertUser = databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name, provider, account)
            VALUES (:userId, NOW(), NOW(), null, 'test-user', 'handbook', 'test-account')
            ON CONFLICT (id) DO NOTHING;
        """.trimIndent())
            .bind("userId", userId)
            .fetch().rowsUpdated()
        val insertWorkspace = databaseClient.sql("""
            INSERT INTO public.workspace (id, created_at, created_by, last_modified_at, last_modified_by, name, description)
            VALUES (:workspace, NOW(), :userId, NOW(), :userId, 'test-workspace', 'Test Workspace')
            ON CONFLICT (id) DO NOTHING;
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .fetch().rowsUpdated()
        val insertOtherWorkspace = databaseClient.sql("""
            INSERT INTO public.workspace (id, created_at, created_by, last_modified_at, last_modified_by, name, description)
            VALUES (:workspace, NOW(), :userId, NOW(), :userId, 'test-workspace', 'Test Workspace')
            ON CONFLICT (id) DO NOTHING;
        """.trimIndent())
            .bind("workspace", otherWorkspace)
            .bind("userId", userId)
            .fetch().rowsUpdated()

        // 테스트용 Type 데이터 삽입 (Scenario 1 - 개별 INSERT 문으로 분리)
        // x, y, width, height, parent 컬럼 추가 (기본값 0, null)
        val insertScenario1Type1 = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description, parent, x, y, width, height) VALUES
            (:workspace, :typeId, NOW(), 'type1', 'v1', :t1, :t3, true, :userId, false, '', null, -32768, -32768, -32668, -32568) -- 10:00 ~ 12:00
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", type1Id) // 미리 정의된 ID 사용
            .bind("t1", t1)
            .bind("t3", t3)
            .fetch().rowsUpdated()

        val insertScenario1Type2 = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description, parent, x, y, width, height) VALUES
            (:workspace, :typeId, NOW(), 'type2', 'v1', :t2, :t4, true, :userId, false, '', null, -32768, -32768, -32668, -32568) -- 11:00 ~ 13:00
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", type2Id) // 미리 정의된 ID 사용
            .bind("t2", t2)
            .bind("t4", t4)
            .fetch().rowsUpdated()

        val insertScenario1Type3 = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description, parent, x, y, width, height) VALUES
            (:workspace, :typeId, NOW(), 'type3', 'v1', :t4, :t5, true, :userId, false, '', null, -32768, -32768, -32668, -32568) -- 13:00 ~ 14:00
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", type3Id) // 미리 정의된 ID 사용
            .bind("t4", t4)
            .bind("t5", t5)
            .fetch().rowsUpdated()

        val insertScenario2Type = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description, parent, x, y, width, height) VALUES
            (:otherWorkspace, :typeId, NOW(), 'type_other', 'v1', :t1, :t2, true, :userId, false, '', null, -32768, -32768, -32668, -32568);
        """.trimIndent())
            .bind("otherWorkspace", otherWorkspace)
            .bind("userId", userId)
            .bind("typeId", typeOtherId) // 미리 정의된 ID 사용
            .bind("t1", t1)
            .bind("t2", t2)
            .fetch().rowsUpdated()

        val insertScenario3Type = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description, parent, x, y, width, height) VALUES
            (:workspace, :typeId, NOW(), 'type_old', 'v0', :t1, :t2, false, :userId, false, '', null, -32768, -32768, -32668, -32568); 
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", typeOldId) // 미리 정의된 ID 사용
            .bind("t1", t1)
            .bind("t2", t2)
            .fetch().rowsUpdated()

        val insertScenario4Type = databaseClient.sql("""
             INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description, parent, x, y, width, height) VALUES
             (:workspace, :typeId, NOW(), 'type4_single_interval', 'v1', :t1, :t2, true, :userId, false, '', null, -32768, -32768, -32668, -32568);
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", type4Id) // 미리 정의된 ID 사용
            .bind("t1", t1)
            .bind("t2", t2)
            .fetch().rowsUpdated()


        // 모든 INSERT 문을 순차적으로 실행하고 최종 완료를 검증
        insertUser
            .then(insertWorkspace) // 워크스페이스 데이터 삽입
            .then(insertOtherWorkspace) // 다른 워크스페이스 데이터 삽입
            .then(insertScenario1Type1) // Scenario 1의 첫 번째 데이터
            .then(insertScenario1Type2) // Scenario 1의 두 번째 데이터
            .then(insertScenario1Type3) // Scenario 1의 세 번째 데이터
            .then(insertScenario2Type) // 시나리오 1 insert 후 시나리오 2 insert
            .then(insertScenario3Type) // ... 순차적으로 실행
            .then(insertScenario4Type)
            .then()
            .let(StepVerifier::create)
            .verifyComplete() // 모든 작업이 오류 없이 완료되었는지 확인
    }
    context("findAll") {

    }
    context("findByRange") {
        should("주어진 시간 범위와 완전히 겹치는 타입을 찾아야 한다 (10:30 ~ 11:30)") {
            // Given: Query range (t1 < queryStart < t2) and (t2 < queryEnd < t3)
            val queryStart = Instant.parse("2025-01-01T10:30:00Z")
            val queryEnd = Instant.parse("2025-01-01T11:30:00Z")
            // When & Then:
            // Expected: type1 (10:00 ~ 12:00), type2 (11:00 ~ 13:00), type4 (10:00 ~ 11:00)
            // type1은 effective_at(10:00) < queryEnd(11:30) 이고 expire_at(12:00) >= queryStart(10:30) 이므로 포함.
            // type2는 effective_at(11:00) < queryEnd(11:30) 이고 expire_at(13:00) >= queryStart(10:30) 이므로 포함.
            repository.findByRange(workspace, queryStart, queryEnd)
                .collectSortedList(compareBy({ it.id }, { it.version })) // 결과를 이름과 버전으로 정렬하여 순서 보장
                .let(StepVerifier::create)
                .expectNext(listOf(expectedType1, expectedType2, expectedType4).sortedBy { it.id }) // 순서 보장된 기대값 리스트
                .verifyComplete()
        }
        should("주어진 시간 범위의 시작점과 끝점에 걸치는 타입을 찾아야 한다 (11:00 ~ 13:00)") {
            // Given: Query range (t2 ~ t4)
            val queryStart = t2 // 11:00
            val queryEnd = t4 // 13:00

            // When & Then:
            // Expected: type1 (10:00 ~ 12:00), type2 (11:00 ~ 13:00)
            // type1: eff(10:00) < exp(13:00) AND exp(12:00) > eff(11:00) -> 포함
            // type2: eff(11:00) < exp(13:00) AND exp(13:00) > eff(11:00) -> 포함
            // type3: eff(13:00) < exp(13:00) -> false -> 미포함
            // type4: exp(11:00) > eff(11:00) -> false -> 미포함
            repository.findByRange(workspace, queryStart, queryEnd)
                .collectSortedList(compareBy({ it.id }, { it.version }))
                .let(StepVerifier::create)
                .expectNext(listOf(expectedType1, expectedType2).sortedBy { it.id })
                .verifyComplete()
        }

        should("주어진 시간 범위가 특정 타입을 완전히 포함하는 경우 해당 타입을 찾아야 한다 (09:00 ~ 15:00)") {
            // Given: Query range covers all test types
            val queryStart = Instant.parse("2025-01-01T09:00:00Z")
            val queryEnd = Instant.parse("2025-01-01T15:00:00Z")

            // When & Then:
            // Expected: type1, type2, type3, type4 (모두 범위 내에 포함됨)
            repository.findByRange(workspace, queryStart, queryEnd)
                .collectSortedList(compareBy({ it.id }, { it.version }))
                .let(StepVerifier::create)
                .expectNext(listOf(expectedType1, expectedType2, expectedType3, expectedType4).sortedBy { it.id })
                .verifyComplete()
        }

        should("주어진 시간 범위와 겹치는 타입이 없으면 빈 Flux를 반환해야 한다 (15:00 ~ 16:00)") {
            // Given: Query range after all test types
            val queryStart = Instant.parse("2025-01-01T15:00:00Z")
            val queryEnd = Instant.parse("2025-01-01T16:00:00Z")

            // When & Then:
            repository.findByRange(workspace, queryStart, queryEnd)
                .let(StepVerifier::create)
                .verifyComplete()
        }

        should("잘못된 워크스페이스 ID로는 타입을 찾지 못해야 한다") {
            // Given: Query range and a non-existent workspace
            val queryStart = t1
            val queryEnd = t5
            val nonExistentWorkspace = UUID.randomUUID()

            // When & Then:
            repository.findByRange(nonExistentWorkspace, queryStart, queryEnd)
                .let(StepVerifier::create)
                .verifyComplete()
        }

        should("last=false인 타입은 결과에 포함하지 않아야 한다 (10:00 ~ 11:00)") {
            // Given: Query range t1 to t2 (covers type_old which is last=false)
            val queryStart = t1
            val queryEnd = t2

            // When & Then:
            // Expected: type1 (10:00 ~ 12:00), type4 (10:00 ~ 11:00)
            // type_old (10:00 ~ 11:00, last=false) should be excluded by the query itself.
            repository.findByRange(workspace, queryStart, queryEnd)
                .collectSortedList(compareBy({ it.id }, { it.version }))
                .let(StepVerifier::create)
                .expectNext(listOf(expectedType1, expectedType4).sortedBy { it.id }) // type_old 제외 기대
                .verifyComplete()
        }
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