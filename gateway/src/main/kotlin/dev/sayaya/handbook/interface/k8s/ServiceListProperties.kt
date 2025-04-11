package dev.sayaya.handbook.`interface`.k8s

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "services")
class ServiceListProperties: ArrayList<ServiceProperty>()