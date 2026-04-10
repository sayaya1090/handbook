package dev.sayaya.handbook.test

import com.microsoft.playwright.Page

/**
 * MD3 웹 컴포넌트 Playwright 테스트 유틸리티.
 *
 * sayaya-ui MD3 컴포넌트는 Shadow DOM을 사용하므로 Playwright의
 * 기본 fill/check API가 동작하지 않는다. 이 유틸리티는 JavaScript evaluate를
 * 내부적으로 사용하되, 호출 측에서는 간결한 API를 제공한다.
 */
object Md3 {

    // ── md-outlined-text-field ──

    /** MD3 TextField에 값을 설정하고 input 이벤트를 발행한다. */
    fun Page.fillTextField(selector: String, value: String) {
        evaluate("(s => { const el=document.querySelector(s); el.value='$value'; el.dispatchEvent(new Event('input',{bubbles:true})); })('$selector')")
    }

    /** 부모의 n번째 자식 안의 MD3 TextField에 값을 설정한다. */
    fun Page.fillTextFieldIn(parentSelector: String, index: Int, childSelector: String, value: String) {
        evaluate("(()=>{ const p=document.querySelectorAll('$parentSelector')[$index]; const el=p.querySelector('$childSelector'); el.value='$value'; el.dispatchEvent(new Event('input',{bubbles:true})); })()")
    }

    /** MD3 TextField의 현재 값을 반환한다. */
    fun Page.textFieldValue(selector: String): String =
        evaluate("document.querySelector('$selector')?.value||''") as String

    // ── md-radio ──

    /** MD3 Radio의 선택 상태를 반환한다. */
    fun Page.isRadioChecked(selector: String): Boolean =
        evaluate("document.querySelector('$selector')?.checked===true") as Boolean

    // ── md-checkbox ──

    /** MD3 Checkbox의 선택 상태를 반환한다. */
    fun Page.isCheckboxChecked(selector: String): Boolean =
        evaluate("document.querySelector('$selector')?.checked===true") as Boolean

    // ── md-outlined-select ──

    /** MD3 Select의 현재 값을 반환한다. */
    fun Page.selectValue(selector: String): String =
        evaluate("document.querySelector('$selector')?.value||''") as String

    /** MD3 Select에서 값을 선택한다. */
    fun Page.selectOption(selector: String, value: String) {
        evaluate("document.querySelector('$selector')?.select?.('$value')")
    }

    // ── 버튼 ──

    /** 버튼의 disabled 상태를 반환한다. (md-filled-button, md-outlined-button 등) */
    fun Page.checkDisabled(selector: String): Boolean =
        evaluate("(()=>{ const el=document.querySelector('$selector'); return el?.disabled===true||el?.hasAttribute('disabled')===true; })()") as Boolean
}
