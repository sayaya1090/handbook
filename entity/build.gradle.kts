plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `java-test-fixtures`
}
dependencies {
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation(libs.bundles.test.api)
    testImplementation(libs.bundles.test.containers)
    testImplementation(libs.testcontainers.postgresql)
}
tasks {
    test {
        useJUnitPlatform()
    }
}