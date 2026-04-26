plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
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
    gwt {
        gwtVersion = "2.13.0"
        sourceLevel = "auto"
        modules = listOf("dev.sayaya.handbook.Document")
        devMode {
            modules = listOf("dev.sayaya.handbook.Document", "dev.sayaya.handbook.DocumentTest")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler { strict = true }
        test {
            webPort = 18082
        }
    }
    test { useJUnitPlatform() }
}
