package dev.sayaya.handbook.client.onboarding

import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam
import dev.sayaya.handbook.domain.Labels
import dev.sayaya.handbook.usecase.LabelProvider
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class DialogElementPresenterTest : FunSpec({
    test("라벨이 구독되면 뷰의 텍스트가 갱신된다") {
        val view = mockk<DialogElement>(relaxed = true)
        val builder = mockk<SectionBuilder>(relaxed = true)
        val modeState = mockk<CreateWorkspaceMode>(relaxed = true)
        val param = mockk<CreateWorkspaceParam>(relaxed = true)
        val labelProvider = mockk<LabelProvider>(relaxed = true)

        val slot = slot<java.util.function.Consumer<Labels>>()
        every { labelProvider.subscribe(capture(slot)) } returns mockk()

        val presenter = DialogElementPresenter(view, builder, modeState, param, labelProvider)
        
        // When: 라벨 이벤트 발행
        val mockLabels = mockk<Labels>(relaxed = true)
        every { mockLabels.getOrDefault(any(), any()) } answers { secondArg() }
        slot.captured.accept(mockLabels)

        verify { view.setTitle(any()) }
        verify { view.setSubtitle(any()) }
    }
})
