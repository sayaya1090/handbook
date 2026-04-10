package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.security.Principal

/**
 * 로그인 모듈의 메뉴 제공 컨트롤러.
 *
 * **책임:** 인증 상태에 따라 Sign In 또는 Sign Out 메뉴를 반환한다.
 * gateway의 [MenuController][dev.sayaya.handbook.interfaces.api.MenuController]가 이 엔드포인트를 호출하여 메뉴를 집계한다.
 *
 * **주의:** principal이 null이면 미인증 상태로 Sign In 메뉴를, 인증 상태면 Sign Out 메뉴를 반환한다.
 */
@RestController
class MenuController {
    companion object {
        val SIGN_IN: Menu = Menu.builder()
            .title("sign in")
            .order("Z")
            .icon("fa-right-to-bracket")
            .iconType("sharp")
            .script("js/login.nocache.js")
            .bottom(true)
            .build()

        val SIGN_OUT: Menu = Menu.builder()
            .title("sign out")
            .order("Z")
            .icon("fa-right-from-bracket")
            .iconType("sharp")
            .script("js/logout.nocache.js")
            .bottom(true)
            .build()
    }

    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?): Flux<Menu> {
        return if (principal == null) Flux.just(SIGN_IN)
        else Flux.just(SIGN_OUT)
    }
}
