package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.util.*

class WorkspaceTest : StringSpec({
    "Workspace는 올바르게 생성된다" {
        val id = UUID.randomUUID()
        val workspace = Workspace(
            id = id,
            name = "Test Workspace",
            description = "Test Description"
        )

        workspace.id shouldBe id
        workspace.name shouldBe "Test Workspace"
        workspace.description shouldBe "Test Description"
    }

    "Workspace는 description이 null일 수 있다" {
        val id = UUID.randomUUID()
        val workspace = Workspace(
            id = id,
            name = "Test Workspace",
            description = null
        )

        workspace.id shouldBe id
        workspace.name shouldBe "Test Workspace"
        workspace.description shouldBe null
    }

    "Workspace는 빈 name을 허용하지 않는다" {
        val id = UUID.randomUUID()
        shouldThrow<IllegalArgumentException> {
            Workspace(
                id = id,
                name = "",
                description = "Test Description"
            )
        } shouldHaveMessage "Name cannot be blank"
    }

    "WorkspaceSimple은 올바르게 생성된다" {
        val id = UUID.randomUUID()
        val workspaceSimple = Workspace.Companion.WorkspaceSimple(
            id = id,
            name = "Simple Workspace"
        )

        workspaceSimple.id shouldBe id
        workspaceSimple.name shouldBe "Simple Workspace"
    }

    "WorkspaceSimple은 빈 name을 허용하지 않는다" {
        val id = UUID.randomUUID()
        shouldThrow<IllegalArgumentException> {
            Workspace.Companion.WorkspaceSimple(
                id = id,
                name = ""
            )
        } shouldHaveMessage "Name cannot be blank"
    }

    "두 Workspace는 같은 id를 가지면 같다" {
        val id = UUID.randomUUID()
        val workspace1 = Workspace(id, "Name1", "Desc1")
        val workspace2 = Workspace(id, "Name2", "Desc2")

        workspace1 shouldBe workspace2
    }

    "두 Workspace는 다른 id를 가지면 다르다" {
        val workspace1 = Workspace(UUID.randomUUID(), "Name", "Desc")
        val workspace2 = Workspace(UUID.randomUUID(), "Name", "Desc")

        workspace1 shouldNotBe workspace2
    }
})
