plugins {
    war
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
        // HTML + CSS + vendor JS + i18n 만 패키징.
        // shell-ui / agent-ui 의 GWT 출력은 각 모듈이 독립 배포 (S3 별도 sync).
        dependsOn(mergeI18nProd)
        archiveFileName.set("app.war")
    }
}
