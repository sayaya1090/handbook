package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.SchedulerConfig
import dev.sayaya.handbook.domain.event.TypeEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.scheduler.Scheduler

@Service
class ValidationRequestService(
    private val repo: DocumentRepository,
    private val tasks: ValidationTaskRepository,
    private val queue: TaskQueue,
    @Qualifier(SchedulerConfig.VIRTUAL_THREAD_SCHEDULER_BEAN_NAME) private val virtualThreadScheduler: Scheduler
) {
    fun request(event: TypeEvent): Disposable = repo.findByType(event.workspace, event.param.id, event.param.effectDateTime, event.param.expireDateTime)
        .collectList()
        .delayUntil { tasks.expire(event.workspace, it) }
        .flatMapMany { Flux.fromIterable(it) }
        .parallel().runOn(virtualThreadScheduler)
        .flatMap { document ->
            queue.publish(event.workspace, document)
        }.then().subscribe()
}