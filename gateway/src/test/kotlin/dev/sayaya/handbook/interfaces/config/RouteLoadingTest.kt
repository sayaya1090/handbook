package dev.sayaya.handbook.interfaces.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeGreaterThan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions
import org.springframework.cloud.gateway.server.webflux.config.GatewayProperties
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
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "services[0].name=test-service"
])
class RouteLoadingTest(
    private val gatewayProperties: GatewayProperties,
) : DescribeSpec({
    extensions(SpringExtension)

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

        it("login 라우트가 /auth/** 경로를 포함한다") {
            val loginRoute = gatewayProperties.routes.first { it.id == "login" }
            val predicateStrings = loginRoute.predicates.map { it.toString() }
            predicateStrings.any { it.contains("/auth/**") } shouldBe true
        }
    }
}) {
    companion object {
        private infix fun Boolean.shouldBe(expected: Boolean) {
            io.kotest.matchers.shouldBe(this, expected)
        }
    }
}
