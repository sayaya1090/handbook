plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
}
dependencies {
    implementation(project(":agent-bridge"))
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.bundles.test.web)
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
}

gwt {
    gwtVersion = "2.13.0"
    sourceLevel = "auto"
    war = file("src/main/webapp")
    devMode {
        modules = listOf("dev.sayaya.handbook.UiComponentsTest")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18093
    }
    modules = listOf("dev.sayaya.handbook.UiComponents")
}

tasks {
    jar {
        enabled = true
        from(sourceSets.main.get().allSource)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
    test {
        useJUnitPlatform()
    }
}
