plugins {
    kotlin("jvm")
    id("com.adarshr.test-logger")
}
dependencies {
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions.core)
    testImplementation("com.microsoft.playwright:playwright:1.52.0")
}
