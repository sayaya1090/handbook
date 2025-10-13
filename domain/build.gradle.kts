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
    implementation("org.springframework.data:spring-data-commons")  // Page 클래스
    testImplementation(libs.bundles.test.api)
}
tasks.test {
    useJUnitPlatform()
}
kover.reports.verify.rule {
    disabled = false
    bound {
        minValue = 80
    }
}