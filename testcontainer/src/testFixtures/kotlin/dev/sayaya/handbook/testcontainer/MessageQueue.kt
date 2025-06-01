package dev.sayaya.handbook.testcontainer

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.kafka.KafkaContainer

class MessageQueue {
    private val kafka = KafkaContainer("apache/kafka-native:3.8.0")
    init {
        kafka.start()
    }
    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers") { kafka.bootstrapServers }
        registry.add("spring.cloud.stream.kafka.binder.auto-add-partitions") { true }
    }
}