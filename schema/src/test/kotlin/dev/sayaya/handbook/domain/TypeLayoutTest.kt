package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import java.util.*

class TypeLayoutTest : DescribeSpec({

    val now = Instant.now()
    val workspace = UUID.randomUUID()

    describe("TypeLayout는") {

        describe("생성될 때") {
            it("올바른 값이 주어지면 성공적으로 생성된다") {
                shouldNotThrow<Exception> {
                    TypeLayout(
                        UUID.randomUUID(), workspace, now, now.plusSeconds(3600),
                        mapOf("customer" to TypeLayout.Position(10, 20, 200, 100))
                    )
                }
            }
            it("빈 positions로도 생성할 수 있다") {
                shouldNotThrow<Exception> {
                    TypeLayout(UUID.randomUUID(), workspace, now, now.plusSeconds(1), emptyMap())
                }
            }
            it("만료일이 발효일보다 빠르면 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> {
                    TypeLayout(UUID.randomUUID(), workspace, now, now.minusSeconds(1), emptyMap())
                } shouldHaveMessage "Expire date time must be after effect date time"
            }
        }

        describe("동등성을 비교할 때") {
            val id = UUID.randomUUID()

            it("id가 같으면 같은 객체로 판단한다") {
                val layout1 = TypeLayout(id, workspace, now, now.plusSeconds(1), emptyMap())
                val layout2 = TypeLayout(id, workspace, now, now.plusSeconds(100),
                    mapOf("x" to TypeLayout.Position(0, 0, 50, 50)))
                layout1 shouldBe layout2
                layout1.hashCode() shouldBe layout2.hashCode()
            }
            it("id가 다르면 다른 객체로 판단한다") {
                val layout1 = TypeLayout(UUID.randomUUID(), workspace, now, now.plusSeconds(1), emptyMap())
                val layout2 = TypeLayout(UUID.randomUUID(), workspace, now, now.plusSeconds(1), emptyMap())
                layout1 shouldNotBe layout2
            }
        }
    }

    describe("TypeLayout.Position은") {
        it("올바른 값이 주어지면 성공적으로 생성된다") {
            val pos = TypeLayout.Position(100, 200, 300, 150)
            pos.x shouldBe 100
            pos.y shouldBe 200
            pos.width shouldBe 300
            pos.height shouldBe 150
        }
        it("width가 0 이하이면 예외를 발생시킨다") {
            shouldThrow<IllegalArgumentException> {
                TypeLayout.Position(0, 0, 0, 100)
            } shouldHaveMessage "Width must be greater than 0. Current value: 0"
        }
        it("height가 0 이하이면 예외를 발생시킨다") {
            shouldThrow<IllegalArgumentException> {
                TypeLayout.Position(0, 0, 100, 0)
            } shouldHaveMessage "Height must be greater than 0. Current value: 0"
        }
        it("값 객체로서 모든 프로퍼티가 같을 때만 동일하다") {
            val pos1 = TypeLayout.Position(10, 20, 100, 50)
            val pos2 = TypeLayout.Position(10, 20, 100, 50)
            val pos3 = TypeLayout.Position(10, 20, 100, 60)
            pos1 shouldBe pos2
            pos1 shouldNotBe pos3
        }
    }
})
