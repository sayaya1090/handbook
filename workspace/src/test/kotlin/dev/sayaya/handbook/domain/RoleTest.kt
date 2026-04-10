package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RoleTest : DescribeSpec({

    describe("Role은") {

        describe("생성 시") {
            it("유효한 이름과 권한으로 생성된다") {
                shouldNotThrow<Exception> {
                    Role("ADMIN", RoleLevel.WORKSPACE, setOf(Permission("workspace:*:*")))
                }
            }
            it("빈 이름은 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    Role("", RoleLevel.WORKSPACE, emptySet())
                }
            }
            it("빈 권한 집합도 허용된다") {
                shouldNotThrow<Exception> {
                    Role("VIEWER", RoleLevel.WORKSPACE, emptySet())
                }
            }
        }

        describe("hasPermission은") {
            val admin = Role("ADMIN", RoleLevel.WORKSPACE, setOf(
                Permission("workspace:*:*")
            ))
            val viewer = Role("VIEWER", RoleLevel.TYPE, setOf(
                Permission("workspace:type:customer:view"),
                Permission("workspace:type:customer:document:view")
            ))
            val empty = Role("EMPTY", RoleLevel.WORKSPACE, emptySet())

            it("와일드카드 역할은 모든 하위 권한을 포함한다") {
                admin.hasPermission(Permission("workspace:type:create")) shouldBe true
                admin.hasPermission(Permission("workspace:group:delete")) shouldBe true
            }
            it("정확히 일치하는 권한을 포함한다") {
                viewer.hasPermission(Permission("workspace:type:customer:view")) shouldBe true
            }
            it("불일치하면 false") {
                viewer.hasPermission(Permission("workspace:type:customer:edit")) shouldBe false
            }
            it("빈 권한 역할은 항상 false") {
                empty.hasPermission(Permission("workspace:type:create")) shouldBe false
            }
        }

        describe("동등성은") {
            it("같은 이름, 레벨, 권한이면 동일하다") {
                val perms = setOf(Permission("system:audit-logs"))
                Role("ADMIN", RoleLevel.SYSTEM, perms) shouldBe Role("ADMIN", RoleLevel.SYSTEM, perms)
            }
        }
    }
})
