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
    implementation(libs.spring.security)
    implementation(libs.kotlin.jackson)
    compileOnly(libs.r2dbc)
    api(libs.bouncycastle.bcprov)
    api(libs.jjwt.api)
    runtimeOnly(libs.bundles.jjwt.runtime)
    testImplementation(libs.bundles.test.api)
    testImplementation(libs.r2dbc)
}

tasks.named("bootJar") {
    enabled = false
}
tasks.named("jar") {
    enabled = true
}
