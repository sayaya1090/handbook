plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib")
    id("com.adarshr.test-logger")
    id("org.jetbrains.kotlinx.kover")
}
dependencies {
    implementation(project(":activity"))
    implementation(project(":persist-document"))
    implementation(project(":search-document"))
    implementation(libs.bundles.spring.client)
    implementation(libs.bundles.kotlin.webflux)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(libs.springdoc.webflux)
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    testImplementation(libs.bundles.test.api)
}
tasks.jar { enabled = false }
