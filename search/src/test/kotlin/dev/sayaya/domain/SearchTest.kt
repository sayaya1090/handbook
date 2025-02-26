package dev.sayaya.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class SearchTest : StringSpec({
    "page가 음수일 경우 예외를 발생시켜야 한다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Search(page = -1, limit = 10, sortBy = null, asc = null)
        }
        exception.message shouldBe "Page must be a non-negative integer. Given: -1"
    }

    "limit이 1보다 작거나 100보다 클 경우 예외를 발생시켜야 한다" {
        val exceptionLow = shouldThrow<IllegalArgumentException> {
            Search(page = 0, limit = 0, sortBy = null, asc = null)
        }
        exceptionLow.message shouldBe "Limit must be greater than 0, and less than or equal to 100. Given: 0"

        val exceptionHigh = shouldThrow<IllegalArgumentException> {
            Search(page = 1, limit = 101, sortBy = null, asc = null)
        }
        exceptionHigh.message shouldBe "Limit must be greater than 0, and less than or equal to 100. Given: 101"
    }

    "asc가 null이 아니지만 sortBy가 null일 경우 예외를 발생시켜야 한다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Search(page = 0, limit = 10, sortBy = null, asc = true)
        }
        exception.message shouldBe "If 'asc' is not null, 'sortBy' must also be provided. Given: asc=true, sortBy=null"
    }

    "모든 값이 올바르면 성공적으로 인스턴스를 생성해야 한다" {
        val search = Search(
            page = 1,
            limit = 10,
            sortBy = "name",
            asc = true,
            filters = listOf("status" to "active", "category" to "books")
        )
        search.page shouldBe 1
        search.limit shouldBe 10
        search.sortBy shouldBe "name"
        search.asc shouldBe true
        search.filters shouldBe listOf("status" to "active", "category" to "books")
    }

    "filters가 비어 있어도 올바르게 인스턴스가 생성되어야 한다" {
        val search = Search(page = 1, limit = 10, sortBy = null, asc = null)
        search.filters shouldBe emptyList<Pair<String, String>>()
    }
})