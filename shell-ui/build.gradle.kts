plugins {
    kotlin("jvm")
    war
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}

dependencies {
    implementation(project(":activity"))
    implementation(project(":workspace"))
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
        modules = listOf(
            "dev.sayaya.handbook.Shell",
            "dev.sayaya.handbook.ApiTest",
            "dev.sayaya.handbook.DrawerTest",
            "dev.sayaya.handbook.FrameTest",
            "dev.sayaya.handbook.ProgressTest",
            "dev.sayaya.handbook.HistoryTest",
            "dev.sayaya.handbook.RedirectTest"
        )
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler {
        strict = true
    }
    test {
        webPort = 18080
    }
    modules = listOf("dev.sayaya.handbook.Shell")
}

tasks {
    war {
        dependsOn("gwtCompile")
        from("build/gwt/war") {
            into("js")
        }
        archiveFileName.set("shell-ui.war")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    test {
        useJUnitPlatform()
    }
}
