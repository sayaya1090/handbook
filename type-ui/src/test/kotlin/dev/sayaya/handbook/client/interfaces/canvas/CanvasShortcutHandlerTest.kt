package dev.sayaya.handbook.client.interfaces.canvas

import dev.sayaya.handbook.client.components.ActionManager
import dev.sayaya.handbook.client.components.ChangeTracker
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement
import dev.sayaya.handbook.client.usecase.GridSnap
import dev.sayaya.handbook.client.usecase.LayoutProvider
import dev.sayaya.handbook.client.usecase.PositionMap
import dev.sayaya.handbook.client.usecase.TypeList
import dev.sayaya.handbook.client.usecase.TypeSearchProvider
import dev.sayaya.handbook.client.usecase.action.ComplexAction
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction
import dev.sayaya.handbook.domain.Type
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*

class CanvasShortcutHandlerTest : BehaviorSpec({
    val actionManager = mockk<ActionManager>(relaxed = true)
    val typeList = mockk<TypeList>(relaxed = true)
    val typeSearchProvider = mockk<TypeSearchProvider>(relaxed = true)
    val selection = mockk<SelectedBoxElement>(relaxed = true)
    val positionMap = mockk<PositionMap>(relaxed = true)
    val tracker = mockk<ChangeTracker>(relaxed = true)
    val gridSnap = mockk<GridSnap>(relaxed = true)
    val layoutProvider = mockk<LayoutProvider>(relaxed = true)

    val handler = CanvasShortcutHandler(
        actionManager, typeList, typeSearchProvider, selection, positionMap, tracker, gridSnap, layoutProvider
    )

    Given("CanvasShortcutHandler가 초기화됨") {
        
        When("Ctrl+Z를 누르면") {
            val event = mockk<CanvasShortcutHandler.KeyboardInput>(relaxed = true)
            every { event.isCtrl } returns true
            every { event.key } returns "z"
            every { event.isShift } returns false
            
            handler.handle(event)
            
            Then("ActionManager.undo()가 호출된다") {
                verify { actionManager.undo() }
            }
        }

        When("Ctrl+Shift+Z를 누르면") {
            val event = mockk<CanvasShortcutHandler.KeyboardInput>(relaxed = true)
            every { event.isCtrl } returns true
            every { event.key } returns "z"
            every { event.isShift } returns true
            
            handler.handle(event)
            
            Then("ActionManager.redo()가 호출된다") {
                verify { actionManager.redo() }
            }
        }

        When("Ctrl+A를 누르면") {
            val event = mockk<CanvasShortcutHandler.KeyboardInput>(relaxed = true)
            every { event.isCtrl } returns true
            every { event.key } returns "a"
            
            val type1 = mockk<Type>()
            every { type1.key() } returns "t1"
            every { typeSearchProvider.visibleTypes } returns setOf(type1)
            
            handler.handle(event)
            
            Then("모든 가시적인 타입이 선택된다") {
                verify { selection.selectAll(setOf("t1")) }
            }
        }

        When("Delete 키를 누르면") {
            val event = mockk<CanvasShortcutHandler.KeyboardInput>(relaxed = true)
            every { event.key } returns "Delete"
            
            val type1 = mockk<Type>()
            every { type1.key() } returns "t1"
            every { selection.value } returns setOf("t1")
            every { typeSearchProvider.visibleTypes } returns setOf(type1)
            
            handler.handle(event)
            
            Then("선택된 타입 박스 삭제 액션이 실행된다") {
                verify { actionManager.execute(any<DeleteBoxAction>()) }
                verify { selection.clear() }
            }
        }

        When("방향키(Right)를 누르면") {
            val event = mockk<CanvasShortcutHandler.KeyboardInput>(relaxed = true)
            every { event.key } returns "ArrowRight"
            every { selection.value } returns setOf("t1")
            every { gridSnap.isEnabled } returns false
            
            handler.handle(event)
            
            Then("이동 액션(ComplexAction)이 실행된다") {
                verify { actionManager.execute(any<ComplexAction>()) }
            }
        }
    }
})
