package dev.sayaya.handbook.interfaces.k8s

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import jakarta.annotation.PreDestroy
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.*
import kotlin.random.Random

@TestConfiguration
internal class KubernetesMockConfiguration {
    private val server = KubernetesServer(true, true).apply { before() }
    @Bean fun kubernetesClient(): KubernetesClient = server.client
    @PreDestroy
    fun tearDown() {
        server.after()
        println("🧹 KubernetesServer shutdown 완료")
    }
    companion object {
        fun KubernetesClient.createServiceAndPod(namespace: String, name: String, serviceIp: String, labels: Map<String, String> = emptyMap(), instances: Int = 1) {
            createService(namespace, name, serviceIp, labels)
            createEndpoint(namespace, name, labels) { ep ->
                for(i in 1..instances) {
                    val podName = "$name-${i.toString().padStart(2, '0')}"
                    val ip = "10.10.${100 + Random.nextInt(150)}.${i+1}"
                    ep.addNewAddress()
                        .withIp(ip)
                        .withNodeName("node1")
                        .withNewTargetRef()
                        .withKind("Pod")
                        .withNamespace(namespace)
                        .withName(podName)
                        .withUid(UUID.randomUUID().toString())
                        .endTargetRef()
                        .endAddress()
                }
            }
        }
        private fun KubernetesClient.createService(namespace: String, name: String, serviceIp: String, labels: Map<String, String> = emptyMap()): Service = services().inNamespace(namespace)
            .resource(
                ServiceBuilder()
                    .withNewMetadata()
                    .withName(name)
                    .withNamespace(namespace)
                    .withLabels<String, String>(labels)
                    .endMetadata()
                    .withNewSpec()
                    .withClusterIP(serviceIp)
                    .addToSelector("app", name)
                    .withType("ClusterIP")
                    .withPorts(
                        ServicePortBuilder()
                            .withNewTargetPort(8080)
                            .withPort(80)
                            .withProtocol("TCP")
                            .build())
                    .endSpec()
                    .build())
            .create()
        private fun KubernetesClient.createEndpoint(namespace: String, name: String, labels: Map<String, String> = emptyMap(), addrs: (EndpointSubsetFluent<*>) -> Unit) {
            val subset = EndpointsBuilder()
                .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .withLabels<String, String>(labels)
                .endMetadata()
                .addNewSubset()
                .withPorts()
                .addNewPort()
                .withPort(8080)
                .withProtocol("TCP")
                .endPort()
            addrs.invoke(subset)
            endpoints().inNamespace(namespace).resource(subset.endSubset().build()).create()
        }
    }
}