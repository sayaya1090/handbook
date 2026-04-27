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

gwt {
    gwtVersion = "2.13.0"
    sourceLevel = "auto"
    devMode {
        modules = listOf("dev.sayaya.handbook.UiComponents", "dev.sayaya.handbook.UiComponentsTest")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18088
    }
    modules = listOf("dev.sayaya.handbook.UiComponents")
}

tasks {
    jar {
        enabled = true
        from(sourceSets.main.get().allSource)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
    test { useJUnitPlatform() }
}
