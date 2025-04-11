package dev.sayaya.handbook.`interface`.k8s

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(ServiceListProperties::class)
class ServiceBeanConfig(private val client: WebClient.Builder) {
    @Bean fun serviceBeans(config: ServiceListProperties): List<ServiceDiscovery> = config.map { ServiceDiscovery(client, it.name) }
}