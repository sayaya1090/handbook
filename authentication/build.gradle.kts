plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}
dependencies {
    implementation(libs.spring.webflux)
    implementation(libs.spring.security)
    implementation(libs.kotlin.jackson)
    implementation(libs.r2dbc)
    api(libs.bouncycastle.bcprov)
    api(libs.jjwt.api)
    runtimeOnly(libs.bundles.jjwt.runtime)
    testImplementation(libs.bundles.test.api)
}
tasks.test {
    useJUnitPlatform()
}
