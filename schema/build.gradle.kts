plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.adarshr.test-logger")
    id("org.jetbrains.kotlinx.kover")
}
dependencies {
    implementation(libs.bundles.kotlin.webflux)
    testImplementation(libs.bundles.test.api)
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")
}
