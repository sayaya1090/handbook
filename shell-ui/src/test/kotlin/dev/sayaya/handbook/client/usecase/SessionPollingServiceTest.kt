package dev.sayaya.handbook.client.usecase

import dev.sayaya.handbook.client.components.ToastContainer
import dev.sayaya.handbook.domain.Labels
import dev.sayaya.handbook.domain.ToastLevel
import dev.sayaya.handbook.usecase.LabelProvider
import io.kotest.core.spec.style.FunSpec
import io.mockk.*

class SessionPollingServiceTest : FunSpec({
    
    lateinit var authRepository: AuthRepository
    lateinit var toastContainer: ToastContainer
    lateinit var labelProvider: LabelProvider
    lateinit var env: SessionEnvironment
    lateinit var service: SessionPollingService

    beforeTest {
        authRepository = mockk(relaxed = true)
        toastContainer = mockk(relaxed = true)
        labelProvider = mockk(relaxed = true)
        env = mockk(relaxed = true)
        
        service = SessionPollingService(authRepository, toastContainer, labelProvider, env)
    }

    test("토큰이 없으면 로그인 페이지로 리다이렉트한다") {
        every { env.getCookies() } returns ""
        
        val method = SessionPollingService::class.java.getDeclaredMethod("checkSession")
        method.isAccessible = true
        method.invoke(service)
        
        verify { env.redirect("/auth/login") }
    }

    test("토큰이 만료되었으면 로그인 페이지로 리다이렉트한다") {
        every { env.getCookies() } returns "token=abc.def.ghi"
        
        val nowSec = System.currentTimeMillis() / 1000.0
        every { env.getJwtClaimAsDouble("abc.def.ghi", "exp") } returns nowSec - 100.0 // 100초 지남
        
        val method = SessionPollingService::class.java.getDeclaredMethod("checkSession")
        method.isAccessible = true
        method.invoke(service)
        
        verify { env.redirect("/auth/login") }
    }

    test("토큰 만료가 임박(5분 이내)하면 경고 토스트를 표시한다") {
        every { env.getCookies() } returns "token=abc.def.ghi"
        
        val nowSec = System.currentTimeMillis() / 1000.0
        every { env.getJwtClaimAsDouble("abc.def.ghi", "exp") } returns nowSec + 240.0 // 4분 남음
        every { env.getJwtClaimAsDouble("abc.def.ghi", "iat") } returns nowSec - 3360.0 // 56분 전 발급
        
        val method = SessionPollingService::class.java.getDeclaredMethod("checkSession")
        method.isAccessible = true
        method.invoke(service)
        
        verify { toastContainer.show(ToastLevel.WARNING, any(), any()) }
    }

    test("토큰 수명의 80%가 지났으면 토큰을 갱신한다") {
        every { env.getCookies() } returns "token=abc.def.ghi"
        
        val nowSec = System.currentTimeMillis() / 1000.0
        every { env.getJwtClaimAsDouble("abc.def.ghi", "exp") } returns nowSec + 600.0 // 10분 남음 (전체 60분 중 83% 경과)
        every { env.getJwtClaimAsDouble("abc.def.ghi", "iat") } returns nowSec - 3000.0 // 50분 전 발급
        
        val obs = mockk<dev.sayaya.rx.Observable<Boolean>>(relaxed = true)
        every { authRepository.refresh() } returns obs
        
        val method = SessionPollingService::class.java.getDeclaredMethod("checkSession")
        method.isAccessible = true
        method.invoke(service)
        
        verify { authRepository.refresh() }
    }
})
