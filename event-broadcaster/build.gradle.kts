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
    //implementation(project(":domain"))
    //implementation(project(":authentication"))
    implementation(libs.bundles.spring.client)
    implementation(libs.bundles.kotlin.webflux)
    //implementation(libs.bundles.r2dbc.postgres)
    // implementation(libs.spring.kafka)
    implementation("com.github.f4b6a3:ulid-creator:5.2.3")
    //testImplementation(testFixtures(project(":testcontainer")))
    //testImplementation(testFixtures(project(":entity")))
    testImplementation(libs.testcontainers.postgresql)
}
configurations { all { exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging") } }
dependencyManagement { imports { mavenBom(libs.spring.cloud.bom.get().toString()) } }
tasks {
    jar {
        enabled = false
    }
    test {
        useJUnitPlatform()
    }
}
jib {
    container {
        environment = mapOf(
            "LANG" to "C.UTF-8",
            "TZ" to "Asia/Seoul",
        )
    }
}
kover {
    reports {
        verify {
            rule {
                disabled = true
                bound {
                    minValue = 80
                }
            }
        }
    }
}