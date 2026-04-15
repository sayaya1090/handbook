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
    // spring-boot-starter-data-r2dbc 는 SecurityContextUuidAuditorConfig 컴파일용으로만 필요하다.
    // 실행 시에는 @ConditionalOnClass / @ConditionalOnBean 로 r2dbc 가 classpath 에 있을 때만
    // auditor 가 활성화되도록 설계돼 있음. 전이되면 DB 를 쓰지 않는 소비자(event-broadcaster 등)가
    // R2dbcAutoConfiguration 이 트리거되어 URL 누락으로 부팅 실패한다.
    compileOnly(libs.r2dbc)
    api(libs.bouncycastle.bcprov)
    api(libs.jjwt.api)
    runtimeOnly(libs.bundles.jjwt.runtime)
    testImplementation(libs.bundles.test.api)
    testImplementation(libs.r2dbc)
}

