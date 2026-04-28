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
    implementation(project(":workspace"))
    implementation(project(":activity"))
    implementation(project(":authentication"))
    implementation(libs.spring.security)
    implementation(libs.bundles.spring.client)
    implementation(libs.bundles.kotlin.webflux)
    // authentication 의 GlobalExceptionHandler 가 DuplicateKeyException (spring-tx, spring-data-commons) 을
    // 참조하기 때문에 reflection 시점에 관련 클래스가 classpath 에 필요하다.
    // DB 를 직접 사용하지 않더라도 r2dbc 번들을 포함해 spring-data-commons 를 끌어온다.
    implementation(libs.bundles.r2dbc.postgres)
    implementation(libs.springdoc.webflux)
    testImplementation(libs.bundles.test.api)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
}
tasks.jar { enabled = false }
