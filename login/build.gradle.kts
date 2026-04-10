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
    implementation(project(":authentication"))
    implementation(project(":activity"))
    implementation(libs.bundles.spring.client)
    implementation(libs.bundles.kotlin.webflux)
    implementation(libs.spring.security)
    implementation(libs.bundles.r2dbc.postgres)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    testImplementation(libs.bundles.test.api)
}
tasks.jar { enabled = false }
