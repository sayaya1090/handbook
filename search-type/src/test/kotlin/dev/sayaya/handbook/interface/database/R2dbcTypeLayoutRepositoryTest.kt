package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Layout
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
internal class R2dbcTypeLayoutRepositoryTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    val repository = R2dbcTypeLayoutRepository(template)
    // 테스트에 사용할 고정된 Workspace UUID
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    val otherWorkspace = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee") // 다른 워크스페이스 테스트용
    val userId = UUID.fromString("93951bc3-be1e-4fc8-865f-d6376ac3e87b") // created_by 용

    val t1 = Instant.parse("2025-01-01T10:00:00Z")
    val t2 = Instant.parse("2025-01-01T11:00:00Z")
    val t3 = Instant.parse("2025-01-01T12:00:00Z")
    val t4 = Instant.parse("2025-01-01T13:00:00Z")
    val t5 = Instant.parse("2025-01-01T14:00:00Z")

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

        // 테스트용 Type 데이터 삽입 (Scenario 1 - 개별 INSERT 문으로 분리)
        val insertScenario1Type1 = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description) VALUES 
            (:workspace, :typeId, NOW(), 'type1', 'v1', :t1, :t3, true, :userId, false, '') -- 10:00 ~ 12:00
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", UUID.randomUUID())
            .bind("t1", t1)
            .bind("t3", t3)
            .fetch().rowsUpdated()

        val insertScenario1Type2 = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description) VALUES 
            (:workspace, :typeId, NOW(), 'type2', 'v1', :t2, :t4, true, :userId, false, '') -- 11:00 ~ 13:00
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", UUID.randomUUID())
            .bind("t2", t2)
            .bind("t4", t4)
            .fetch().rowsUpdated()

        val insertScenario1Type3 = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description) VALUES 
            (:workspace, :typeId, NOW(), 'type3', 'v1', :t4, :t5, true, :userId, false, '') -- 13:00 ~ 14:00
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", UUID.randomUUID())
            .bind("t4", t4)
            .bind("t5", t5)
            .fetch().rowsUpdated()

        val insertScenario2Type = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description) VALUES 
            (:otherWorkspace, :typeId, NOW(), 'type_other', 'v1', :t1, :t2, true, :userId, false, '');
        """.trimIndent())
            .bind("otherWorkspace", otherWorkspace)
            .bind("userId", userId)
            .bind("typeId", UUID.randomUUID())
            .bind("t1", t1)
            .bind("t2", t2)
            .fetch().rowsUpdated()

        val insertScenario3Type = databaseClient.sql("""
            INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description) VALUES 
            (:workspace, :typeId, NOW(), 'type_old', 'v0', :t1, :t2, false, :userId, false, '');
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", UUID.randomUUID())
            .bind("t1", t1)
            .bind("t2", t2)
            .fetch().rowsUpdated()

        val insertScenario4Type = databaseClient.sql("""
             INSERT INTO public.type (workspace, id, created_at, name, version, effective_at, expire_at, last, created_by, primitive, description) VALUES
             (:workspace, :typeId, NOW(), 'type4_single_interval', 'v1', :t1, :t2, true, :userId, false, '');
        """.trimIndent())
            .bind("workspace", workspace)
            .bind("userId", userId)
            .bind("typeId", UUID.randomUUID())
            .bind("t1", t1)
            .bind("t2", t2)
            .fetch().rowsUpdated()

        // 모든 INSERT 문을 순차적으로 실행하고 최종 완료를 검증
        insertUser
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
    should("지정된 워크스페이스의 유효한 타입들로부터 Layout 시퀀스를 생성해야 한다") {
        // Given: beforeSpec에서 데이터 삽입됨
        //        Unique timestamps expected: t1, t2, t3, t4, t5 (10:00, 11:00, 12:00, 13:00, 14:00)
        //        zipWithNext pairs expected: (t1,t2), (t2,t3), (t3,t4), (t4,t5)

        // Expected Layouts in order
        val expectedLayout1 = Layout(workspace, t1, t2) // 10:00 -> 11:00
        val expectedLayout2 = Layout(workspace, t2, t3) // 11:00 -> 12:00
        val expectedLayout3 = Layout(workspace, t3, t4) // 12:00 -> 13:00
        val expectedLayout4 = Layout(workspace, t4, t5) // 13:00 -> 14:00

        // When & Then: findAll 호출, 반환된 Layout Flux 검증
        repository.findAll(workspace)
            .let(StepVerifier::create)
            .expectNext(expectedLayout1) // 순서대로 기대값 확인
            .expectNext(expectedLayout2)
            .expectNext(expectedLayout3)
            .expectNext(expectedLayout4)
            .verifyComplete() // 더 이상 데이터가 없음을 확인
    }

    should("워크스페이스에 해당하는 데이터가 없으면 빈 Flux를 반환해야 한다") {
        // Given: 존재하지 않는 workspace ID
        val nonExistentWorkspace = UUID.randomUUID()

        // When & Then: findAll 호출, 빈 결과 검증
        repository.findAll(nonExistentWorkspace)
            .let(StepVerifier::create)
            .verifyComplete() // 아무런 데이터도 emit되지 않아야 함
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