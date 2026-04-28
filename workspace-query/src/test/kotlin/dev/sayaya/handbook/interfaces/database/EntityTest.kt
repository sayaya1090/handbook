package dev.sayaya.handbook.interfaces.database

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*

class EntityTest : DescribeSpec({
    describe("R2dbcWorkspaceEntity") {
        it("필드 초기화 검증") {
            val id = UUID.randomUUID()
            val entity = R2dbcWorkspaceEntity(id, "name", "desc")
            entity.id shouldBe id
            entity.name shouldBe "name"
            entity.description shouldBe "desc"
        }
        it("equals/hashCode 검증") {
            val id = UUID.randomUUID()
            val e1 = R2dbcWorkspaceEntity(id, "a", "b")
            val e2 = R2dbcWorkspaceEntity(id, "a", "b")
            val e3 = R2dbcWorkspaceEntity(UUID.randomUUID(), "a", "b")
            
            e1 shouldBe e2
            e1.hashCode() shouldBe e2.hashCode()
            e1 shouldNotBe e3
        }
        it("toString 검증") {
            val id = UUID.randomUUID()
            val entity = R2dbcWorkspaceEntity(id, "a", "b")
            entity.toString() shouldBe "R2dbcWorkspaceEntity(id=$id, name=a, description=b)"
        }
        it("copy 검증") {
            val id = UUID.randomUUID()
            val e1 = R2dbcWorkspaceEntity(id, "a", "b")
            val e2 = e1.copy(name = "new")
            e2.name shouldBe "new"
            e2.id shouldBe id
        }
    }
})
