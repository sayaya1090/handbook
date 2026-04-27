package dev.sayaya.handbook.client.usecase

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class PlaceholderResolverTest : DescribeSpec({
    val context = mockk<SessionContext>()
    val resolver = PlaceholderResolver(context)
    
    describe("PlaceholderResolver는") {
        it("예약어를 컨텍스트 값으로 치환한다") {
            every { context.getAll() } returns mapOf("workspaceId" to "abc-123")
            
            val result = resolver.resolve("/workspace/{workspaceId}/type")
            result shouldBe "/workspace/abc-123/type"
        }
        
        it("여러 개의 예약어를 치환한다") {
            every { context.getAll() } returns mapOf("workspaceId" to "abc-123", "userId" to "user-456")
            
            val result = resolver.resolve("/workspace/{workspaceId}/user/{userId}")
            result shouldBe "/workspace/abc-123/user/user-456"
        }
        
        it("값이 없는 예약어는 치환하지 않는다") {
            every { context.getAll() } returns emptyMap()
            
            val result = resolver.resolve("/workspace/{workspaceId}/type")
            result shouldBe "/workspace/{workspaceId}/type"
        }
        
        it("null 입력에 대해 null을 반환한다") {
            val result = resolver.resolve(null)
            result shouldBe null
        }
    }
})
