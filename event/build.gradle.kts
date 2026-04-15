plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.adarshr.test-logger")
    id("org.jetbrains.kotlinx.kover")
}
dependencies {
    api(project(":document"))
    api(project(":schema"))
    implementation(libs.bundles.kotlin.webflux)
    testImplementation(libs.bundles.test.api)
}
