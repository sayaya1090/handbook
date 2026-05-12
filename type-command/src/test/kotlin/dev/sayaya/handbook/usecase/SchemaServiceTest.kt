package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.SchemaPatch
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypeLayout
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

class SchemaServiceTest : BehaviorSpec({
    val typeRepo = mockk<TypeRepository>()
    val layoutRepo = mockk<LayoutRepository>()
    val eventPublisher = mockk<TypeEventPublisher>(relaxed = true)
    
    val tx = object : TransactionalOperator {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> transactional(flux: Flux<T>): Flux<T> = flux
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> transactional(mono: Mono<T>): Mono<T> = mono
        override fun <T : Any> execute(action: org.springframework.transaction.reactive.TransactionCallback<T>): Flux<T> = error("Not needed")
    }
    
    val service = SchemaService(typeRepo, layoutRepo, eventPublisher, tx)
    val workspace = UUID.randomUUID()

    Given("스키마 일괄 패치 요청이 주어졌을 때") {
        val type1 = Type.create("customer", "1.1", 100.0, 200.0)
        val type2 = Type.create("order", "1.0", 100.0, 200.0)
        val layout = TypeLayout.create("l1", workspace.toString(), 100.0, 200.0, null)
        
        val patch = SchemaPatch.create(
            arrayOf(
                SchemaPatch.TypeOperation.upsert(type1),
                SchemaPatch.TypeOperation.delete(type2)
            ),
            arrayOf(
                SchemaPatch.LayoutOperation.upsert(layout)
            )
        )

        When("patch를 호출하면") {
            clearMocks(typeRepo, layoutRepo, eventPublisher)
            every { typeRepo.save(any(), any()) } returns Flux.just(type1)
            every { typeRepo.delete(any(), any()) } returns Mono.empty()
            every { layoutRepo.save(any(), any()) } returns Mono.just(layout)
            
            val result = service.patch(workspace, patch)

            Then("모든 작업이 성공하고 이벤트가 발행된다") {
                StepVerifier.create(result)
                    .assertNext { r ->
                        r.types().size shouldBe 2
                        r.layouts().size shouldBe 1
                        r.types()[0].data().id() shouldBe "customer"
                    }
                    .verifyComplete()
                
                verifyOrder {
                    typeRepo.save(workspace, any())
                    typeRepo.delete(workspace, any())
                    layoutRepo.save(workspace, any())
                }
                verify { eventPublisher.publishCreated(workspace, any()) }
                verify { eventPublisher.publishDeleted(workspace, any()) }
            }
        }
    }

    Given("작업 중 하나가 실패하는 경우") {
        val type1 = Type.create("customer", "1.1", 100.0, 200.0)
        val patch = SchemaPatch.create(
            arrayOf(SchemaPatch.TypeOperation.upsert(type1)),
            emptyArray()
        )

        When("patch를 호출하면") {
            clearMocks(typeRepo, layoutRepo, eventPublisher)
            every { typeRepo.save(any(), any()) } returns Flux.error(RuntimeException("DB Error"))
            
            val result = service.patch(workspace, patch)

            Then("에러가 전파된다") {
                StepVerifier.create(result)
                    .expectErrorMessage("DB Error")
                    .verify()
            }
        }
    }

    Given("빈 요청이나 알 수 없는 연산이 포함된 경우") {
        val type = Type.create("t1", "1.0", 0.0, 0.0)
        val layout = TypeLayout.create("l1", workspace.toString(), 0.0, 0.0, null)
        val patch = SchemaPatch.create(
            arrayOf(
                SchemaPatch.TypeOperation.upsert(type).apply { op("UNKNOWN") }
            ),
            arrayOf(
                SchemaPatch.LayoutOperation.upsert(layout).apply { op("DELETE") } // LayoutOperation.delete branch cover
            )
        )

        When("patch를 호출하면") {
            clearMocks(typeRepo, layoutRepo, eventPublisher)
            
            val result = service.patch(workspace, patch)

            Then("에러 없이 완료되며 알려진 연산만 처리된다") {
                StepVerifier.create(result)
                    .assertNext { r ->
                        r.types().size shouldBe 0
                        r.layouts().size shouldBe 0
                    }
                    .verifyComplete()
                
                verify(exactly = 0) { typeRepo.save(any(), any()) }
                verify(exactly = 0) { layoutRepo.save(any(), any()) }
            }
        }
    }

    Given("완전히 비어있는 패치인 경우") {
        val patch = SchemaPatch.create(null, null)

        When("patch를 호출하면") {
            clearMocks(typeRepo, layoutRepo, eventPublisher)
            
            val result = service.patch(workspace, patch)

            Then("정상적으로 즉시 완료된다") {
                StepVerifier.create(result)
                    .assertNext { r ->
                        r.types().size shouldBe 0
                        r.layouts().size shouldBe 0
                    }
                    .verifyComplete()
            }
        }
    }
})
