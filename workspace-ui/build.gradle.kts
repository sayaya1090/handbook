plugins {
    java
    kotlin("jvm")
    id("war")
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation(project(":activity"))
    implementation(project(":workspace"))
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
    devMode {
        modules = listOf("dev.sayaya.handbook.Workspace", "dev.sayaya.handbook.WorkspaceManagementTest")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18085
    }
    modules = listOf("dev.sayaya.handbook.Workspace")
}

tasks {

    war {
        dependsOn("gwtCompile")
        from("build/gwt/war") {
            into("js")
        }
        archiveFileName.set("workspace-ui.war")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    test { useJUnitPlatform() }
}
