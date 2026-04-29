package dev.sayaya.handbook.client.onboarding

import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class SectionElementPresenterTest : FunSpec({
    test("mode가 변경되면 뷰의 상태를 active/inactive로 전환한다") {
        val view = mockk<SectionElement>(relaxed = true)
        val modeState = mockk<CreateWorkspaceMode>(relaxed = true)
        val modeName = "CREATE"
        val param = mockk<dev.sayaya.handbook.client.usecase.CreateWorkspaceParam>(relaxed = true)
        
        // modeState 구독을 캡처
        val slot = slot<java.util.function.Consumer<CreateWorkspaceMode.Mode>>()
        every { modeState.subscribe(capture(slot)) } returns mockk()
        every { modeState.getValue() } returns CreateWorkspaceMode.Mode.CREATE

        val presenter = SectionElementPresenter(view, modeName, modeState, param)
        
        // When: 모드 변경 이벤트 발생
        slot.captured.accept(CreateWorkspaceMode.Mode.CREATE)

        
        // Then: 뷰는 active 되어야 함
        verify { view.setActive(true) }
        
        // When: 모드 변경(다른 모드) 발생
        slot.captured.accept(CreateWorkspaceMode.Mode.JOIN)

        
        // Then: 뷰는 inactive 되어야 함
        verify { view.setActive(false) }
        verify { view.clearInput() }
    }
})
