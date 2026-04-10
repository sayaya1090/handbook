package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.util.*

class UserTest : StringSpec({

    "User는 올바른 값이 주어지면 성공적으로 생성된다" {
        val id = UUID.randomUUID()
        val workspaces = listOf(
            Workspace.Companion.WorkspaceSimple(
                id = UUID.randomUUID(),
                name = "Simple Workspace"
            )
        )
        val user = User(
            id = id,
            name = "Test User",
            workspaces = workspaces
        )

        user.id shouldBe id
        user.name shouldBe "Test User"
        user.workspaces shouldBe workspaces
    }

    "User는 name이 비어있으면 IllegalArgumentException 예외를 발생시킨다" {
        shouldThrow<IllegalArgumentException> {
            User(
                id = UUID.randomUUID(),
                name = "",
                workspaces = emptyList()
            )
        } shouldHaveMessage "Name cannot be blank"
    }

    "두 User는 id가 같으면 같은 객체로 판단한다" {
        val id = UUID.randomUUID()
        val user1 = User(id, "Name1", emptyList())
        val user2 = User(id, "Name2", listOf(Workspace.Companion.WorkspaceSimple(UUID.randomUUID(), "ws1")))

        user1 shouldBe user2
        user1.hashCode() shouldBe user2.hashCode()
    }

    "두 User는 id가 다르면 다른 객체로 판단한다" {
        val user1 = User(UUID.randomUUID(), "Name", emptyList())
        val user2 = User(UUID.randomUUID(), "Name", emptyList())

        user1 shouldNotBe user2
    }
    "User는 자기 자신과 같으며, null이나 다른 타입과는 다르다" {
        val user = User(UUID.randomUUID(), "Test User", emptyList())

        (user == user) shouldBe true
        (user.equals(null)) shouldBe false
        (user.equals("some string")) shouldBe false
    }
})