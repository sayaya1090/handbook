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
    implementation(project(":activity"))
    implementation(libs.spring.gateway)
    implementation(libs.spring.actuator)
    implementation(libs.prometheus)
    implementation(libs.kotlin.jackson)
    implementation(libs.spring.log4j2)
    implementation(libs.spring.kubernetes.client)
    testImplementation(libs.bundles.test.api)
    testImplementation(libs.kubernetes.mock)
    testImplementation(libs.kubernetes.mockserver)
    testImplementation("junit:junit:4.13.2")
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
                disabled = false
                bound {
                    minValue = 80
                }
            }
        }
    }
}
