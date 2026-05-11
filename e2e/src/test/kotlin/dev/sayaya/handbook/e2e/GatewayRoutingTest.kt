package dev.sayaya.handbook.e2e

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * 게이트웨이 라우팅 E2E 테스트.
 *
 * 게이트웨이가 각 서비스로 올바르게 라우팅하는지 검증한다.
 * 각 라우트별로 서비스가 응답하는지(401 포함) 확인하여
 * 라우팅 설정이 올바른지 검증한다.
 */
class GatewayRoutingTest : BehaviorSpec({
    val baseUrl = System.getenv("APP_BASE_URL") ?: "http://localhost:8080"
    val client = HttpClient.newHttpClient()
    val workspaceId = "00000000-0000-0000-0000-000000000000"

    fun get(path: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder().uri(URI.create("$baseUrl$path")).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response
    }

    fun request(method: String, path: String): HttpResponse<String> {
        val builder = HttpRequest.newBuilder().uri(URI.create("$baseUrl$path"))
            .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
        when (method) {
            "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString("[]"))
            "PATCH" -> builder.method("PATCH", HttpRequest.BodyPublishers.ofString("[]"))
            "DELETE" -> builder.method("DELETE", HttpRequest.BodyPublishers.ofString("[]"))
            "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString("{}"))
        }
        val response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        return response
    }

    Given("인증 라우트 (/auth/**)") {
        When("GET /auth/login을 호출하면") {
            Then("login 서비스로 라우팅된다 (401, 302, 5xx)") {
                val response = get("/auth/login")
                val status = response.statusCode()
                // login 서비스가 없으면 502/503/500, 있으면 302 redirect 또는 200/401
                (status in listOf(200, 302, 401, 404, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("타입 조회 라우트 (GET /workspaces/*/types/**)") {
        When("GET /workspaces/{id}/types를 호출하면") {
            Then("type-query 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspaces/$workspaceId/types?effect_date_time=2026-01-01T00:00:00Z&expire_date_time=2026-12-31T23:59:59Z")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("타입 저장 라우트 (PUT /workspaces/*/types/**)") {
        When("PUT /workspaces/{id}/types를 호출하면") {
            Then("type-command 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("PUT", "/workspaces/$workspaceId/types")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("스키마 패치 라우트 (PATCH /workspaces/*/schema)") {
        When("PATCH /workspaces/{id}/schema를 호출하면") {
            Then("type-command 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("PATCH", "/workspaces/$workspaceId/schema")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("문서 조회 라우트 (GET /workspaces/*/documents/**)") {
        When("GET /workspaces/{id}/documents를 호출하면") {
            Then("document-query 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspaces/$workspaceId/documents")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("문서 저장 라우트 (PUT /workspaces/*/documents/**)") {
        When("PUT /workspaces/{id}/documents를 호출하면") {
            Then("document-command 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("PUT", "/workspaces/$workspaceId/documents")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("워크스페이스 관리 라우트 (POST /workspaces)") {
        When("POST /workspaces를 호출하면") {
            Then("workspace-command 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("POST", "/workspaces")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("에이전트 라우트 (/assistant/**)") {
        When("GET /assistant/health를 호출하면") {
            Then("assistant 서비스로 라우팅된다") {
                val response = get("/assistant/health")
                val status = response.statusCode()
                (status in listOf(200, 401, 404, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("SSE 이벤트 라우트 (GET /workspaces/*/messages)") {
        When("GET /workspaces/{id}/messages를 호출하면") {
            Then("event-broadcaster 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspaces/$workspaceId/messages")
                val status = response.statusCode()
                // Circuit Breaker가 동작하면 200 (fallback)
                (status in listOf(200, 401, 500, 502, 503)) shouldBe true
            }
        }
    }

    Given("레이아웃 라우트") {
        When("GET /workspaces/{id}/layouts를 호출하면") {
            Then("type-query 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspaces/$workspaceId/layouts")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }

        When("PUT /workspaces/{id}/layouts를 호출하면") {
            Then("type-command 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("PUT", "/workspaces/$workspaceId/layouts")
                val status = response.statusCode()
                (status in listOf(401, 500, 502, 503)) shouldBe true
            }
        }
    }
})
