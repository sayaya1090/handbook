
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

val gwtSuperDevMonitorJob = CompletableDeferred<Unit>()
tasks.named("gwtSuperDev") {
    doFirst {
        println("Starting GWT SuperDev monitoring...")

        // Gradle 로그 캡처 세팅
        logging.captureStandardOutput(LogLevel.LIFECYCLE)
        logging.captureStandardError(LogLevel.ERROR)
    }

    doLast {
        // 태스크 완료 후 전체 로그를 출력하거나 저장
        println("GWT SuperDev task completed.")
    }

    // 로그 리스너를 바로 등록해 출력 실시간 감시
    project.gradle.addListener(object : StandardOutputListener {
        override fun onOutput(output: CharSequence?) {
            if (output != null) {
                //println(output) // 실시간 로그 출력
                // "The code server is ready" 메시지 감지 시 추가 처리
                if (output.contains("The code server is ready")) {
                    println("GWT SuperDev is ready!")
                    gwtSuperDevMonitorJob.complete(Unit)
                }
            }
        }
    })
}
tasks.register("gwtTest") {
    group = "other"
    description = "This is a custom task to print a message."
    dependsOn("gwtSuperDev")

    doLast {
        runBlocking {
            println("Waiting for GWT SuperDev to be ready...")
            gwtSuperDevMonitorJob.await() // SuperDev 준비 완료 대기
        }
        println("Hello from the new custom task!")
    }
    finalizedBy("stopGwtSuperDev")
}
