plugins {
    kotlin("jvm")
    war
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation(project(":activity"))
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

tasks {
    war {
        dependsOn("gwtCompile")
        archiveFileName.set("shell-ui.war")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    gwt {
        gwtVersion = "2.13.0"
        sourceLevel = "auto"
        modules = listOf("dev.sayaya.handbook.Shell")
        devMode {
            modules = listOf("dev.sayaya.handbook.Shell", "dev.sayaya.handbook.ApiTest", "dev.sayaya.handbook.DrawerTest", "dev.sayaya.handbook.FrameTest", "dev.sayaya.handbook.ProgressTest", "dev.sayaya.handbook.HistoryTest")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler {
            strict = true
        }
        test {
            webPort = 18080
        }
    }
    test { useJUnitPlatform() }
}

