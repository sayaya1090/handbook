package dev.sayaya.handbook.testcontainer

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer

internal class Database {
    companion object {
        private val postgres = PostgreSQLContainer("postgres:latest")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("password")
        init {
            postgres.start()
        }
        fun dump(): String {
            val process = ProcessBuilder(
                "docker", "exec", postgres.containerId,
                "pg_dump", "-h", "localhost", "-U", postgres.username, "--no-owner", postgres.databaseName
            ).redirectErrorStream(true).start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            return output
        }
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}" }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.test.database.default-rollback") { false }
        }
    }
}