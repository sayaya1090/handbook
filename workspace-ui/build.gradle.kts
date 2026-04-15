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
    gwtVersion = "2.12.2"
    modules = listOf("dev.sayaya.handbook.Workspace")
    sourceLevel = "auto"
    devMode {
        modules = listOf("dev.sayaya.handbook.WorkspaceTest")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
}
tasks.register<Copy>("copyTestResources") {
    from("src/main/webapp")
    into("src/test/webapp")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.named("compileTestJava") { dependsOn("copyTestResources") }
tasks.named("war", War::class) {
    // GWT 컴파일 출력(build/gwt/war/workspace) 을 js/ 하위로 포함해
    // shell 이 참조하는 /js/workspace/** 경로와 일치시킨다.
    dependsOn("gwtCompile")
    from("build/gwt/war") {
        into("js")
    }
    archiveFileName.set("workspace-ui.war")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
