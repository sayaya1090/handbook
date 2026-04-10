package dev.sayaya.handbook.interfaces.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe

class BroadcasterConfigTest : DescribeSpec({

    val config = BroadcasterConfig()

    describe("BroadcasterConfig는") {
        it("ObjectMapper를 생성한다") {
            config.objectMapper() shouldNotBe null
        }
        it("WorkspaceSinkManager를 생성한다") {
            config.workspaceSinkManager() shouldNotBe null
        }
        it("Broadcaster를 생성한다") {
            val om = config.objectMapper()
            val sinkManager = config.workspaceSinkManager()
            config.broadcaster(om, sinkManager) shouldNotBe null
        }
    }
})
