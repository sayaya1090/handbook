package dev.sayaya.handbook

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ExpectSpec
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*

@DataJpaTest(properties = [
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=update"
])
internal class ExportSchema(@PersistenceContext private val em: EntityManager): ExpectSpec({
    expect("PostgreSQL 스키마 덤프") {
        val schemaSql = Database.dump()
        val schemaPath = Path.of("src/testFixtures/resources/schema.sql")
        Files.createDirectories(schemaPath.parent)
        Files.writeString(schemaPath, schemaSql, CREATE, TRUNCATE_EXISTING)
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

/*
@Component
class SQL(
    @PersistenceContext
    private val entityManager: EntityManager,
) {
    @Transactional
    @EventListener(ApplicationReadyEvent::class)
    fun createView() {
        val sqlFiles = listOf(
            "resources/create_request_view.sql",
            "resources/create_work_view.sql",
            "resources/create_worklist_view.sql",
            "resources/create_serial_trigger.sql",
            "resources/create_index_trigger.sql",
            "resources/create_sequencing_batch_trigger.sql",
        )
        sqlFiles.forEach { sqlFile ->
            val sql = readSqlFromFile(sqlFile)
            entityManager.createNativeQuery(sql.trimIndent()).executeUpdate()
        }
    }
    private fun readSqlFromFile(filePath: String): String {
        val resource = ClassPathResource(filePath)
        val path = resource.file.toPath()
        return String(Files.readAllBytes(path))
    }
}
 */