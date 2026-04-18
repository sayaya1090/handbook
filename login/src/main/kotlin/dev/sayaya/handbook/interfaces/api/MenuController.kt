package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.SessionStateKind
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
        // title 은 표시 리터럴이 아닌 i18n 키. shell-ui 의 LabelProvider 가 language.{locale}.json
        // 의 매핑으로 해석. docs/contracts/menus.md 의 "title i18n 키 규약" 참조.
        // allowedSessionStates 는 계층 추론 없는 명시 집합. docs/contracts/menus.md §allowedSessionStates 규약.
        // Sign In: 비로그인(ANONYMOUS) 사용자만 노출.
        // Sign Out: 로그인된 사용자(AUTHENTICATED, IN_WORKSPACE)만 노출 — 두 상태 반드시 열거.
        val SIGN_IN: Menu = Menu.builder()
            .title("login.sign_in")
            .order("Z")
            .icon("fa-right-to-bracket")
            .iconType("solid")
            .script("js/login/login.nocache.js")
            .bottom(true)
            .appBarSlot("trailing")
            .allowedSessionStates(SessionStateKind.ANONYMOUS)
            .build()

        val SIGN_OUT: Menu = Menu.builder()
            .title("login.sign_out")
            .order("Z")
            .icon("fa-right-from-bracket")
            .iconType("solid")
            .script("js/logout/logout.nocache.js")
            .bottom(true)
            .appBarSlot("trailing")
            .allowedSessionStates(SessionStateKind.AUTHENTICATED, SessionStateKind.IN_WORKSPACE)
            .build()
    }

    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?): Flux<Menu> {
        return if (principal == null) Flux.just(SIGN_IN)
        else Flux.just(SIGN_OUT)
    }
}
