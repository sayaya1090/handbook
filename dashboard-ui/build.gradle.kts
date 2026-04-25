plugins {
    kotlin("jvm")
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
tasks {
    gwt {
        gwtVersion = "2.13.0"
        sourceLevel = "auto"
        modules = listOf("dev.sayaya.handbook.Dashboard")
        devMode {
            modules = listOf("dev.sayaya.handbook.Dashboard", "dev.sayaya.handbook.DashboardTest")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler { strict = true }
        test {
            webPort = 18084
        }
    }
    test { useJUnitPlatform() }
}
