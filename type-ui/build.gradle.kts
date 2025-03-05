plugins {
    kotlin("jvm")
    id("war")
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.dagger.compiler)
    testImplementation(libs.bundles.test.web)
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
}
gwt {
    gwtVersion = "2.12.2"
    modules = listOf("dev.sayaya.handbook.Type")
    sourceLevel = "auto"
    war = file("src/main/webapp")
    devMode {
        modules = listOf(
            "dev.sayaya.handbook.Type",
            "dev.sayaya.handbook.Canvas",
            "dev.sayaya.handbook.Action",
            "dev.sayaya.handbook.TypeBox"
        )
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler {
        strict = true
    }
}
tasks.named("war", War::class) {
    archiveFileName.set("type-ui.war")
    duplicatesStrategy = DuplicatesStrategy.WARN
}
