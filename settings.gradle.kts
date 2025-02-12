rootProject.name = "handbook"
pluginManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sayaya1090/maven")
            credentials {
                username = if(settings.extra.has("github_username")) settings.extra["github_username"] as String else System.getenv("GITHUB_USERNAME")
                password = if(settings.extra.has("github_password")) settings.extra["github_password"] as String else System.getenv("GITHUB_TOKEN")
            }
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("reflect", "org.jetbrains.kotlin", "kotlin-reflect").withoutVersion()
            library("stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").withoutVersion()
            bundle("kotlin", listOf("reflect", "stdlib"))

            library("spring-webflux", "org.springframework.boot", "spring-boot-starter-webflux").withoutVersion()
            library("kotlin-reactor", "io.projectreactor.kotlin", "reactor-kotlin-extensions").withoutVersion()
            library("kotlin-coroutines-reactor", "org.jetbrains.kotlinx", "kotlinx-coroutines-reactor").withoutVersion()
            library("kotlin-jackson", "com.fasterxml.jackson.module", "jackson-module-kotlin").withoutVersion()
            library("spring-actuator", "org.springframework.boot", "spring-boot-starter-actuator").withoutVersion()
            bundle("kotlin-webflux", listOf("spring-webflux", "kotlin-reactor", "kotlin-coroutines-reactor", "kotlin-jackson", "spring-actuator"))

            library("spring-gateway", "org.springframework.cloud", "spring-cloud-starter-gateway").withoutVersion()
            library("spring-discovery", "org.springframework.cloud", "spring-cloud-starter-zookeeper-discovery").withoutVersion()

            library("spring-cloud-bom", "org.springframework.cloud", "spring-cloud-dependencies").version { require("2024.0.0") }

            library("reactor-test", "io.projectreactor", "reactor-test").withoutVersion()
            library("kotest-runner", "io.kotest", "kotest-runner-junit5").version { require("5.9.1") }
            library("mockk", "io.mockk", "mockk").version { require("1.13.16") }
            library("kotest-extensions-spring", "io.kotest.extensions", "kotest-extensions-spring").version { require("1.3.0") }
            library("spring-boot-test", "org.springframework.boot", "spring-boot-starter-test").withoutVersion()
            bundle("test-api", listOf("reactor-test", "kotest-runner", "mockk", "kotest-extensions-spring", "spring-boot-test"))
            library("testcontainers-junit", "org.testcontainers", "junit-jupiter").withoutVersion()
            library("testcontainers-postgresql", "org.testcontainers", "postgresql").withoutVersion()
            library("kotest-extensions-testcontainers", "io.kotest.extensions", "kotest-extensions-testcontainers").version { require("2.0.2") }
            bundle("test-containers", listOf("testcontainers-junit", "kotest-extensions-testcontainers"))
        }
    }
}
include("domain")
include("entity")
include("persist")
