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
    // GWT UI 모듈 공통: 모듈별 고유 포트 할당 (병렬 테스트 가능)
    pluginManager.withPlugin("dev.sayaya.gwt") {
        val portMap = mapOf(
            "shell-ui" to 18080, "type-ui" to 18081, "document-ui" to 18082,
            "agent-ui" to 18083, "dashboard-ui" to 18084, "workspace-ui" to 18085,
            "login-ui" to 18086, "app" to 18087, "ui-components" to 18088,
            "agent-bridge" to 18089, "activity" to 18090,
        )
        tasks.withType<Test> {
            extensions.configure<dev.sayaya.gwt.GwtTestTaskExtension>("gwt") {
                webPort.set(portMap[project.name] ?: 18099)
            }
        }
    }
    // GWT UI 모듈 공통: 테스트 리소스(JS/CSS) 자동 복사
    pluginManager.withPlugin("dev.sayaya.gwt") {
        val copyTestResources = tasks.register<Copy>("copyTestWebResources") {
            from("${rootProject.projectDir}/shell-ui/src/test/webapp/js")
            into("${project.projectDir}/src/test/webapp/js")
            exclude("language.*.json")
        }
        val copyTestCss = tasks.register<Copy>("copyTestCssResources") {
            from("${rootProject.projectDir}/shell-ui/src/test/webapp/css")
            into("${project.projectDir}/src/test/webapp/css")
        }
        // I18N: 모든 모듈의 src/main/i18n/language.*.json을 머지하여 테스트 webapp에 출력
        val mergeI18n = tasks.register("mergeI18n") {
            val i18nDirs = rootProject.subprojects.map { it.file("src/main/i18n") }
            inputs.files(i18nDirs.filter { it.exists() }.flatMap { dir ->
                dir.listFiles()?.filter { it.name.startsWith("language.") && it.name.endsWith(".json") } ?: emptyList()
            })
            val outputDir = file("${project.projectDir}/src/test/webapp/js")
            outputs.dir(outputDir)
            doLast {
                val locales = mutableSetOf<String>()
                val allFiles = mutableListOf<java.io.File>()
                rootProject.subprojects.forEach { sub ->
                    val i18nDir = sub.file("src/main/i18n")
                    if (i18nDir.exists()) {
                        i18nDir.listFiles()?.filter { it.name.startsWith("language.") && it.name.endsWith(".json") }?.forEach { f ->
                            allFiles.add(f)
                            val locale = f.name.removePrefix("language.").removeSuffix(".json")
                            locales.add(locale)
                        }
                    }
                }
                locales.forEach { locale ->
                    val merged = mutableMapOf<String, Any>()
                    allFiles.filter { it.name == "language.${locale}.json" }.forEach { f ->
                        @Suppress("UNCHECKED_CAST")
                        val map = groovy.json.JsonSlurper().parse(f) as Map<String, Any>
                        merged.putAll(map)
                    }
                    val sorted = merged.toSortedMap()
                    val json = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(sorted))
                    // Groovy JsonOutput은 non-ASCII를 \uXXXX로 이스케이프하므로 역변환
                    val output = json.replace(Regex("\\\\u([0-9a-fA-F]{4})")) {
                        it.groupValues[1].toInt(16).toChar().toString()
                    }
                    outputDir.mkdirs()
                    File(outputDir, "language.${locale}.json").writeText(output + "\n", Charsets.UTF_8)
                }
            }
        }
        // shell-ui는 JS/CSS 소스가 자기 자신이므로 복사 불필요
        if (project.name != "shell-ui") {
            tasks.matching { it.name.startsWith("gwtDev") || it.name.startsWith("gwtCompile") }.configureEach {
                dependsOn(copyTestResources, copyTestCss, mergeI18n)
            }
        } else {
            tasks.matching { it.name.startsWith("gwtDev") || it.name.startsWith("gwtCompile") }.configureEach {
                dependsOn(mergeI18n)
            }
        }
    }
}