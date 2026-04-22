package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Search
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

class R2dbcDocumentSearchRepositoryHelperTest : DescribeSpec({
    val repo = R2dbcDocumentSearchRepository(mockk(relaxed = true), jacksonObjectMapper())

    describe("R2dbcDocumentSearchRepository Helpers") {
        it("createPageRequest: 검색 파라미터로부터 PageRequest를 생성한다") {
            val search = Search(page = 1, limit = 10, sortBy = "serial", asc = false)
            val result = repo.createPageRequest(search)
            
            result.pageNumber shouldBe 1
            result.pageSize shouldBe 10
            result.sort.getOrderFor("serial")?.isDescending shouldBe true
        }
        it("predicate: 다양한 필터 조건에 대한 Criteria를 생성한다") {
            // workspace
            val wsId = UUID.randomUUID()
            repo.predicate("workspace", wsId) shouldNotBe null
            
            // date (Long)
            repo.predicate("date", 123456789L) shouldNotBe null
            
            // date (String)
            repo.predicate("date", "123456789") shouldNotBe null
            
            // date (String invalid)
            repo.predicate("date", "invalid-date") shouldNotBe null
            
            // date (non-string/non-long)
            repo.predicate("date", true) shouldNotBe null
            
            // other properties
            repo.predicate("serial", "S-001") shouldNotBe null
            repo.predicate("type", "T-001") shouldNotBe null
            repo.predicate("serial", null) shouldNotBe null // isNull branch
            repo.predicate("unknown", "val") shouldNotBe null
        }
        it("property: 필드명 매핑을 검증한다") {
            repo.property("serial") shouldBe "serial"
            repo.property("type") shouldBe "type"
            repo.property("unknown") shouldBe null
        }
        it("buildCriteria: 필터 목록으로부터 복합 Criteria를 생성한다") {
            repo.buildCriteria(emptyList<Pair<String, Any?>>()) shouldNotBe null
            repo.buildCriteria(listOf("serial" to "S-001", "type" to "T-001")) shouldNotBe null
        }
    }
})
