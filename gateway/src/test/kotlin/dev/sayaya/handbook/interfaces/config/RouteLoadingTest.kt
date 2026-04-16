package dev.sayaya.handbook.interfaces.config

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.gateway.config.GatewayProperties
import org.springframework.test.context.TestPropertySource

/**
 * Gateway 라우트 로딩 통합 테스트.
 *
 * Spring Cloud Gateway 5.0 (server-webflux) 환경에서
 * `spring.cloud.gateway.server.webflux.routes` 경로의 라우트가
 * 정상적으로 파싱·로딩되는지 검증한다.
 *
 * **주의:** 구 프로퍼티 경로(`spring.cloud.gateway.routes`)를 사용하면
 * 라우트가 0개로 로딩되므로 반드시 `server.webflux` 경로를 사용해야 한다.
 *
 * **의존관계:** [GatewayProperties] — Spring Cloud Gateway가 자동 바인딩하는 라우트 설정
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "services[0].name=test-service"
])
@ApplyExtension(SpringExtension::class)
class RouteLoadingTest(
    private val gatewayProperties: GatewayProperties,
) : DescribeSpec({

    describe("Gateway 라우트 로딩은") {
        it("application.yml에서 라우트를 1개 이상 로딩한다") {
            gatewayProperties.routes.size shouldBeGreaterThan 0
        }

        it("필수 라우트 ID를 모두 포함한다") {
            val routeIds = gatewayProperties.routes.map { it.id }
            routeIds shouldContainAll listOf(
                "login", "search-type", "persist-type",
                "search-document", "persist-document",
                "persist-workspace", "assistant", "static"
            )
        }

        it("login 라우트가 Path 프레디케이트에 /auth/** 경로를 포함한다") {
            val loginRoute = gatewayProperties.routes.first { it.id == "login" }
            val pathPredicate = loginRoute.predicates.first { it.name == "Path" }
            val pathArgs = pathPredicate.args.values.joinToString(",")
            pathArgs.contains("/auth/**") shouldBe true
        }

        it("CircuitBreaker 필터가 assistant와 event-broadcaster에 적용된다") {
            val assistantRoute = gatewayProperties.routes.first { it.id == "assistant" }
            assistantRoute.filters.any { it.name == "CircuitBreaker" } shouldBe true

            val eventRoute = gatewayProperties.routes.first { it.id == "event-broadcaster" }
            eventRoute.filters.any { it.name == "CircuitBreaker" } shouldBe true
        }
    }
})
