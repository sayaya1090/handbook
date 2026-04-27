plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
}
dependencies {
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    testImplementation(libs.bundles.test.web)
    testImplementation(project(":test-utils"))
    testAnnotationProcessor(libs.lombok)
}
tasks {
    jar {
        enabled = true
        from(sourceSets.main.get().allSource)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
    gwt {
        gwtVersion = "2.13.0"
        sourceLevel = "auto"
        modules = listOf("dev.sayaya.handbook.UiComponents")
        devMode {
            modules = listOf("dev.sayaya.handbook.UiComponentsTest")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler { strict = true }
        test {
            modules = listOf("dev.sayaya.handbook.UiComponentsTest")
            webPort = 18088
        }
    }
    test { useJUnitPlatform() }
}
