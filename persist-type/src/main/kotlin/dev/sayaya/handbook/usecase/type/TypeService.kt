package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@Service
class TypeService(
    private val repo: TypeRepository,
    private val eventHandler: ExternalServiceHandler,
) {
    /*
     * 저장 Case
     * 1. 새로운 타입 생성            -> 새로운 레이아웃 생성
     * 2. 기존 타입 수정(버전업)       -> 새로운 레이아웃 생성
     * 3. 기존 타입 수정(버전 동일)    -> 기존 레이아웃 수정
     *
     * 저장 프로세스
     * 1. 타입 정보를 저장한다.
     * 2. 전체 Batch의 ID와 버전을 사용하여 레이아웃 UUID를 생성한다.
     * 3. 레이아웃을 조회하고, 레이아웃이 존재하지 않으면 새로운 레이아웃을 생성한다.
     * 4. 타입 레이아웃 정보를 저장한다.
     * 5. 저장된 레이아웃 구간을 확인한다.
     * 5-1. 레이아웃이 모두 오버랩되는 다른 레이아웃
     * 5-1-1. 레이아웃을 삭제한다.
     * 5-1-2. 삭제한 레이아웃이 참조하는 타입이 다른 레이아웃에 포함되는지 확인하고, 그렇지 않을 경우 last=false로 업데이트한다.
     * 5-2. 레이아웃이 일부 오버랩되는 레이아웃의 경우
     * 5-2-1. 오버랩을 유발시키는 타입을 찾아 유효기간을 업데이트한다.
     * 5-2-2. 레이아웃의 유효기간을 재계산하여 업데이트한다.
     */
    fun save(principal: Principal, workspace: UUID, types: List<Type>): Mono<List<Type>> = if (types.isEmpty()) Mono.empty()
    else repo.saveAll(workspace, types).delayUntil {
        eventHandler.publish(principal, workspace, it.associateBy(ExternalService.TypeKey::of))
    }

    fun delete(principal: Principal, workspace: UUID, types: List<Type>): Mono<List<Type>> = if (types.isEmpty()) Mono.empty()
    else repo.deleteAll(workspace, types).delayUntil {
        eventHandler.publish(principal, workspace, it.associateBy(ExternalService.TypeKey::of) { null })
    }
}