package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.usecase.MenuService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/**
 * 메뉴 집계 API 컨트롤러.
 * 등록된 모든 서비스로부터 메뉴를 수집하여 반환한다.
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
