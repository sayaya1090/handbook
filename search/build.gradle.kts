plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.adarshr.test-logger")
    id("org.jetbrains.kotlinx.kover")
}
dependencies {
    implementation(libs.spring.webflux)
    implementation(libs.kotlin.jackson)
    implementation(libs.r2dbc)
    testImplementation(libs.bundles.test.api)
}
tasks {
    test {
        useJUnitPlatform()
    }
}