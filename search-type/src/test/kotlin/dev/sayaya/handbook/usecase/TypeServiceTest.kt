package dev.sayaya.handbook.usecase

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
internal class TypeServiceTest : ShouldSpec({
    val mockRepository = mockk<TypeRepository>()
    val service = TypeService(mockRepository)
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    beforeEach {
        clearMocks(mockRepository, recordedCalls = true, answers = false, verificationMarks = true)
    }

    context("findAll 메서드 테스트") {

    }
    context("findByRange 메서드 테스트") {
        val baseEffectTime = Instant.parse("2025-03-15T00:00:00Z")
        val baseExpireTime = Instant.parse("2025-03-16T00:00:00Z") // effect + 1 day

        // Type 샘플 데이터 생성 (필요에 따라 수정)
        val sampleType1 = Type(
            id = "TypeA",
            version = "1.0",
            parent = null,
            effectDateTime = baseEffectTime.minusSeconds(3600), // 검색 시작 시간보다 이전
            expireDateTime = baseEffectTime.plusSeconds(3600),    // 검색 시작 시간보다 이후
            description = "Description A",
            primitive = true,
            x=10u, y=10u, width=100u, height=50u
        )
        val sampleType2 = Type(
            id = "TypeB",
            version = "2.0",
            parent = "TypeA",
            effectDateTime = baseEffectTime.plusSeconds(1800),  // 검색 구간 내
            expireDateTime = baseExpireTime.minusSeconds(1800), // 검색 구간 내
            description = "Description B",
            primitive = false,
            x=150u, y=10u, width=80u, height=80u
        )

        should("리포지토리의 findByRange를 호출하고 결과를 그대로 반환해야 한다") {
            // Given: Mock Repository가 특정 Type Flux를 반환하도록 설정
            every { mockRepository.findByRange(workspace, baseEffectTime, baseExpireTime) } returns
                    Flux.just(sampleType1, sampleType2)

            // When: 서비스의 findByRange 메서드 호출
            val resultFlux = service.findByRange(workspace, baseEffectTime, baseExpireTime)

            // Then: 결과 Flux 검증
            StepVerifier.create(resultFlux)
                .expectNext(sampleType1) // 예상되는 첫 번째 요소
                .expectNext(sampleType2) // 예상되는 두 번째 요소
                .verifyComplete() // Flux 완료 검증

            // Verify: 리포지토리의 findByRange 메서드가 정확한 인자와 함께 1번 호출되었는지 확인
            verify(exactly = 1) { mockRepository.findByRange(workspace, baseEffectTime, baseExpireTime) }
        }

        should("리포지토리에서 빈 Flux를 반환하면 빈 Flux를 반환해야 한다") {
            // Given: Mock Repository가 빈 Flux를 반환하도록 설정
            every { mockRepository.findByRange(workspace, baseEffectTime, baseExpireTime) } returns Flux.empty()

            // When: 서비스의 findByRange 메서드 호출
            val resultFlux = service.findByRange(workspace, baseEffectTime, baseExpireTime)

            // Then: 빈 Flux 검증
            StepVerifier.create(resultFlux).verifyComplete()

            // Verify: 리포지토리 메서드 호출 확인
            verify(exactly = 1) { mockRepository.findByRange(workspace, baseEffectTime, baseExpireTime) }
        }

        should("리포지토리에서 에러 Flux를 반환하면 에러를 전파해야 한다") {
            // Given: Mock Repository가 에러를 반환하도록 설정
            val exception = RuntimeException("범위 조회 중 DB 오류")
            every { mockRepository.findByRange(workspace, baseEffectTime, baseExpireTime) } returns Flux.error(exception)

            // When: 서비스의 findByRange 메서드 호출
            val resultFlux = service.findByRange(workspace, baseEffectTime, baseExpireTime)

            // Then: 에러 검증
            StepVerifier.create(resultFlux)
                .expectErrorMatches { it is RuntimeException && it.message == "범위 조회 중 DB 오류" }
                .verify()

            // Verify: 리포지토리 메서드 호출 확인
            verify(exactly = 1) { mockRepository.findByRange(workspace, baseEffectTime, baseExpireTime) }
        }
    }
})