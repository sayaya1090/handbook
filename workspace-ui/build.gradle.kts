plugins {
    java
    kotlin("jvm")
    id("war")
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation(project(":activity"))
    implementation(project(":workspace-api-gwt"))
    implementation(project(":agent-bridge"))
    implementation(project(":ui-components"))
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
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
        modules = listOf("dev.sayaya.handbook.Workspace")
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
    register<Copy>("copyTestResources") {
        from("src/main/webapp")
        into("src/test/webapp")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    register<Copy>("copyGwtToTest") {
        dependsOn("gwtTestCompile")
        from("build/gwt/war/workspace")
        into("src/test/webapp/workspace")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    named("compileTestJava") { dependsOn("copyTestResources") }
    named("test") { 
        dependsOn("copyGwtToTest")
    }
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

tasks.jar {
    from(sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
