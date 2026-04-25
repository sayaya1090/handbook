plugins {
    kotlin("jvm")
    id("war")
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation(project(":activity"))
    implementation(project(":agent-bridge"))
    implementation(project(":ui-components"))
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.dagger.compiler)
    testImplementation(libs.bundles.test.web)
    testImplementation(project(":test-utils"))
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
}
gwt {
    gwtVersion = "2.13.0"
    sourceLevel = "auto"
    modules = listOf("dev.sayaya.handbook.Type")
    devMode {
        modules = listOf("dev.sayaya.handbook.Type", "dev.sayaya.handbook.CanvasTest")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18081
    }
}
tasks.register<Copy>("copyTestResources") {
    from("src/main/webapp")
    into("src/test/webapp")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.named("compileTestJava") { dependsOn("copyTestResources") }
tasks.named("war", War::class) {
    dependsOn("gwtCompile")
    archiveFileName.set("type-ui.war")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.test { useJUnitPlatform() }
