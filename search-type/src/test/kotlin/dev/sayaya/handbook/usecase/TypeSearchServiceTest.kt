package dev.sayaya.handbook.usecase

import dev.sayaya.domain.Search
import dev.sayaya.handbook.domain.Type
import io.mockk.every
import io.mockk.mockk
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

internal class TypeSearchServiceTest : ShouldSpec({
    val mockRepository = mockk<TypeSearchRepository>()
    val service = TypeSearchService(mockRepository)
    should("date 필터가 주어지지 않으면 필터에 추가되어 검색 조건이 전달되고 결과를 반환해야 한다") {
        // Given: Date 필터가 없는 검색 조건을 설정
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

        // Date 필터 추가를 확인하기 위해 매칭 조건 설정
        every { mockRepository.search(match { param ->
            param.filters.any { it.first == "date" } && param.filters.contains("name" to "Name1") })
        } returns Mono.just(expectedPage)

        // When: 서비스 메서드 호출
        val result = service.search(searchParam)

        // Then: 검증
        StepVerifier.create(result).assertNext { page ->
            page.content shouldBe expectedTypes
            page.totalElements shouldBe 1
            page.number shouldBe 0
        }.verifyComplete()
    }

    should("date 필터가 이미 있는 경우 필터를 추가하지 않고 검색해야 한다") {
        // Given: Date 필터가 포함된 검색 조건을 설정
        val existingDate = Instant.now().toEpochMilli().toString()
        val searchParam = Search(
            filters = listOf("name" to "Name1", "date" to existingDate),
            page = 0,
            limit = 10,
            sortBy = null,
            asc = null
        )
        val expectedTypes = listOf(
            Type(
                id = "type_2",
                parent = null,
                version = "v2",
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plusSeconds(7200),
                description = "description",
                primitive = true,
                attributes = emptyList()
            )
        )
        val expectedPage = PageImpl(expectedTypes, PageRequest.of(0, 10), 1)

        // Date 필터가 수정되었는지 확인 (기존 값 유지)
        every { mockRepository.search(match { param ->
            param.filters.contains("name" to "Name1") && param.filters.contains("date" to existingDate) })
        } returns Mono.just(expectedPage)

        // When: 서비스 메서드 호출
        val result = service.search(searchParam)

        // Then: 검증
        StepVerifier.create(result).assertNext { page ->
            page.content shouldBe expectedTypes
            page.totalElements shouldBe 1
        }.verifyComplete()
    }
})