plugins {
    kotlin("jvm")
    id("war")
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation(project(":activity"))
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.dagger.compiler)
    testImplementation(libs.bundles.test.web)
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
}
gwt {
    gwtVersion = "2.12.2"
    modules = listOf("dev.sayaya.handbook.Document")
    sourceLevel = "auto"
    war = file("src/main/webapp")
    devMode {
        modules = listOf(
            "dev.sayaya.handbook.Document",
        )
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler {
        strict = true
    }
}
tasks {
    register<Copy>("copyResources") {
        from(project(":ui-asset").file("src/main/webapp"))
        into("src/main/webapp")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    named("processResources") {
        dependsOn("copyResources")
    }
    register<Copy>("copyTestResources") {
        dependsOn("copyResources")
        from("src/main/webapp")
        into("src/test/webapp")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    named("compileTestJava") {
        dependsOn("copyTestResources")
    }
    named("war", War::class) {
        archiveFileName.set("document-ui.war")
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
    named("compileTestKotlin") {
        dependsOn(named("compileJava"))
    }
}
