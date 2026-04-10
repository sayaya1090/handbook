package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PermissionTest : DescribeSpec({

    describe("Permission은") {

        describe("생성 시") {
            it("유효한 권한 문자열로 생성된다") {
                shouldNotThrow<Exception> {
                    Permission("workspace:type:create")
                }
            }
            it("2 세그먼트 권한도 허용된다") {
                shouldNotThrow<Exception> {
                    Permission("system:audit-logs")
                }
            }
            it("빈 문자열은 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    Permission("")
                }
            }
            it("세그먼트가 1개이면 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    Permission("workspace")
                }
            }
            it("빈 세그먼트가 있으면 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    Permission("workspace::create")
                }
            }
        }

        describe("matches는") {
            it("정확히 일치하면 true") {
                Permission("workspace:type:create").matches(Permission("workspace:type:create")) shouldBe true
            }
            it("와일드카드가 해당 세그먼트를 매칭한다") {
                Permission("workspace:type:*").matches(Permission("workspace:type:create")) shouldBe true
                Permission("workspace:type:*").matches(Permission("workspace:type:delete")) shouldBe true
            }
            it("중간 세그먼트 와일드카드도 동작한다") {
                Permission("workspace:*:view").matches(Permission("workspace:type:view")) shouldBe true
                Permission("workspace:*:view").matches(Permission("workspace:group:view")) shouldBe true
            }
            it("전체 와일드카드") {
                Permission("*:*").matches(Permission("system:audit-logs")) shouldBe true
            }
            it("불일치하면 false") {
                Permission("workspace:type:create").matches(Permission("workspace:type:delete")) shouldBe false
            }
            it("세그먼트 수가 다르면 false") {
                Permission("workspace:type").matches(Permission("workspace:type:create")) shouldBe false
            }
            it("대상에 와일드카드가 있어도 this 기준으로 매칭한다") {
                Permission("workspace:type:create").matches(Permission("workspace:*:create")) shouldBe false
            }
        }

        describe("동등성은") {
            it("같은 값이면 동일하다") {
                Permission("workspace:type:create") shouldBe Permission("workspace:type:create")
            }
            it("다른 값이면 다르다") {
                (Permission("workspace:type:create") == Permission("workspace:type:delete")) shouldBe false
            }
        }
    }
})
