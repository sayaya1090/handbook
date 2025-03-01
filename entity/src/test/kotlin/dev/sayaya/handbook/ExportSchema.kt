package dev.sayaya.handbook

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ExpectSpec
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.core.io.ClassPathResource
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING

@DataJpaTest(properties = [
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=update"
])
internal class ExportSchema(
    private val tx: PlatformTransactionManager,
    @PersistenceContext private val em: EntityManager
): ExpectSpec({
    tx.transactional {
        ClassPathResource("createTriggers.sql").let { em.execute(it) }          // 트리거 생성
        ClassPathResource("createMaterializedView.sql").let { em.execute(it) }  // MV 생성
    }
    expect("PostgreSQL 스키마 덤프") {
        val schemaSql = database.dump()
        val schemaPath = Path.of("src/testFixtures/resources/schema.sql")
        Files.createDirectories(schemaPath.parent)
        Files.writeString(schemaPath, schemaSql, CREATE, TRUNCATE_EXISTING)
    }
    expect("샘플 데이터 삽입 테스트") {
        tx.transactional {
            ClassPathResource("sampleData.sql").let { em.execute(it) }
        }
    }
}) {
    companion object {
        val database = Database()
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            database.registerDynamicProperties(registry)
        }
        fun PlatformTransactionManager.transactional(action: () -> Unit) {
            val transactionDefinition = DefaultTransactionDefinition()
            val status = getTransaction(transactionDefinition)
            try {
                action()
                commit(status)
            } catch (e: JpaSystemException) {
                if (!status.isCompleted) rollback(status)
                throw unwrapException(e)
            } catch (e: Exception) {
                if (!status.isCompleted) rollback(status)
                throw e
            }
        }
        private fun unwrapException(exception: JpaSystemException): Throwable {
            var cause: Throwable = exception
            while (cause.cause != null && cause != cause.cause) { // 자신과 부모가 동일하지 않을 때
                cause = cause.cause!!
            }
            return cause
        }
        private fun EntityManager.execute(resource: ClassPathResource) {
            resource.inputStream.bufferedReader().use { it.readText() }.trimIndent().let { execute(it) }
        }
        private fun EntityManager.execute(sql: String) {
            sql.also(::println)
                .let(::createNativeQuery)
                .executeUpdate()
        }
    }
}