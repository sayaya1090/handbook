plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
}
dependencies {
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.bundles.test.web)
}

gwt {
    gwtVersion = "2.13.0"
    sourceLevel = "auto"
    war = file("src/main/webapp")
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18089
    }
    modules = listOf("dev.sayaya.handbook.AgentBridge")
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
