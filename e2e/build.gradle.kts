plugins {
    kotlin("jvm")
    id("com.adarshr.test-logger")
}
dependencies {
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions.core)
    testImplementation("com.microsoft.playwright:playwright:1.52.0")
}
// E2E 테스트는 라이브 서버가 필요하므로 기본 test에서 제외
tasks.test {
    onlyIf { System.getenv("E2E") != null }
}
// 명시적 E2E 실행용 태스크
tasks.register<Test>("e2eTest") {
    useJUnitPlatform()
    group = "verification"
    description = "서버 실행 상태에서 E2E 테스트 수행 (APP_BASE_URL 환경변수로 URL 지정 가능)"
}
