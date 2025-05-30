package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

@Suppress("ReactiveStreamsUnusedPublisher")
internal class LayoutServiceTest : ShouldSpec({
    val mockRepository = mockk<LayoutRepository>()
    val service = LayoutService(mockRepository)
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    beforeEach {
        clearMocks(mockRepository, recordedCalls = true, answers = false, verificationMarks = true)
    }

    context("findAll 메서드 테스트") {
        should("리포지토리에서 반환된 Layout 목록을 effectDateTime 기준으로 정렬하여 반환해야 한다") {
            // Given: Mock Repository가 반환할 Layout 데이터 (정렬되지 않은 상태)
            val t1 = Instant.parse("2024-01-01T10:00:00Z")
            val t2 = Instant.parse("2024-01-01T11:00:00Z")
            val t3 = Instant.parse("2024-01-01T09:00:00Z") // t1보다 이전 시간
            val layout1 = Layout(workspace, t1, t2)
            val layout2 = Layout(workspace, t2, t2.plusSeconds(3600)) // t2 이후
            val layout3 = Layout(workspace, t3, t1) // t3 (가장 이름) 시작

            // Mock 설정: repository.findAll(workspace)가 호출되면 미리 정의된 Flux 반환
            every { mockRepository.findAll(workspace) } returns Flux.just(layout1, layout2, layout3)

            // When: 서비스의 findAll 메서드 호출
            val resultFlux = service.findAll(workspace)

            // Then: 결과 Flux 검증 (StepVerifier 사용)
            StepVerifier.create(resultFlux)
                .expectNext(layout3) // t3가 가장 먼저 나와야 함 (정렬 확인)
                .expectNext(layout1) // 다음은 t1
                .expectNext(layout2) // 마지막은 t2
                .verifyComplete() // Flux가 정상적으로 완료되었는지 확인

            // Verify: 리포지토리의 findAll 메서드가 정확히 1번 호출되었는지 확인
            verify(exactly = 1) { mockRepository.findAll(workspace) }
        }

        should("리포지토리에서 빈 Flux를 반환하면 빈 Flux를 반환해야 한다") {
            // Given: Mock Repository가 빈 Flux를 반환하도록 설정
            every { mockRepository.findAll(workspace) } returns Flux.empty()

            // When: 서비스의 findAll 메서드 호출
            val resultFlux = service.findAll(workspace)

            // Then: 빈 Flux 검증
            StepVerifier.create(resultFlux).verifyComplete() // 아무런 요소도 방출하지 않고 완료되어야 함

            // Verify: 리포지토리 메서드 호출 확인
            verify(exactly = 1) { mockRepository.findAll(workspace) }
        }

        should("리포지토리에서 에러 Flux를 반환하면 에러를 전파해야 한다") {
            // Given: Mock Repository가 에러를 반환하도록 설정
            val exception = RuntimeException("DB 오류 발생")
            every { mockRepository.findAll(workspace) } returns Flux.error(exception)

            // When: 서비스의 findAll 메서드 호출
            val resultFlux = service.findAll(workspace)

            // Then: 에러 검증
            StepVerifier.create(resultFlux)
                .expectErrorMatches { it is RuntimeException && it.message == "DB 오류 발생" } // 발생한 에러 검증
                .verify()

            // Verify: 리포지토리 메서드 호출 확인
            verify(exactly = 1) { mockRepository.findAll(workspace) }
        }
    }
})