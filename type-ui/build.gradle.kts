import org.docstr.gwt.AbstractBaseTask

plugins {
    kotlin("jvm")
    id("war")
    id("org.docstr.gwt")
    id("com.adarshr.test-logger")
    id("gwt-test")
}
dependencies {
    implementation(libs.bundles.sayaya.web)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.dagger.compiler)
    testImplementation(libs.bundles.test.web)
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.dagger.compiler)
}
val lombok = project.configurations.annotationProcessor.get().filter { it.name.startsWith("lombok") }.single()!!
sourceSets {
    named("main") {
        java.srcDirs("build/generated/sources/annotationProcessor/java/main")
    }
    named("test") {
        java.srcDirs("build/generated/sources/annotationProcessor/java/test")
    }
}
gwt {
    gwtVersion = "2.12.1"
    modules = listOf("dev.sayaya.handbook.Type")
    sourceLevel = "auto"
    war = file("src/main/webapp")
}
tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<AbstractBaseTask> {
        setJvmArgs(listOf("-javaagent:${lombok}=ECJ"))
    }
    test {
        useJUnitPlatform()
        jvmArgs = listOf("-javaagent:${lombok}=ECJ")
        systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
    }
    jar {
        enabled = false
    }
    withType<War> {
        dependsOn(gwtCompile)
        archiveFileName.set("type-ui.war")
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
}
