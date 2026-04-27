plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.adarshr.test-logger")
    id("org.jetbrains.kotlinx.kover")
}
dependencies {
    implementation(libs.kotlin.jackson)
    testImplementation(libs.bundles.test.api)
}

tasks.named("bootJar") {
    enabled = false
}
tasks.named("jar") {
    enabled = true
}
