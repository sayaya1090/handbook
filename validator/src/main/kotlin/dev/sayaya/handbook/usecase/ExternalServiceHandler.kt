package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.security.Principal
import java.time.Duration
import java.util.UUID

@Service
class ExternalServiceHandler(private val externals: List<ExternalService>) {
    fun publish(workspace: UUID, document: Document): Mono<ExternalPublishResult> = Flux.fromIterable(externals).flatMap {
        it.publish(workspace, document)
          .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // 최대 3회, 1초 간격
          .onErrorResume { ex ->
              // 실패한 외부 서비스 처리 (로그 기록 또는 무시 가능)
              // log.error("Failed to publish to external service: ${external.javaClass.name}", ex)
              Mono.empty() // 오류 복구
          }
    }.then(Mono.fromCallable {
        ExternalPublishResult(
            workspace = workspace,
            success = true, // 필요한 경우 정의
            failedServices = listOf() // 실패한 externals 이름 추가
        )
    })
    companion object {
        data class ExternalPublishResult (
            val workspace: UUID,
            val success: Boolean,
            val failedServices: Collection<String>
        )
    }
}