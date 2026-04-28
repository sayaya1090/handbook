package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.usecase.FileStorageService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.util.*

class FileUploadControllerTest : BehaviorSpec({
    val storageService = mockk<FileStorageService>()
    val allowedExtensions = setOf("pdf", "xlsx", "docx", "png", "jpg", "jpeg", "csv")
    val controller = FileUploadController(storageService, allowedExtensions, 52428800L)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    Given("허용된 확장자의 파일 업로드") {
        val fileUrl = "/workspaces/$workspace/files/${UUID.randomUUID()}.pdf"
        every { storageService.upload(workspace, "test.pdf", any()) } returns Mono.just(fileUrl)

        When("POST /workspaces/{id}/files를 호출하면") {
            Then("201 Created와 파일 URL이 반환된다") {
                val bodyBuilder = MultipartBodyBuilder()
                bodyBuilder.part("file", "dummy-pdf-content".toByteArray())
                    .filename("test.pdf")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)

                client.post()
                    .uri("/workspaces/$workspace/files")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody()
                    .jsonPath("$.url").isEqualTo(fileUrl)
            }
        }
    }

    Given("허용되지 않은 확장자의 파일 업로드") {
        When("POST /workspaces/{id}/files에 .exe 파일을 업로드하면") {
            Then("400 Bad Request가 반환된다") {
                val bodyBuilder = MultipartBodyBuilder()
                bodyBuilder.part("file", "malicious-content".toByteArray())
                    .filename("virus.exe")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)

                client.post()
                    .uri("/workspaces/$workspace/files")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }
    }
})
