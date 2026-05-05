package dev.sayaya.handbook.interfaces.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import tools.jackson.databind.json.JsonMapper

@JsonTest
class JacksonSerializationTest {
    @Autowired
    lateinit var mapper: JsonMapper

    @Test
    fun `Menu can be serialized`() {
        val menu = MenuController.ONBOARDING_MENU
        val json = mapper.writeValueAsString(menu)
        println("Serialized Menu JSON: " + json)
    }
}