package dev.sayaya.handbook.`interface`.k8s

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebConfig(om: ObjectMapper) {
    private val strategy = ExchangeStrategies.builder().codecs { configurer ->
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(om))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(om))
    }.build()

    @Bean fun client(): WebClient.Builder = WebClient.builder().exchangeStrategies(strategy)
}