plugins {
    java
    kotlin("jvm")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation("com.google.jsinterop:jsinterop-annotations:2.1.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions.core)
}
tasks {
    jar {
        enabled = true
        from(sourceSets.main.get().allSource)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
    test { useJUnitPlatform() }
}
