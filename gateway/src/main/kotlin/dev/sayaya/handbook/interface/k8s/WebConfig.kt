package dev.sayaya.handbook.`interface`.k8s

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebConfig {
    @Bean fun client(): WebClient.Builder = WebClient.builder()
}