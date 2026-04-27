package dev.sayaya.handbook.usecase

import io.mockk.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jsinterop.base.Js
import jsinterop.base.JsPropertyMap
import elemental2.dom.DomGlobal

class WindowToolBridgeTest : DescribeSpec({
    beforeTest {
        mockkStatic(Js::class)
        mockkStatic(DomGlobal::class)
    }

    afterTest {
        unmockkAll()
    }

    describe("WindowToolPublisherBridge") {
        it("쉘(Host) 측: 콜백을 window.__handbook_tool_publisher 에 등록한다") {
            val win = mockk<JsPropertyMap<Any?>>()
            every { Js.asPropertyMap(any()) } returns win
            val callback = mockk<WindowToolPublisherBridge.PublisherFn>()
            
            every { win.set(any(), any()) } returns Unit
            WindowToolPublisherBridge.register(callback)
            
            verify { win.set("__handbook_tool_publisher", callback) }
        }

        it("자식(Child) 측: window.__handbook_tool_publisher 를 호출하여 도구 목록을 전달한다") {
            val win = mockk<JsPropertyMap<Any?>>()
            every { Js.asPropertyMap(any()) } returns win
            val callback = mockk<WindowToolPublisherBridge.PublisherFn>()
            val tools = arrayOf<Any>()
            
            every { win.has("__handbook_tool_publisher") } returns true
            every { win.get("__handbook_tool_publisher") } returns callback
            every { callback.call(any()) } returns Unit
            
            WindowToolPublisherBridge.publish(tools)
            
            verify { callback.call(tools) }
        }
    }

    describe("WindowToolSubscriberBridge") {
        it("자식(Child) 측: 콜백을 window.__handbook_tool_subscriber 에 등록한다") {
            val win = mockk<JsPropertyMap<Any?>>()
            every { Js.asPropertyMap(any()) } returns win
            val callback = mockk<WindowToolSubscriberBridge.SubscriberFn>()
            
            every { win.set(any(), any()) } returns Unit
            WindowToolSubscriberBridge.register(callback)
            
            verify { win.set("__handbook_tool_subscriber", callback) }
        }

        it("쉘(Host) 측: window.__handbook_tool_subscriber 를 호출하여 선택된 도구 ID를 전달한다") {
            val win = mockk<JsPropertyMap<Any?>>()
            every { Js.asPropertyMap(any()) } returns win
            val callback = mockk<WindowToolSubscriberBridge.SubscriberFn>()
            val toolId = "test-tool"
            
            every { win.has("__handbook_tool_subscriber") } returns true
            every { win.get("__handbook_tool_subscriber") } returns callback
            every { callback.call(any()) } returns Unit
            
            WindowToolSubscriberBridge.select(toolId)
            
            verify { callback.call(toolId) }
        }
    }
})
