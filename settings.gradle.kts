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
include("entity")
