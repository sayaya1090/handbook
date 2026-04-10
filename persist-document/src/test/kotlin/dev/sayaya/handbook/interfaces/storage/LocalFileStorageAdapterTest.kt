package dev.sayaya.handbook.interfaces.storage

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import java.nio.file.Files
import java.util.*

class LocalFileStorageAdapterTest : BehaviorSpec({
    val tempDir = Files.createTempDirectory("file-upload-test")
    val adapter = LocalFileStorageAdapter(tempDir)

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    Given("로컬 파일 저장소") {
        val workspace = UUID.randomUUID()
        val content = "test file content".toByteArray()

        When("파일을 업로드하면") {
            val url = adapter.upload(workspace, "report.pdf", content).block()

            Then("파일 URL이 반환된다") {
                url shouldNotBe null
                url!! shouldContain workspace.toString()
                url shouldEndWith ".pdf"
            }

            Then("파일이 실제로 저장된다") {
                val wsDir = tempDir.resolve(workspace.toString())
                val files = Files.list(wsDir).toList()
                files.size shouldNotBe 0
            }
        }

        When("확장자가 없는 파일을 업로드하면") {
            Then("예외가 발생한다") {
                try {
                    adapter.upload(workspace, "noext", content).block()
                    throw AssertionError("Expected exception")
                } catch (e: Exception) {
                    e.message shouldContain "extension"
                }
            }
        }
    }
})
