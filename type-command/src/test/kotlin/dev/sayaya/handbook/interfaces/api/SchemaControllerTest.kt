package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.SchemaPatch
import dev.sayaya.handbook.usecase.SchemaService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

class SchemaControllerTest : BehaviorSpec({
    val schemaService = mockk<SchemaService>()
    val controller = SchemaController(schemaService)
    val webClient = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    Given("스키마 일괄 패치 API") {
        val patch = SchemaPatch.create(emptyArray(), emptyArray())
        every { schemaService.patch(workspace, any()) } returns Mono.just(patch)

        When("PATCH /workspaces/{ws}/schema를 호출하면") {
            val response = webClient.patch()
                .uri("/workspaces/$workspace/schema")
                .contentType(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .bodyValue(patch)
                .exchange()

            Then("200 OK가 반환된다") {
                response.expectStatus().isOk
                verify { schemaService.patch(workspace, any()) }
            }
        }
    }
})
