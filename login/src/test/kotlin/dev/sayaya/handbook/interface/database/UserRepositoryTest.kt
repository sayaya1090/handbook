package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.State
import dev.sayaya.handbook.interfaces.authentication.R2dbcAuditorConfig
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.*

@DataR2dbcTest
@Import(R2dbcConfig::class, R2dbcAuditorConfig::class)
@Testcontainers
internal class UserRepositoryTest(
    private val repo: R2dbcUserRepository,
    private val databaseClient: DatabaseClient
): BehaviorSpec({
    beforeSpec {
        databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name, provider, account) VALUES ('93951bc3-be1e-4fc8-865f-d6376ac3e87b', '2025-01-01', '2025-01-01', null, 'system', 'handbook', 'system');
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    val now = LocalDateTime.now()
    Given("DB 초기화 및 연결 완료"){
        repo shouldNotBe null
        When("존재하지 않는 사용자를 조회하면") {
            val user = repo.findById(UUID.randomUUID()).block()
            Then("null이 반환된다") {
                user shouldBe null
            }
        }
        When("새로운 사용자를 저장하면") {
            val entity = R2dbcUserEntity(id=UUID.randomUUID(), provider="any", account="any", name="any")
            val saved = repo.save(entity).block()
            Then("생성시각, 변경시각이 업데이트 되어 저장된 객체가 반환된다") {
                saved shouldNotBe null
                saved!!.id shouldBeEqual entity.id
                saved.createDateTime shouldBeGreaterThanOrEqualTo now
                saved.lastModifyDateTime shouldBeGreaterThanOrEqualTo now
            }
            And("저장한 사용자를 조회하면") {
                val read = repo.findById(entity.id).block()
                Then("동일한 객체가 반환된다") {
                    read shouldNotBe null
                    read!!.id shouldBeEqual entity.id
                    read.createDateTime shouldBeGreaterThanOrEqualTo now
                    read.lastModifyDateTime shouldBeGreaterThanOrEqualTo now
                }
            }
        }
    }
    Given("특정 ID의 사용자가 존재하고") {
        repo shouldNotBe null
        val id = UUID.fromString("93951bc3-be1e-4fc8-865f-d6376ac3e87b")
        When("DB에 존재하는 사용자를 요청하면") {
            val user = repo.findById(id).block()
            Then("User 객체를 반환한다.") {
                user shouldNotBe null
                user!!.id shouldBe id
            }
        }
        When("ID가 동일한 사용자를 신규로 저장 시도하면") {
            val entity = R2dbcUserEntity(id=id, provider="any", account="any", name="any")
            val request = repo.save(entity)
            Then("DataIntegrityViolationException 예외가 발생한다") {
                shouldThrow<DataIntegrityViolationException> {
                    request.block()
                }
            }
        }
        When("ID가 동일한 사용자를 DB에서 읽어온 다음 변경, 저장하면") {
            val entity = repo.findById(id).block()!!
            val saved = entity.apply { state= State.INACTIVATED }.let(repo::save).block()
            Then("생성시각은 변경되지 않고 변경시각은 업데이트 되어 저장된 객체가 반환된다") {
                saved shouldNotBe null
                saved!!.id shouldBeEqual entity.id
                saved.state shouldBeEqual entity.state
                saved.createDateTime shouldBeLessThanOrEqualTo now
                saved.lastModifyDateTime shouldBeGreaterThanOrEqualTo now
            }
            And("저장한 사용자를 조회하면") {
                val read = repo.findById(entity.id).block()
                Then("변경된 결과가 반환된다") {
                    read shouldNotBe null
                    read!!.id shouldBeEqual entity.id
                    read.state shouldBeEqual entity.state
                    read.createDateTime shouldBeLessThanOrEqualTo now
                    read.lastModifyDateTime shouldBeGreaterThanOrEqualTo now
                }
            }
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