package dev.sayaya.handbook.interfaces.authentication

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

internal class SecurityContextUuidAuditorConfigTest : BehaviorSpec({
    val config = SecurityContextUuidAuditorConfig()
    val auditorProvider = config.auditorProvider()
    fun <T> ReactiveAuditorAware<T>.auditor(authentication: Authentication): Mono<T> = currentAuditor
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))

    Given("인증된 사용자의 principal이 UUID일 때") {
        val userId = UUID.randomUUID()
        val authentication = TestingAuthenticationToken(userId, null).apply { isAuthenticated = true }

        When("SecurityContext로부터 현재 Auditor를 조회하면") {
            val auditor = auditorProvider.auditor(authentication)

            Then("해당 UUID를 반환한다") {
                StepVerifier.create(auditor).expectNext(userId).verifyComplete()
            }
        }
    }

    Given("인증된 사용자의 principal이 String 타입의 UUID일 때") {
        val userId = UUID.randomUUID()
        val authentication = TestingAuthenticationToken(userId.toString(), null).apply { isAuthenticated = true }

        When("SecurityContext로부터 현재 Auditor를 조회하면") {
            val auditor = auditorProvider.auditor(authentication)

            Then("UUID로 변환하여 반환한다") {
                StepVerifier.create(auditor).expectNext(userId).verifyComplete()
            }
        }
    }

    Given("사용자가 인증되지 않았을 때") {
        val authentication = TestingAuthenticationToken(UUID.randomUUID(), null) // isAuthenticated is false

        When("SecurityContext로부터 현재 Auditor를 조회하면") {
            val auditor = auditorProvider.auditor(authentication)

            Then("비어있는 Mono를 반환한다") {
                StepVerifier.create(auditor)
                    .verifyComplete()
            }
        }
    }

    Given("인증된 사용자의 principal이 예상치 못한 타입일 때") {
        val principal = 12345 // Int 타입
        val authentication = TestingAuthenticationToken(principal, null).apply { isAuthenticated = true }

        When("SecurityContext로부터 현재 Auditor를 조회하면") {
            val auditor = auditorProvider.auditor(authentication)

            Then("비어있는 Mono를 반환한다") {
                StepVerifier.create(auditor)
                    .verifyComplete()
            }
        }
    }

    Given("보안 컨텍스트가 비어있을 때") {
        When("현재 Auditor를 조회하면") {
            val auditor = auditorProvider.currentAuditor

            Then("비어있는 Mono를 반환한다") {
                StepVerifier.create(auditor)
                    .verifyComplete()
            }
        }
    }
})