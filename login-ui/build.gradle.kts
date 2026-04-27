plugins {
    kotlin("jvm")
    war
    id("dev.sayaya.gwt")
    id("com.adarshr.test-logger")
}
dependencies {
    implementation(project(":activity"))
    implementation(project(":agent-bridge"))
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.dagger.compiler)
    testImplementation(libs.bundles.test.web)
    testImplementation(project(":test-utils"))
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
}

gwt {
    gwtVersion = "2.13.0"
    sourceLevel = "auto"
    devMode {
        modules = listOf("dev.sayaya.handbook.LoginTest", "dev.sayaya.handbook.Login", "dev.sayaya.handbook.Logout")
        war = file("src/test/webapp")
    }
    generateJsInteropExports = true
    compiler { strict = true }
    test {
        webPort = 18087
    }
    modules = listOf("dev.sayaya.handbook.Login", "dev.sayaya.handbook.Logout")
}

tasks {
    war {
        // GWT 컴파일 출력(build/gwt/war/{login,logout}) 을 js/ 하위로 포함해
        // src/main/webapp/login.html, logout.html 이 참조하는 경로와 일치시킨다.
        dependsOn("gwtCompile")
        from("build/gwt/war") {
            into("js")
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    test { useJUnitPlatform() }
}
