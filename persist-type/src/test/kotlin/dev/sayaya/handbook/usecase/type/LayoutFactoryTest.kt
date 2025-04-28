package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Suppress("ReactiveStreamsUnusedPublisher")
internal class LayoutFactoryTest : ShouldSpec({
    val repo = mockk<LayoutRepository>()
    val factory = LayoutFactory(repo)

    beforeTest {
        clearMocks(repo)
    }

    // Given - 공통 테스트 데이터
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    val fixedId = UUID.fromString("d8b77904-04fc-45f0-9a3d-ac9a21e0a216")
    val effectiveDate = Instant.parse("2024-01-01T00:00:00Z")
    val expireDate = Instant.parse("2024-12-31T23:59:59Z")

    val type1 = Type(
        id = "test1",
        version = "1.0",
        effectDateTime = effectiveDate,
        expireDateTime = expireDate,
        primitive = false,
        description = "test type",
        attributes = emptyList()
    )

    val type2 = Type(
        id = "test2",
        version = "1.0",
        effectDateTime = effectiveDate.plus(5, ChronoUnit.DAYS),
        expireDateTime = expireDate.minus(5, ChronoUnit.DAYS),
        primitive = false,
        description = "test type2",
        attributes = emptyList()
    )

    // Layout 객체 생성 헬퍼 함수
    fun createLayout (
        workspace: UUID,
        id: UUID,
        effectDate: Instant,
        expireDate: Instant
    ): Layout = Layout(workspace, id).apply {
        effectDateTime = effectDate
        expireDateTime = expireDate
    }

    should("빈 타입 목록으로 호출 시 빈 Mono 반환") {
        // When & Then
        factory.getOrCreateLayouts(workspace, emptyList())
            .let(StepVerifier::create)
            .verifyComplete()

        verify(exactly = 0) { repo.findById(workspace, any()) }
    }

    should("기존 레이아웃이 없을 때 새 레이아웃 생성 및 저장") {
        // 새 레이아웃 생성 시나리오
        val types = listOf(type1, type2)
        val newLayout = createLayout(workspace, fixedId, type2.effectDateTime, type2.expireDateTime)

        every { repo.findById(workspace,any()) } returns Mono.empty()
        every { repo.save(any()) } returns Mono.just(newLayout)

        // When & Then
        factory.getOrCreateLayouts(workspace, types)
            .let(StepVerifier::create)
            .expectNextMatches { layout ->
                layout.workspace == workspace &&
                        layout.effectDateTime == type2.effectDateTime &&
                        layout.expireDateTime == type2.expireDateTime
            }.verifyComplete()

        verify(exactly = 1) { repo.findById(workspace,any()) }
        verify(exactly = 1) { repo.save(any()) }
    }

    should("기존 레이아웃의 날짜가 계산된 날짜와 일치할 때 저장 스킵") {
        // 날짜가 일치하는 경우
        val types = listOf(type1)
        val existingLayout = createLayout(workspace, fixedId, type1.effectDateTime, type1.expireDateTime)

        every { repo.findById(workspace,any()) } returns Mono.just(existingLayout)

        // When & Then
        factory.getOrCreateLayouts(workspace, types)
            .let(StepVerifier::create)
            .expectNextMatches { layout ->
                layout.workspace == workspace &&
                        layout.effectDateTime == type1.effectDateTime &&
                        layout.expireDateTime == type1.expireDateTime
            }.verifyComplete()

        verify(exactly = 1) { repo.findById(workspace,any()) }
        verify(exactly = 0) { repo.save(any()) } // 날짜가 같으므로 저장 호출 없음
    }

    should("기존 레이아웃의 날짜가 계산된 날짜와 다를 때 업데이트 및 저장") {
        // 날짜가 다른 경우
        val types = listOf(type2)
        val oldEffectiveDate = effectiveDate.minus(10, ChronoUnit.DAYS)
        val oldExpireDate = expireDate.plus(10, ChronoUnit.DAYS)

        val existingLayout = createLayout(workspace, fixedId, oldEffectiveDate, oldExpireDate)
        val updatedLayout = createLayout(workspace, fixedId, type2.effectDateTime, type2.expireDateTime)

        every { repo.findById(workspace,any()) } returns Mono.just(existingLayout)
        every { repo.save(any()) } returns Mono.just(updatedLayout)

        // When & Then
        factory.getOrCreateLayouts(workspace, types)
            .let(StepVerifier::create)
            .expectNextMatches { layout ->
                layout.workspace == workspace &&
                        layout.effectDateTime == type2.effectDateTime &&
                        layout.expireDateTime == type2.expireDateTime
            }.verifyComplete()

        verify(exactly = 1) { repo.findById(workspace,any()) }
        verify(exactly = 1) { repo.save(any()) } // 날짜가 다르므로 저장 호출
    }

    should("유효기간이 교차하지 않는 타입들로 호출 시 예외 발생") {
        // 교차하지 않는 타입들
        val nonOverlappingType1 = Type(
            id = "no-overlap1",
            version = "1.0",
            effectDateTime = Instant.parse("2024-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2024-01-31T23:59:59Z"),
            primitive = false,
            description = "",
            attributes = emptyList()
        )

        val nonOverlappingType2 = Type(
            id = "no-overlap2",
            version = "1.0",
            effectDateTime = Instant.parse("2024-02-01T00:00:00Z"),
            expireDateTime = Instant.parse("2024-02-28T23:59:59Z"),
            primitive = false,
            description = "",
            attributes = emptyList()
        )

        val types = listOf(nonOverlappingType1, nonOverlappingType2)

        // When & Then
        factory.getOrCreateLayouts(workspace, types)
            .let(StepVerifier::create)
            .verifyErrorSatisfies { error ->
                check(error is IllegalStateException)
                error.message?.contains("유효 기간이 교차하지 않습니다")
            }
    }
})