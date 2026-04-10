package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant
import java.util.*

class ValidationTaskTest : DescribeSpec({

    val now = Instant.now()
    val workspace = UUID.randomUUID()
    val docId = UUID.randomUUID()

    describe("ValidationTask는") {

        describe("생성될 때") {
            it("NEW 상태로 생성할 수 있다 (completedAt = null)") {
                shouldNotThrow<Exception> {
                    ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                        ValidationTask.Status.NEW, now, null)
                }
            }
            it("PROCESSING 상태로 생성할 수 있다 (completedAt = null)") {
                shouldNotThrow<Exception> {
                    ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                        ValidationTask.Status.PROCESSING, now, null)
                }
            }
            it("DONE 상태로 생성할 수 있다 (completedAt 필수)") {
                shouldNotThrow<Exception> {
                    ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                        ValidationTask.Status.DONE, now, now.plusSeconds(10))
                }
            }
            it("FAILED 상태로 생성할 수 있다 (completedAt 필수)") {
                shouldNotThrow<Exception> {
                    ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                        ValidationTask.Status.FAILED, now, now.plusSeconds(5))
                }
            }
        }

        describe("유효성 검증이") {
            it("비완료 상태인데 completedAt이 있으면 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> {
                    ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                        ValidationTask.Status.NEW, now, now.plusSeconds(10))
                }
            }
            it("완료 상태인데 completedAt이 없으면 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> {
                    ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                        ValidationTask.Status.DONE, now, null)
                }
            }
        }

        describe("동등성을 비교할 때") {
            val id = UUID.randomUUID()

            it("id가 같으면 같은 객체로 판단한다") {
                val task1 = ValidationTask(id, workspace, docId, "customer", "1.0",
                    ValidationTask.Status.NEW, now, null)
                val task2 = ValidationTask(id, workspace, docId, "customer", "2.0",
                    ValidationTask.Status.PROCESSING, now, null)
                task1 shouldBe task2
                task1.hashCode() shouldBe task2.hashCode()
            }
            it("id가 다르면 다른 객체로 판단한다") {
                val task1 = ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                    ValidationTask.Status.NEW, now, null)
                val task2 = ValidationTask(UUID.randomUUID(), workspace, docId, "customer", "1.0",
                    ValidationTask.Status.NEW, now, null)
                task1 shouldNotBe task2
            }
            it("엣지 케이스(자기 자신, null, 다른 타입)를 올바르게 처리한다") {
                val task = ValidationTask(id, workspace, docId, "customer", "1.0",
                    ValidationTask.Status.NEW, now, null)
                (task == task) shouldBe true
                (task.equals(null)) shouldBe false
                (task.equals("some string")) shouldBe false
            }
        }

        describe("Status enum은") {
            it("NEW와 PROCESSING은 비완료 상태이다") {
                ValidationTask.Status.NEW.isTerminal() shouldBe false
                ValidationTask.Status.PROCESSING.isTerminal() shouldBe false
            }
            it("DONE과 FAILED는 완료 상태이다") {
                ValidationTask.Status.DONE.isTerminal() shouldBe true
                ValidationTask.Status.FAILED.isTerminal() shouldBe true
            }
        }
    }
})
