plugins {
    id("java")
    id("war")
    id("dev.sayaya.gwt")
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
    sourceLevel = "auto"
    war = file("src/main/webapp")
    devMode {
        modules = listOf(
            "dev.sayaya.handbook.ActivityTest"
        )
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler {
        strict = true
    }
}
tasks.jar {
    enabled = true
    from(sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.WARN
}