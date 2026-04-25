package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec

@GwtHtml("activityTest.html")
internal class MenuTest: GwtTestSpec({
    Given("a") {
        When("b") {
            Then("c") {
                page shouldContainLog "DD"
            }
        }
    }
})