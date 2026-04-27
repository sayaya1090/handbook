package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Tool
import dev.sayaya.rx.subject.BehaviorSubject
import io.mockk.*
import io.kotest.core.spec.style.DescribeSpec
import java.util.function.Consumer

class ToolProviderTest : DescribeSpec({
    val mockTools = mockk<BehaviorSubject<Array<Tool>>>(relaxed = true)
    val mockSelected = mockk<BehaviorSubject<String>>(relaxed = true)

    beforeSpec {
        mockkStatic(WindowToolPublisherBridge::class)
        mockkStatic(WindowToolSubscriberBridge::class)
        mockkStatic(BehaviorSubject::class)
        
        every { BehaviorSubject.behavior<Array<Tool>>(any()) } returns mockTools
        every { BehaviorSubject.behavior<String>(any()) } returns mockSelected
    }

    afterSpec {
        unmockkAll()
    }

    describe("ToolProvider") {
        it("자식 모듈: publish 호출 시 PublisherBridge 를 통해 도구 목록을 발행한다") {
            every { WindowToolPublisherBridge.publish(any()) } returns Unit
            val provider = ToolProvider()
            val tools = arrayOf<Tool>()
            
            provider.publish(tools)
            
            verify { WindowToolPublisherBridge.publish(any()) }
        }

        it("쉘 UI: select 호출 시 SubscriberBridge 를 통해 도구 선택 이벤트를 전달한다") {
            every { WindowToolSubscriberBridge.select(any()) } returns Unit
            val provider = ToolProvider()
            val toolId = "test-tool"
            
            provider.select(toolId)
            
            verify { WindowToolSubscriberBridge.select(toolId) }
        }
    }
})
