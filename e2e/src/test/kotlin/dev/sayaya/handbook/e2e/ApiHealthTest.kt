package dev.sayaya.handbook.e2e

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * 게이트웨이를 통한 API 엔드포인트 E2E 테스트.
 *
 * 각 백엔드 서비스가 게이트웨이 뒤에서 정상적으로 응답하는지 검증한다.
 * JWT 인증이 필요한 엔드포인트는 인증 없이 401을 반환하는지 확인한다.
 */
class ApiHealthTest : BehaviorSpec({
    val baseUrl = System.getenv("APP_BASE_URL") ?: "http://localhost:8080"
    val client = HttpClient.newHttpClient()

    fun get(path: String, headers: Map<String, String> = emptyMap()): HttpResponse<String> {
        val builder = HttpRequest.newBuilder().uri(URI.create("$baseUrl$path")).GET()
        headers.forEach { (k, v) -> builder.header(k, v) }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    fun put(path: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse<String> {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
        headers.forEach { (k, v) -> builder.header(k, v) }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    Given("게이트웨이 헬스체크") {
        When("actuator/health 엔드포인트를 호출하면") {
            Then("200 OK와 UP 상태가 반환된다") {
                val response = get("/actuator/health")
                response.statusCode() shouldBe 200
                response.body().contains("UP") shouldBe true
            }
        }
    }

    Given("인증 없이 보호된 API에 접근") {
        When("GET /workspace를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = get("/workspace")
                response.statusCode() shouldBe 401
            }
        }

        When("GET /workspace/{id}/types를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = get("/workspace/00000000-0000-0000-0000-000000000000/types?effect_date_time=2026-01-01T00:00:00Z&expire_date_time=2026-12-31T23:59:59Z")
                response.statusCode() shouldBe 401
            }
        }

        When("GET /workspace/{id}/documents를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = get("/workspace/00000000-0000-0000-0000-000000000000/documents")
                response.statusCode() shouldBe 401
            }
        }

        When("PUT /workspace/{id}/types를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = put("/workspace/00000000-0000-0000-0000-000000000000/types", "[]")
                response.statusCode() shouldBe 401
            }
        }

        When("PUT /workspace/{id}/documents를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = put("/workspace/00000000-0000-0000-0000-000000000000/documents", "[]")
                response.statusCode() shouldBe 401
            }
        }
    }

    Given("인증 서비스 라우팅") {
        When("GET /user를 호출하면") {
            Then("인증 없이 401이 반환된다") {
                val response = get("/user")
                response.statusCode() shouldBe 401
            }
        }
    }

    Given("잘못된 JWT로 접근") {
        val fakeJwt = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJmYWtlIn0.invalid"

        When("유효하지 않은 토큰으로 워크스페이스를 조회하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = get("/workspace", mapOf("Authorization" to fakeJwt))
                response.statusCode() shouldBe 401
            }
        }
    }

    Given("존재하지 않는 경로 접근") {
        When("존재하지 않는 경로를 호출하면") {
            Then("404 Not Found가 반환된다") {
                val response = get("/nonexistent/path/that/does/not/exist")
                // 게이트웨이가 매칭 안 되면 404, 인증 필터에 걸리면 401
                (response.statusCode() in listOf(401, 404)) shouldBe true
            }
        }
    }
})
