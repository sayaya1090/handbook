package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.util.*

class GroupTest : StringSpec({

    "Group은 올바른 값이 주어지면 성공적으로 생성된다" {
        val id = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val group = Group(
            id = id,
            workspace = workspaceId,
            name = "Test Group",
            description = "Test Description"
        )

        group.id shouldBe id
        group.workspace shouldBe workspaceId
        group.name shouldBe "Test Group"
        group.description shouldBe "Test Description"
    }

    "Group은 description이 null이어도 성공적으로 생성된다" {
        val group = Group(
            id = UUID.randomUUID(),
            workspace = UUID.randomUUID(),
            name = "Test Group",
            description = null
        )

        group.name shouldBe "Test Group"
        group.description shouldBe null
    }

    "Group은 name이 비어있으면 IllegalArgumentException 예외를 발생시킨다" {
        shouldThrow<IllegalArgumentException> {
            Group(
                id = UUID.randomUUID(),
                workspace = UUID.randomUUID(),
                name = "",
                description = "Test Description"
            )
        } shouldHaveMessage "Name cannot be blank"
    }

    "두 Group은 id가 같으면 같은 객체로 판단한다" {
        val id = UUID.randomUUID()
        val group1 = Group(id, UUID.randomUUID(), "Name1", "Desc1")
        val group2 = Group(id, UUID.randomUUID(), "Name2", "Desc2")

        group1 shouldBe group2
        group1.hashCode() shouldBe group2.hashCode()
    }

    "두 Group은 id가 다르면 다른 객체로 판단한다" {
        val workspaceId = UUID.randomUUID()
        val group1 = Group(UUID.randomUUID(), workspaceId, "Name", "Desc")
        val group2 = Group(UUID.randomUUID(), workspaceId, "Name", "Desc")

        group1 shouldNotBe group2
    }

    "Group은 자기 자신과 같으며, null이나 다른 타입과는 다르다" {
        val group = Group(UUID.randomUUID(), UUID.randomUUID(), "Name", "Desc")

        (group == group) shouldBe true
        (group.equals(null)) shouldBe false
        (group.equals("some string")) shouldBe false
    }
})