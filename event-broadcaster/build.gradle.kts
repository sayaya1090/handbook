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
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(libs.springdoc.webflux)
    testImplementation(libs.bundles.test.api)
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
tasks.jar { enabled = false }
