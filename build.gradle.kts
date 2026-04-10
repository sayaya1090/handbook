plugins {
    id("java")
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.spring") version "2.3.0" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.google.cloud.tools.jib") version "3.5.2" apply false
    id("dev.sayaya.gwt") version "2.2.7" apply false
    id("com.adarshr.test-logger") version "4.0.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.4" apply false
}
subprojects {
    repositories {
        mavenCentral()
    }
    group = "dev.sayaya"
    version = "0.0.1"

    // Spring Boot 공통: logging exclude
    pluginManager.withPlugin("org.springframework.boot") {
        configurations.all {
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        }
    }
    // Spring Dependency Management 공통: Cloud BOM
    pluginManager.withPlugin("io.spring.dependency-management") {
        val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()
        the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().imports {
            mavenBom(libs.spring.cloud.bom.get().toString())
        }
    }
    // Jib 공통: 컨테이너 환경 설정
    pluginManager.withPlugin("com.google.cloud.tools.jib") {
        extensions.configure<com.google.cloud.tools.jib.gradle.JibExtension> {
            container {
                environment = mapOf(
                    "LANG" to "C.UTF-8",
                    "TZ" to "Asia/Seoul",
                )
            }
        }
    }
    // Kover 공통: 최소 커버리지 80%
    pluginManager.withPlugin("org.jetbrains.kotlinx.kover") {
        extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
            reports {
                verify {
                    rule {
                        disabled = false
                        bound { minValue = 80 }
                    }
                }
            }
        }
    }
    // 테스트 공통: JUnit Platform
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    // GWT UI 모듈 공통: 테스트 리소스(JS/CSS) 자동 복사
    pluginManager.withPlugin("dev.sayaya.gwt") {
        val copyTestResources = tasks.register<Copy>("copyTestWebResources") {
            from("${rootProject.projectDir}/shell-ui/src/test/webapp/js")
            into("${project.projectDir}/src/test/webapp/js")
        }
        val copyTestCss = tasks.register<Copy>("copyTestCssResources") {
            from("${rootProject.projectDir}/shell-ui/src/test/webapp/css")
            into("${project.projectDir}/src/test/webapp/css")
        }
        tasks.matching { it.name.startsWith("gwtDev") || it.name.startsWith("gwtCompile") }.configureEach {
            dependsOn(copyTestResources, copyTestCss)
        }
    }
}