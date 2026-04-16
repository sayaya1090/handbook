plugins {
    kotlin("jvm")
    war
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
    jar {
        enabled = true
        from(sourceSets.main.get().allSource)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
    war {
        // GWT 컴파일 출력(build/gwt/war/shell) 을 WAR 루트에 포함.
        // app.html 이 참조하는 shell/shell.nocache.js 경로와 일치.
        dependsOn("gwtCompile")
        from("build/gwt/war")
        archiveFileName.set("shell-ui.war")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    gwt {
        gwtVersion = "2.13.0"
        sourceLevel = "auto"
        modules = listOf("dev.sayaya.handbook.Shell")
        devMode {
            modules = listOf("dev.sayaya.handbook.Shell",  "dev.sayaya.handbook.ApiTest", "dev.sayaya.handbook.FrameTest", "dev.sayaya.handbook.DrawerTest", "dev.sayaya.handbook.ProgressTest")
            war = file("src/test/webapp")
        }
        generateJsInteropExports = true
        compiler {
            strict = true
        }
    }
    test { useJUnitPlatform() }
}

