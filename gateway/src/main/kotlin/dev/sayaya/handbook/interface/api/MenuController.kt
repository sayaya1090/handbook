package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.usecase.MenuService
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
class MenuController(private val svc: MenuService) {
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(request: ServerHttpRequest): Flux<Menu> = svc.menus(request.headers)
}