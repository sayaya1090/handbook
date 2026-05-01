package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.usecase.MenuService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

/**
 * 메뉴 집계 API 컨트롤러.
 *
 * **책임:** 등록된 모든 서비스로부터 메뉴를 병렬로 수집하여 정렬 후 반환한다.
 * 클라이언트의 요청 헤더(인증 정보 포함)를 각 서비스에 전달한다.
 *
 * **의존관계:**
 * - [MenuService] — 메뉴 병렬 수집 및 정렬 유스케이스
 */
@RestController
class MenuController(private val menuService: MenuService) {
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(request: ServerHttpRequest): Flux<Menu> =
        menuService.menus(request.headers.toPlainMap())

    private fun HttpHeaders.toPlainMap(): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        this.forEach { key, values -> result[key] = values }
        return result
    }
}
