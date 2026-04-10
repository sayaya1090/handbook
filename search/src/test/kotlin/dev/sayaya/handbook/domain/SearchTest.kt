package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SearchTest : BehaviorSpec({

    Given("유효한 파라미터로 Search를 생성할 때") {
        When("page, limit, sortBy, asc가 모두 유효하면") {
            val search = Search(page = 0, limit = 10, sortBy = "name", asc = true)

            Then("정상적으로 생성된다") {
                search.page shouldBe 0
                search.limit shouldBe 10
                search.sortBy shouldBe "name"
                search.asc shouldBe true
            }
        }

        When("sortBy와 asc가 모두 null이면") {
            val search = Search(page = 0, limit = 50, sortBy = null, asc = null)

            Then("정상적으로 생성된다") {
                search.sortBy shouldBe null
                search.asc shouldBe null
            }
        }

        When("filters가 지정되면") {
            val search = Search(
                page = 0, limit = 10, sortBy = null, asc = null,
                filters = listOf("type" to "customer", "serial" to "CUST-001"),
            )

            Then("필터가 정상적으로 포함된다") {
                search.filters.size shouldBe 2
                search.filters[0] shouldBe ("type" to "customer")
            }
        }
    }

    Given("page가 음수인 경우") {
        When("Search를 생성하면") {
            Then("IllegalArgumentException이 발생한다") {
                val ex = shouldThrow<IllegalArgumentException> {
                    Search(page = -1, limit = 10, sortBy = null, asc = null)
                }
                ex.message shouldContain "Page must be a non-negative integer"
            }
        }
    }

    Given("asc가 지정되었지만 sortBy가 null인 경우") {
        When("Search를 생성하면") {
            Then("IllegalArgumentException이 발생한다") {
                val ex = shouldThrow<IllegalArgumentException> {
                    Search(page = 0, limit = 10, sortBy = null, asc = true)
                }
                ex.message shouldContain "sortBy"
            }
        }
    }
})
