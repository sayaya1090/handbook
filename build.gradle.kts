plugins {
    id("java")
    kotlin("jvm") version "2.2.20" apply false
    kotlin("plugin.spring") version "2.2.20" apply false
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.google.cloud.tools.jib") version "3.4.5" apply false
    id("dev.sayaya.gwt") version "2.1.6" apply false
    id("com.adarshr.test-logger") version "4.0.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.2" apply false
}
subprojects {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sayaya1090/maven")
            credentials {
                username = project.findProperty("github_username") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("github_password") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
        mavenCentral()
    }
    group = "dev.sayaya"
    version = "0.0.1"
}