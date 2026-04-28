plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
}

dependencies {
    implementation(libs.bundles.sayaya.web)
    implementation("tools.jackson.core:jackson-databind:3.0.3")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.3")
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
        modules = listOf("dev.sayaya.handbook.SchemaTest")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18091
        modules = listOf("dev.sayaya.handbook.SchemaTest")
    }
    modules = listOf("dev.sayaya.handbook.Schema")
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
