plugins {
    kotlin("jvm")
    id("dev.sayaya.gwt")
}

dependencies {
    implementation(project(":agent-bridge"))
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    testImplementation(libs.bundles.test.api)
}

gwt {
    gwtVersion = "2.13.0"
    sourceLevel = "auto"
    modules = listOf("dev.sayaya.handbook.SchemaApi")
}

tasks {
    jar {
        enabled = true
        from(sourceSets.main.get().allSource)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
}
