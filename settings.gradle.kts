rootProject.name = "handbook"
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Versions
            // https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies
            version("spring-cloud", "2025.1.1")
            // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
            version("jjwt", "0.13.0")
            // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on
            version("bouncycastle", "1.83")
            // https://mvnrepository.com/artifact/io.kotest/kotest-runner-junit5-jvm
            version("kotest", "6.1.3")
            // https://mvnrepository.com/artifact/io.mockk/mockk
            version("mockk", "1.14.9")
            // https://mvnrepository.com/artifact/io.fabric8/kubernetes-server-mock
            version("kubernetes", "7.5.2")
            // https://mvnrepository.com/artifact/org.testcontainers/testcontainers
            version("testcontainers", "2.0.3")
            // https://mvnrepository.com/artifact/org.jboss.elemento/elemento-core
            version("elemento", "2.4.9")
            // https://mvnrepository.com/artifact/dev.sayaya/ui
            version("sayaya-ui", "2.4.1.3")
            // https://mvnrepository.com/artifact/dev.sayaya/rx
            version("sayaya-rx", "2.2.3")
            // https://mvnrepository.com/artifact/org.projectlombok/lombok
            version("lombok", "1.18.42")
            // https://mvnrepository.com/artifact/com.google.dagger/dagger
            version("dagger", "2.59.1")
            // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter
            version("junit5", "6.0.3")
            // https://mvnrepository.com/artifact/dev.sayaya/gwt-test
            version("sayaya-test", "2.2.9.5")

            // Kotlin
            library("reflect", "org.jetbrains.kotlin", "kotlin-reflect").withoutVersion()
            library("stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").withoutVersion()
            bundle("kotlin", listOf("reflect", "stdlib"))

            // Spring WebFlux
            library("spring-webflux", "org.springframework.boot", "spring-boot-starter-webflux").withoutVersion()
            library("kotlin-reactor", "io.projectreactor.kotlin", "reactor-kotlin-extensions").withoutVersion()
            library("kotlin-coroutines-reactor", "org.jetbrains.kotlinx", "kotlinx-coroutines-reactor").withoutVersion()
            library("kotlin-jackson", "tools.jackson.module", "jackson-module-kotlin").withoutVersion()
            library("spring-actuator", "org.springframework.boot", "spring-boot-starter-actuator").withoutVersion()
            library("prometheus", "io.micrometer", "micrometer-registry-prometheus").withoutVersion()
            bundle("kotlin-webflux", listOf("spring-webflux", "kotlin-reactor", "kotlin-coroutines-reactor", "kotlin-jackson", "spring-actuator", "prometheus"))

            // Spring Cloud
            library("spring-gateway", "org.springframework.cloud", "spring-cloud-starter-gateway-server-webflux").withoutVersion()
            library("spring-discovery", "org.springframework.cloud", "spring-cloud-starter-zookeeper-discovery").withoutVersion()
            library("spring-cloud-bom", "org.springframework.cloud", "spring-cloud-dependencies").versionRef("spring-cloud")
            library("spring-log4j2", "org.springframework.boot", "spring-boot-starter-log4j2").withoutVersion()
            library("spring-security", "org.springframework.boot", "spring-boot-starter-security").withoutVersion()
            library(
                "spring-kubernetes-client",
                "org.springframework.cloud",
                "spring-cloud-starter-kubernetes-fabric8"
            ).withoutVersion()
            bundle("spring-client", listOf("spring-log4j2", /*"spring-security"*/))

            // OpenAPI
            version("springdoc", "2.8.9")
            library("springdoc-webflux", "org.springdoc", "springdoc-openapi-starter-webflux-ui").versionRef("springdoc")

            // Kafka
            library("spring-kafka", "org.springframework.cloud", "spring-cloud-starter-stream-kafka").withoutVersion()

            // R2DBC
            library("r2dbc", "org.springframework.boot", "spring-boot-starter-data-r2dbc").withoutVersion()
            library("r2dbc-postgres", "org.postgresql", "r2dbc-postgresql").withoutVersion()
            library("r2dbc-pool", "io.r2dbc", "r2dbc-pool").withoutVersion()
            bundle("r2dbc-postgres", listOf("r2dbc", "r2dbc-postgres", "r2dbc-pool"))

            // JWT
            library("jjwt-api", "io.jsonwebtoken", "jjwt-api").versionRef("jjwt")
            library("jjwt-impl", "io.jsonwebtoken", "jjwt-impl").versionRef("jjwt")
            library("jjwt-jackson", "io.jsonwebtoken", "jjwt-jackson").versionRef("jjwt")
            library("bouncycastle-bcprov", "org.bouncycastle", "bcprov-jdk18on").versionRef("bouncycastle")
            bundle("jjwt-runtime", listOf("jjwt-impl", "jjwt-jackson"))

            // Elasticsearch
            version("elasticsearch", "9.3.3")
            library("spring-elasticsearch", "org.springframework.boot", "spring-boot-starter-data-elasticsearch").withoutVersion()

            // Test - Core
            library("reactor-test", "io.projectreactor", "reactor-test").withoutVersion()
            library("kotest-runner", "io.kotest", "kotest-runner-junit5").versionRef("kotest")
            library("kotest-assertions-core", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-framework-engine", "io.kotest", "kotest-framework-engine").versionRef("kotest")
            library("kotest-extensions-spring", "io.kotest", "kotest-extensions-spring").versionRef("kotest")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            library("spring-boot-test", "org.springframework.boot", "spring-boot-starter-test").withoutVersion()
            library("spring-boot-webflux-test", "org.springframework.boot", "spring-boot-webflux-test").withoutVersion()
            library("spring-boot-webtestclient", "org.springframework.boot", "spring-boot-webtestclient").withoutVersion()
            library("spring-boot-security-test", "org.springframework.boot", "spring-boot-security-test").withoutVersion()
            library("kubernetes-mock", "io.fabric8", "kubernetes-server-mock").versionRef("kubernetes")
            library("kubernetes-mockserver", "io.fabric8", "mockwebserver").versionRef("kubernetes")
            bundle("test-api", listOf("reactor-test", "kotest-runner", "kotest-assertions-core", "kotest-framework-engine", "mockk", "kotest-extensions-spring", "spring-boot-test", "spring-boot-security-test", "spring-boot-webflux-test", "spring-boot-webtestclient"))

            // Test - Containers
            library("testcontainers-bom", "org.testcontainers", "testcontainers-bom").versionRef("testcontainers")
            library("testcontainers-junit", "org.testcontainers", "junit-jupiter").withoutVersion()
            library("testcontainers-postgresql", "org.testcontainers", "testcontainers-postgresql").withoutVersion()
            library("testcontainers-kafka", "org.testcontainers", "testcontainers-kafka").withoutVersion()
            library("kotest-extensions-testcontainers", "io.kotest.extensions", "kotest-extensions-testcontainers").versionRef("testcontainers")
            bundle("test-containers", listOf("testcontainers-junit", "kotest-extensions-testcontainers"))

            // Web UI
            library("elemento-core", "org.jboss.elemento", "elemento-core").versionRef("elemento")
            library("sayaya-ui", "dev.sayaya", "ui").versionRef("sayaya-ui")
            library("sayaya-rx", "dev.sayaya", "rx").versionRef("sayaya-rx")
            library("lombok", "org.projectlombok", "lombok").versionRef("lombok")
            library("dagger-gwt", "com.google.dagger", "dagger-gwt").versionRef("dagger")
            library("dagger-compiler", "com.google.dagger", "dagger-compiler").versionRef("dagger")
            bundle("sayaya-web", listOf("elemento-core", "dagger-gwt", "dagger-compiler", "sayaya-ui", "sayaya-rx", "lombok"))

            // Web Test
            library("junit5", "org.junit.jupiter", "junit-jupiter").versionRef("junit5")
            library("sayaya-test", "dev.sayaya", "gwt-test").versionRef("sayaya-test")
            bundle("test-web", listOf("kotest-runner", "sayaya-test", "mockk", "junit5"))
        }
    }
}
include("workspace")
include("schema")
include("document")
include("event")
include("authentication")
include("activity")
include("gateway")
include("event-broadcaster")
include("persist-type")
include("persist-workspace")
include("search")
include("search-type")
include("search-workspace")
include("ui-components")
include("agent-bridge")
include("agent-protocol")
include("agent-ui")
include("assistant")
include("type-ui")
include("shell-ui")
include("workspace-ui")
include("persist-document")
include("search-document")
include("document-ui")
include("dashboard-ui")
include("login")
include("login-ui")
include("test-utils")
include("e2e")
include("app")
