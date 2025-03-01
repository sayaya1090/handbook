package dev.sayaya.handbook.`interface`.api

import dev.sayaya.domain.Search
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchService
import dev.sayaya.`interface`.api.SearchArgumentResolver
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import io.kotest.core.spec.style.ShouldSpec
import org.springframework.http.MediaType
import java.time.Instant

@ExtendWith(SpringExtension::class)
internal class SearchControllerTest : ShouldSpec({
    val mockService = mockk<TypeSearchService>()
    val controller = SearchController(mockService)
    val webTestClient = WebTestClient.bindToController(controller).argumentResolvers { resolvers ->
        resolvers.addCustomResolver(SearchArgumentResolver())
    }.build()

    should("올바른 검색 요청 시 올바른 결과를 반환해야 한다") {
        // Given: Mock된 서비스 응답 정의
        val searchParam = Search(
            filters = listOf("name" to "Name1"),
            page = 0,
            limit = 10,
            sortBy = "created_at",
            asc = true
        )
        val expectedTypes = listOf(
            Type(
                id = "type_1",
                parent = null,
                version = "v1",
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plusSeconds(3600),
                description = "description",
                primitive = true,
                attributes = emptyList()
            )
        )
        val expectedPage = PageImpl(expectedTypes, PageRequest.of(0, 10), 1)

        every { mockService.search(searchParam) } returns Mono.just(expectedPage)

        // When: API 호출
        webTestClient.get().uri { builder ->
            builder.path("/types")
                .queryParam("name", "Name1")
                .queryParam("page", "0")
                .queryParam("limit", "10")
                .queryParam("sort_by", "created_at")
                .queryParam("asc", "true")
                .build()
        }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content[0].id").isEqualTo("type_1")
            .jsonPath("$.content[0].version").isEqualTo("v1")
            .jsonPath("$.totalElements").isEqualTo(1)
    }

    should("잘못된 요청이 들어오면 400 BAD_REQUEST를 반환해야 한다") {
        // Given: Mock된 서비스에서 IllegalArgumentException이 발생하도록 설정
        every { mockService.search(any()) } throws IllegalArgumentException("Invalid query parameter")

        // When: API 호출
        webTestClient.get().uri { builder ->
            builder.path("/types")
                .queryParam("name", "invalid name")
                .queryParam("page", "0")
                .queryParam("limit", "10")
                .queryParam("sort_by", "created_at")
                .queryParam("asc", "true")
                .build()
        }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .consumeWith { response ->
                assert(response.responseBody!!.toString(Charsets.UTF_8) == "Invalid query parameter")
            }
    }
})