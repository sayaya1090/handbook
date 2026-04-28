plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("dev.sayaya.gwt")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.adarshr.test-logger")
}

dependencies {
    implementation(libs.bundles.kotlin.webflux)
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    testImplementation(libs.bundles.test.api)
    testImplementation(libs.bundles.test.web)
}

gwt {
    gwtVersion = "2.13.0"
    sourceLevel = "auto"
    war = file("src/main/webapp")
    devMode {
        modules = listOf("dev.sayaya.handbook.WorkspaceTest")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18094
        modules = listOf("dev.sayaya.handbook.WorkspaceTest")
    }
    modules = listOf("dev.sayaya.handbook.Workspace")
}

tasks {
    jar {
        enabled = true
        from(sourceSets.main.get().allSource)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
    bootJar {
        enabled = false
    }
    test {
        useJUnitPlatform()
    }
}
