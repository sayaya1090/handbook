plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
}
dependencies {
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    testImplementation(libs.bundles.test.web)
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
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
        modules = listOf("dev.sayaya.handbook.Activity")
        war = file("src/main/webapp")
        devMode {
            modules = listOf("dev.sayaya.handbook.ActivityTest")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler { strict = true }
        test {
            webPort = 18090
        }
        }
    test {
        useJUnitPlatform()
    }
}

