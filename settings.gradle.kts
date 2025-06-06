rootProject.name = "handbook"
pluginManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sayaya1090/maven")
            credentials {
                username =
                    if (settings.extra.has("github_username")) settings.extra["github_username"] as String else System.getenv(
                        "GITHUB_USERNAME"
                    )
                password =
                    if (settings.extra.has("github_password")) settings.extra["github_password"] as String else System.getenv(
                        "GITHUB_TOKEN"
                    )
            }
        }
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
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
            library("prometheus", "io.micrometer", "micrometer-registry-prometheus").withoutVersion()
            bundle(
                "kotlin-webflux",
                listOf(
                    "spring-webflux",
                    "kotlin-reactor",
                    "kotlin-coroutines-reactor",
                    "kotlin-jackson",
                    "spring-actuator",
                    "prometheus"
                )
            )

            library("spring-gateway", "org.springframework.cloud", "spring-cloud-starter-gateway").withoutVersion()
            library(
                "spring-discovery",
                "org.springframework.cloud",
                "spring-cloud-starter-zookeeper-discovery"
            ).withoutVersion()

            library(
                "spring-cloud-bom",
                "org.springframework.cloud",
                "spring-cloud-dependencies"
            ).version { require("2024.0.1") }
            library("spring-log4j2", "org.springframework.boot", "spring-boot-starter-log4j2").withoutVersion()
            library("spring-security", "org.springframework.boot", "spring-boot-starter-security").withoutVersion()
            library(
                "spring-kubernetes-client",
                "org.springframework.cloud",
                "spring-cloud-starter-kubernetes-fabric8"
            ).withoutVersion()
            bundle("spring-client", listOf("spring-log4j2", "spring-security"))

            library("spring-kafka", "org.springframework.cloud", "spring-cloud-starter-stream-kafka").withoutVersion()

            library("r2dbc", "org.springframework.boot", "spring-boot-starter-data-r2dbc").withoutVersion()
            library("r2dbc-postgres", "org.postgresql", "r2dbc-postgresql").withoutVersion()
            library("r2dbc-pool", "io.r2dbc", "r2dbc-pool").withoutVersion()
            bundle("r2dbc-postgres", listOf("r2dbc", "r2dbc-postgres", "r2dbc-pool"))

            library("jjwt-api", "io.jsonwebtoken", "jjwt-api").version { require("0.12.6") }
            library("jjwt-impl", "io.jsonwebtoken", "jjwt-impl").version { require("0.12.6") }
            library("jjwt-jackson", "io.jsonwebtoken", "jjwt-jackson").version { require("0.12.6") }
            library("bouncycastle-bcprov", "org.bouncycastle", "bcprov-jdk18on").version { require("1.80") }
            bundle("jjwt-runtime", listOf("jjwt-impl", "jjwt-jackson"))

            library("reactor-test", "io.projectreactor", "reactor-test").withoutVersion()
            library("kotest-runner", "io.kotest", "kotest-runner-junit5").version { require("5.9.1") }
            library("mockk", "io.mockk", "mockk").version { require("1.14.2") }
            library(
                "kotest-extensions-spring",
                "io.kotest.extensions",
                "kotest-extensions-spring"
            ).version { require("1.3.0") }
            library("spring-boot-test", "org.springframework.boot", "spring-boot-starter-test").withoutVersion()
            library("spring-security-test", "org.springframework.security", "spring-security-test").withoutVersion()
            library("kubernetes-mock", "io.fabric8", "kubernetes-server-mock").version { require("7.1.0") }
            library("kubernetes-mockserver", "io.fabric8", "mockwebserver").version { require("7.1.0") }
            bundle(
                "test-api",
                listOf(
                    "reactor-test",
                    "kotest-runner",
                    "mockk",
                    "kotest-extensions-spring",
                    "spring-boot-test",
                    "spring-security-test"
                )
            )
            library("testcontainers-junit", "org.testcontainers", "junit-jupiter").withoutVersion()
            library("testcontainers-postgresql", "org.testcontainers", "postgresql").withoutVersion()
            library(
                "kotest-extensions-testcontainers",
                "io.kotest.extensions",
                "kotest-extensions-testcontainers"
            ).version { require("2.0.2") }
            library("testcontainers-kafka", "org.testcontainers", "kafka").version { require("1.21.1") }
            bundle("test-containers", listOf("testcontainers-junit", "kotest-extensions-testcontainers"))

            library("elemento-core", "org.jboss.elemento", "elemento-core").version { require("1.7.0") }
            library("elemental2-svg", "com.google.elemental2", "elemental2-svg").version { require("1.2.3") }
            library("gwt-user", "org.gwtproject", "gwt-user").version { require("2.12.2") }
            library("gwt-dev", "org.gwtproject", "gwt-dev").version { require("2.12.2") }
            library("sayaya-ui", "dev.sayaya", "ui").version { require("material3-2.2.1") }
            library("sayaya-rx", "dev.sayaya", "rx").version { require("2.1.2") }
            library("lombok", "org.projectlombok", "lombok").version { require("1.18.38") }
            bundle(
                "sayaya-web",
                listOf(
                    "elemento-core",
                    "elemental2-svg",
                    "gwt-user",
                    "dagger-gwt",
                    "dagger-compiler",
                    "sayaya-ui",
                    "sayaya-rx",
                    "lombok"
                )
            )

            bundle("gwt", listOf("elemento-core", "elemental2-svg", "gwt-user"))

            library("dagger-gwt", "com.google.dagger", "dagger-gwt").version { require("2.56.2") }
            library("dagger-compiler", "com.google.dagger", "dagger-compiler").version { require("2.56.2") }
            library("junit5", "org.junit.jupiter", "junit-jupiter").version { require("5.12.2") }
            library("selenium", "org.seleniumhq.selenium", "selenium-java").version { require("4.32.0") }
            library(
                "selenium-driver",
                "org.seleniumhq.selenium",
                "selenium-chrome-driver"
            ).version { require("4.32.0") }
            bundle("test-web", listOf("kotest-runner", "selenium", "selenium-driver", "mockk", "junit5"))
        }
    }
}
include("domain")
include("entity")
include("authentication")
include("activity")
include("ui-asset")
include("shell-ui")
include("gateway")
include("testcontainer")
include("search")
include("search-workspace")
include("persist-workspace")
include("workspace-ui")
include("search-type")
include("persist-type")
include("type-ui")
include("login")
include("login-ui")
include("search-user")
include("persist-user")
include("user-ui")
include("persist-document")
include("search-document")
include("document-ui")
include("validator")
include("event-broadcaster")
