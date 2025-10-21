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
        workspace1.hashCode() shouldBe workspace2.hashCode()
    }

    "두 Workspace는 다른 id를 가지면 다르다" {
        val workspace1 = Workspace(UUID.randomUUID(), "Name", "Desc")
        val workspace2 = Workspace(UUID.randomUUID(), "Name", "Desc")

        workspace1 shouldNotBe workspace2
    }
    "Workspace는 자기 자신과 같으며, null이나 다른 타입과는 다르다" {
        val workspace = Workspace(UUID.randomUUID(), "Name", "Desc")

        (workspace == workspace) shouldBe true
        (workspace.equals(null)) shouldBe false
        (workspace.equals("some string")) shouldBe false
    }

    "Workspace와 WorkspaceSimple은 id가 같아도 다른 타입이므로 다르다" {
        val id = UUID.randomUUID()
        val workspace = Workspace(id, "Name", "Desc")
        val workspaceSimple = Workspace.Companion.WorkspaceSimple(id, "Name")

        workspace.equals(workspaceSimple) shouldBe false
        workspaceSimple.equals(workspace) shouldBe false
    }

    "WorkspaceSimple의 isFor 함수는 id가 같은 Workspace에 대해 true를 반환한다" {
        val id = UUID.randomUUID()
        val workspace = Workspace(id, "Full Name", "Full Desc")
        val workspaceSimple = Workspace.Companion.WorkspaceSimple(id, "Simple Name")
        val differentWorkspace = Workspace(UUID.randomUUID(), "Another Name", null)

        workspaceSimple.isFor(workspace) shouldBe true
        workspaceSimple.isFor(differentWorkspace) shouldBe false
    }

    "두 WorkspaceSimple은 id와 name이 모두 같아야 같다" {
        val id = UUID.randomUUID()
        val simple1 = Workspace.Companion.WorkspaceSimple(id, "Simple")
        val simple2 = Workspace.Companion.WorkspaceSimple(id, "Simple")

        simple1 shouldBe simple2
        simple1.hashCode() shouldBe simple2.hashCode()
    }

    "두 WorkspaceSimple은 id가 같아도 name이 다르면 다르다" {
        val id = UUID.randomUUID()
        val simple1 = Workspace.Companion.WorkspaceSimple(id, "Simple1")
        val simple2 = Workspace.Companion.WorkspaceSimple(id, "Simple2")

        simple1 shouldNotBe simple2
    }
})
