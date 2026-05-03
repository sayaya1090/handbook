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
    implementation(project(":event"))
    implementation(project(":authentication"))
    implementation(libs.bundles.spring.client)
    implementation(libs.bundles.kotlin.webflux)
    implementation(libs.spring.kafka)
    implementation(libs.springdoc.webflux)
    implementation("com.google.jsinterop:base:1.0.0")
    testImplementation(libs.bundles.test.api)
}
tasks.jar { enabled = false }
