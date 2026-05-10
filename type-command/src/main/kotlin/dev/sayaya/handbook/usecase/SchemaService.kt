package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.SchemaPatch
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 스키마(타입 및 레이아웃) 원자적 패치 비즈니스 로직.
 * 
 * **책임:** 여러 건의 타입 생성/수정/삭제와 레이아웃 변경을 단일 트랜잭션 내에서 처리한다.
 */
class SchemaService(
    private val typeRepository: TypeRepository,
    private val layoutRepository: LayoutRepository,
    private val eventPublisher: TypeEventPublisher,
    private val tx: TransactionalOperator,
) {
    fun patch(workspace: UUID, patch: SchemaPatch): Mono<Void> {
        val typeOps = Flux.fromArray(patch.types() ?: emptyArray())
            .concatMap { op ->
                when (op.op()) {
                    "UPSERT" -> typeRepository.save(workspace, listOf(op.data()))
                        .doOnNext { eventPublisher.publishCreated(workspace, it) }
                    "DELETE" -> typeRepository.delete(workspace, listOf(op.data()))
                        .doOnSuccess { eventPublisher.publishDeleted(workspace, op.data()) }
                    else -> Mono.empty()
                }
            }
            .then()
            
        val layoutOps = Flux.fromArray(patch.layouts() ?: emptyArray())
            .concatMap { op ->
                when (op.op()) {
                    "UPSERT" -> layoutRepository.save(workspace, op.data())
                    else -> Mono.empty()
                }
            }
            .then()
            
        return typeOps.then(layoutOps).`as`(tx::transactional)
    }
}
