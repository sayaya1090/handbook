package dev.sayaya.handbook.interfaces.llm

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * OpenAI API 연결 설정.
 */
@ConfigurationProperties(prefix = "openai")
data class LlmConfig(
    val apiKey: String = "",
    val model: String = "gpt-4o",
    val baseUrl: String = "https://api.openai.com/v1",
)
