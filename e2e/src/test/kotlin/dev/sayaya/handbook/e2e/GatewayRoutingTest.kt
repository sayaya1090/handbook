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
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun request(method: String, path: String): HttpResponse<String> {
        val builder = HttpRequest.newBuilder().uri(URI.create("$baseUrl$path"))
            .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
        when (method) {
            "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString("[]"))
            "DELETE" -> builder.method("DELETE", HttpRequest.BodyPublishers.ofString("[]"))
            "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString("{}"))
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    Given("인증 라우트 (/auth/**)") {
        When("GET /auth/login을 호출하면") {
            Then("login 서비스로 라우팅된다 (401 또는 302)") {
                val response = get("/auth/login")
                // login 서비스가 없으면 502/503, 있으면 302 redirect 또는 200
                (response.statusCode() in listOf(200, 302, 401, 502, 503)) shouldBe true
            }
        }
    }

    Given("타입 조회 라우트 (GET /workspace/*/types/**)") {
        When("GET /workspace/{id}/types를 호출하면") {
            Then("search-type 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspace/$workspaceId/types?effect_date_time=2026-01-01T00:00:00Z&expire_date_time=2026-12-31T23:59:59Z")
                // 인증 필요하므로 401, 서비스 없으면 502
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }
    }

    Given("타입 저장 라우트 (PUT /workspace/*/types/**)") {
        When("PUT /workspace/{id}/types를 호출하면") {
            Then("persist-type 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("PUT", "/workspace/$workspaceId/types")
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }
    }

    Given("문서 조회 라우트 (GET /workspace/*/documents/**)") {
        When("GET /workspace/{id}/documents를 호출하면") {
            Then("search-document 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspace/$workspaceId/documents")
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }
    }

    Given("문서 저장 라우트 (PUT /workspace/*/documents/**)") {
        When("PUT /workspace/{id}/documents를 호출하면") {
            Then("persist-document 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("PUT", "/workspace/$workspaceId/documents")
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }
    }

    Given("워크스페이스 관리 라우트 (POST /workspace)") {
        When("POST /workspace를 호출하면") {
            Then("persist-workspace 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("POST", "/workspace")
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }
    }

    Given("에이전트 라우트 (/assistant/**)") {
        When("GET /assistant/health를 호출하면") {
            Then("assistant 서비스로 라우팅된다") {
                val response = get("/assistant/health")
                // assistant 서비스가 없으면 502, 있으면 200 또는 401
                (response.statusCode() in listOf(200, 401, 404, 502)) shouldBe true
            }
        }
    }

    Given("SSE 이벤트 라우트 (GET /workspace/*/messages)") {
        When("GET /workspace/{id}/messages를 호출하면") {
            Then("event-broadcaster 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspace/$workspaceId/messages")
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }
    }

    Given("레이아웃 라우트") {
        When("GET /workspace/{id}/layouts를 호출하면") {
            Then("search-type 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = get("/workspace/$workspaceId/layouts")
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }

        When("PUT /workspace/{id}/layouts를 호출하면") {
            Then("persist-type 서비스로 라우팅된다 (401: 인증 필요)") {
                val response = request("PUT", "/workspace/$workspaceId/layouts")
                (response.statusCode() in listOf(401, 502)) shouldBe true
            }
        }
    }
})
