plugins {
    kotlin("jvm")
    war
    id("dev.sayaya.gwt")
}
dependencies {
    implementation(project(":activity"))
    implementation(project(":ui-components"))
    implementation(project(":shell-ui"))
    implementation(project(":agent-ui"))
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.dagger.compiler)
    testImplementation(libs.bundles.test.web)
    testImplementation(project(":test-utils"))
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
}
val mergeI18nProd by tasks.registering {
    val i18nDirs = rootProject.subprojects.map { it.file("src/main/i18n") }
    inputs.files(i18nDirs.filter { it.exists() }.flatMap { dir ->
        dir.listFiles()?.filter { it.name.startsWith("language.") && it.name.endsWith(".json") } ?: emptyList()
    })
    val outputDir = file("src/main/webapp/js")
    outputs.dir(outputDir)
    doLast {
        val locales = mutableSetOf<String>()
        val allFiles = mutableListOf<File>()
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
            val output = json.replace(Regex("\\\\u([0-9a-fA-F]{4})")) {
                it.groupValues[1].toInt(16).toChar().toString()
            }
            outputDir.mkdirs()
            File(outputDir, "language.${locale}.json").writeText(output + "\n", Charsets.UTF_8)
        }
    }
}
tasks {
    war {
        // GWT 컴파일 출력(build/gwt/war/app) 을 app/ 하위로 포함해
        // src/main/webapp/app.html 이 참조하는 `app/app.nocache.js` 경로와 일치시킨다.
        dependsOn("gwtCompile")
        from("build/gwt/war")
        archiveFileName.set("app.war")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    gwt {
        gwtVersion = "2.13.0"
        sourceLevel = "auto"
        modules = listOf("dev.sayaya.handbook.App")
        devMode {
            modules = listOf("dev.sayaya.handbook.App", "dev.sayaya.handbook.AppTest")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler { strict = true }
    }
    matching { it.name.startsWith("gwtCompile") }.configureEach {
        dependsOn(mergeI18nProd)
    }
    test { useJUnitPlatform() }
}
