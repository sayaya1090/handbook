package dev.sayaya.handbook.interfaces.k8s

import io.fabric8.kubernetes.client.KubernetesClient
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient

@SpringBootTest(properties = [
    "spring.main.cloud-platform=KUBERNETES",
    "spring.cloud.kubernetes.discovery.service-labels.type=service",
    "spring.cloud.kubernetes.discovery.namespaces=test"
], classes=[ KubernetesMockConfiguration::class ])
@EnableDiscoveryClient
internal class K8sServiceDiscoveryTest (
    private val discoveryClient: ReactiveDiscoveryClient,
    private val k8s: KubernetesClient
): BehaviorSpec({
    with(KubernetesMockConfiguration) {
        extensions(SpringExtension)
        Given("지정된 네임스페이스 내에 지정된 타입의 레이블을 가진 N개의 service가 배포된 k8s 내에서") {
            k8s.createServiceAndPod("test", "svc1", "172.30.1.1", mapOf("type" to "service"), 3)
            k8s.createServiceAndPod("test", "svc2", "172.30.1.2", mapOf("type" to "service"))
            k8s.createServiceAndPod("test", "svc-no-pod", "172.30.1.3", mapOf("type" to "service"), 0)
            k8s.createServiceAndPod("test", "svc4", "172.30.1.4", mapOf("type" to "actuator"), 2)
            k8s.createServiceAndPod("test", "svc5", "172.30.1.5")
            k8s.createServiceAndPod("other", "svc6", "172.30.1.6", mapOf("type" to "service"))
            When("서비스 목록을 요청하면") {
                val services = discoveryClient.services.collectList()
                Then("N개의 목록이 반환된다.") {
                    services.block() shouldContainOnly listOf("svc1", "svc2", "svc-no-pod")
                }
                When("파드가 존재하는 서비스의 인스턴스를 요청하면") {
                    val instances = discoveryClient.getInstances("svc1").collectList()
                    Then("인스턴스가 반환된다.") {
                        val list = instances.block()
                        list shouldNotBe null
                        list!!.size shouldBeGreaterThan 0
                    }
                }
                When("파드가 존재하지 않는 서비스의 인스턴스를 요청하면") {
                    val instances = discoveryClient.getInstances("svc-no-pod").collectList()
                    Then("빈 목록이 제공된다.") {
                        val list = instances.block()
                        list shouldNotBe null
                        list!!.size shouldBeEqual 0
                    }
                }
            }
            When("지정된 타입의 레이블을 가진 서비스가 추가되면") {
                k8s.createServiceAndPod("test", "svc-addendum", "172.30.1.7", mapOf("type" to "service"))
                val services = discoveryClient.services.collectList()
                Then("목록이 늘어난다.") {
                    services.block() shouldContainOnly listOf("svc1", "svc2", "svc-no-pod", "svc-addendum")
                }
            }
            When("지정된 타입의 레이블을 가지지 않은 서비스가 추가되면") {
                k8s.createServiceAndPod("test", "svc-else", "172.30.1.8", mapOf("type" to "other"))
                k8s.createServiceAndPod("test", "svc-else2", "172.30.1.9", mapOf("test" to "service"))
                val services = discoveryClient.services.collectList()
                Then("목록에 반영되지 않는다.") {
                    services.block()!! shouldNotContain "svc-else"
                    services.block()!! shouldNotContain "svc-else2"
                }
            }
            When("서비스가 삭제되면") {
                k8s.services().withName("svc1").delete()
                val services = discoveryClient.services.collectList()
                Then("목록에 반영된다.") {
                    services.block() shouldContainOnly listOf("svc2", "svc-no-pod", "svc-addendum")
                }
            }
            When("파드와 연결이 끊기면") {
                k8s.endpoints().inNamespace("test").withName("svc2").edit { ep ->
                    ep.edit().editFirstSubset().removeAllFromAddresses(ep.subsets.first().addresses).endSubset().build()
                }
                val services = discoveryClient.services.collectList()
                Then("서비스 목록은 동일하게 리턴된다.") {
                    services.block() shouldContainOnly listOf("svc2", "svc-no-pod", "svc-addendum")
                }
                When("파드와 연결이 끊긴 서비스의 인스턴스를 요청하면") {
                    val instances = discoveryClient.getInstances("svc2").collectList()
                    Then("빈 목록이 제공된다.") {
                        val list = instances.block()
                        list shouldNotBe null
                        list!!.size shouldBeEqual 0
                    }
                }
            }
        }
    }
})