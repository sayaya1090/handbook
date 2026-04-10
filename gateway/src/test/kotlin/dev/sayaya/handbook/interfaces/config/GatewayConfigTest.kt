package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.discovery.ServiceListProperties
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class GatewayConfigTest : DescribeSpec({
    val config = GatewayConfig()

    describe("GatewayConfig는") {
        it("ObjectMapper를 생성한다") {
            config.objectMapper() shouldNotBe null
        }
        it("WebClient.Builder를 생성한다") {
            config.webClientBuilder(config.objectMapper()) shouldNotBe null
        }
        it("서비스 목록으로부터 MenuSupplier를 생성한다") {
            val props = ServiceListProperties()
            props.add(ServiceListProperties.ServiceEntry("svc1"))
            props.add(ServiceListProperties.ServiceEntry("svc2"))
            val suppliers = config.menuSuppliers(config.webClientBuilder(config.objectMapper()), props)
            suppliers.size shouldBe 2
        }
        it("빈 서비스 목록이면 빈 MenuSupplier 리스트를 반환한다") {
            val suppliers = config.menuSuppliers(config.webClientBuilder(config.objectMapper()), ServiceListProperties())
            suppliers.size shouldBe 0
        }
        it("MenuService를 생성한다") {
            config.menuService(emptyList()) shouldNotBe null
        }
    }
})
