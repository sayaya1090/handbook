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
    implementation(project(":schema"))
    implementation(project(":event"))
    implementation(project(":authentication"))
    implementation("com.google.jsinterop:base:1.0.0")
    implementation(libs.bundles.spring.client)
    implementation(libs.bundles.kotlin.webflux)
    implementation(libs.bundles.r2dbc.postgres)
    implementation(libs.spring.kafka)
    implementation(libs.springdoc.webflux)
    testImplementation(libs.bundles.test.api)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
}
tasks.jar { enabled = false }
