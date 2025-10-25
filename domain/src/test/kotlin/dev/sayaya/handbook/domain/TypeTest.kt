package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant

class TypeTest : DescribeSpec({

    val now: Instant = Instant.now()

    describe("Type 클래스는") {

        describe("생성될 때") {
            context("올바른 값이 주어지면") {
                it("성공적으로 생성된다") {
                    shouldNotThrow<Exception> {
                        Type("user", "1.0", now, now.plusSeconds(1), "사용자 타입", false, null)
                    }
                }
            }

            context("유효성 검증이") {
                it("id가 비어있으면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Type("", "1.0", now, now.plusSeconds(1), null, false)
                    } shouldHaveMessage "Type id cannot be blank"
                }

                it("id에 허용되지 않는 문자가 있으면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Type("user type!", "1.0", now, now.plusSeconds(1), null, false)
                    } shouldHaveMessage "Type id can only contain alphabet, 한글, numbers, hyphens, and underscores."
                }

                it("만료일이 발효일보다 빠르면 예외를 발생시킨다") {
                    shouldThrow<IllegalArgumentException> {
                        Type("user", "1.0", now, now.minusSeconds(1), null, false)
                    } shouldHaveMessage "Expire date time must be after effect date time"
                }
            }
        }

        describe("동등성을 비교할 때") {
            it("id와 version이 모두 같으면 같은 객체로 판단한다") {
                val type1 = Type("user", "1.0", now, now.plusSeconds(1), "desc1", false)
                val type2 = Type("user", "1.0", now, now.plusSeconds(100), "desc2", true, "parent")
                type1 shouldBe type2
                type1.hashCode() shouldBe type2.hashCode()
            }

            it("id는 같지만 version이 다르면 다른 객체로 판단한다") {
                val type1 = Type("user", "1.0", now, now.plusSeconds(1), null, false)
                val type2 = Type("user", "1.1", now, now.plusSeconds(1), null, false)
                type1 shouldNotBe type2
            }

            it("version은 같지만 id가 다르면 다른 객체로 판단한다") {
                val type1 = Type("user-a", "1.0", now, now.plusSeconds(1), null, false)
                val type2 = Type("user-b", "1.0", now, now.plusSeconds(1), null, false)
                type1 shouldNotBe type2
            }

            it("엣지 케이스(자기 자신, null, 다른 타입)를 올바르게 처리한다") {
                val type1 = Type("user", "1.0", now, now.plusSeconds(1), null, false)
                (type1 == type1) shouldBe true
                (type1.equals(null)) shouldBe false
                (type1.equals("some string")) shouldBe false
            }
        }
    }
})