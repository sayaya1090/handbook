plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
}
dependencies {
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    testImplementation(libs.bundles.test.web)
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
        modules = listOf("dev.sayaya.handbook.AgentBridge")
        war = file("src/main/webapp")
        generateJsInteropExports = true
        compiler { strict = true }
        test {
            webPort = 18089
        }
        }
    test {
        useJUnitPlatform()
    }
}
