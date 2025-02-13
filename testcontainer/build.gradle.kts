plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java-test-fixtures")
}
dependencies {
    testFixturesApi(libs.bundles.test.api)
    testFixturesApi(libs.bundles.test.containers)

    testFixturesImplementation(libs.testcontainers.postgresql)
}