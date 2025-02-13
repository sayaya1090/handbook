package dev.sayaya.handbook

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ExpectSpec
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.core.io.ClassPathResource
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
    expect("트리거 생성") {
        val resource = ClassPathResource("resources/create_request_view.sql")
        val path = resource.file.toPath()
        String(Files.readAllBytes(path)).trimIndent().let(em::createNativeQuery).executeUpdate()
    }
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