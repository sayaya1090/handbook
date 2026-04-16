plugins {
    kotlin("jvm")
    war
    id("com.adarshr.test-logger")
}
dependencies {
    testImplementation(libs.bundles.test.web)
    testImplementation(project(":test-utils"))
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
        // shell-ui, agent-ui 의 GWT 컴파일 출력을 각각 shell/, agent/ 하위에 포함.
        // app.html 이 참조하는 shell/shell.nocache.js, agent/agent.nocache.js 경로와 일치.
        dependsOn(":shell-ui:gwtCompile", ":agent-ui:gwtCompile", mergeI18nProd)
        from("${rootProject.projectDir}/shell-ui/build/gwt/war")
        from("${rootProject.projectDir}/agent-ui/build/gwt/war")
        archiveFileName.set("app.war")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    test { useJUnitPlatform() }
}
