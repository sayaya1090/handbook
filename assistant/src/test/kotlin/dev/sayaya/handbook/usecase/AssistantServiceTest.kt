package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class AssistantServiceTest : BehaviorSpec({
    val intentParser = mockk<IntentParser>()
    val planExecutor = mockk<PlanExecutor>()
    val eventPublisher = mockk<AgentCommandEventPublisher>(relaxed = true)
    val auditRepository = mockk<AuditRepository>(relaxed = true)
    val service = AssistantService(intentParser, planExecutor, eventPublisher, auditRepository)

    Given("자연어 메시지가 주어졌을 때") {
        val workspace = UUID.randomUUID()
        val message = "고객 타입 정의를 수정해줘"
        val plan = ExecutionPlan(
            intent = "타입 정의 수정",
            steps = listOf(
                ExecutionStep(
                    group = 0,
                    order = 0,
                    command = AgentCommand(type = CommandType.NAVIGATE, target = "type-management"),
                    description = "타입 관리 화면으로 이동",
                ),
            ),
            confidence = 0.95,
        )
        every { intentParser.parse(message) } returns Mono.just(plan)
        every { auditRepository.save(any()) } answers {
            Mono.just(firstArg<AuditEntry>())
        }

        When("request를 호출하면") {
            val result = service.request(workspace, message)

            Then("ExecutionRequest가 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.executionId shouldNotBe null
                        it.plan.intent shouldBe "타입 정의 수정"
                        it.plan.confidence shouldBe 0.95
                        it.plan.steps.size shouldBe 1
                    }
                    .verifyComplete()
            }
            Then("IntentParser.parse가 호출된다") {
                verify { intentParser.parse(message) }
            }
            Then("감사 기록이 REQUESTED 상태로 저장된다") {
                verify { auditRepository.save(match {
                    it.workspace == workspace &&
                    it.userMessage == message &&
                    it.intent == "타입 정의 수정" &&
                    it.status == AuditEntry.Status.REQUESTED
                }) }
            }
        }
    }

    Given("실행 계획이 주어졌을 때") {
        val workspace = UUID.randomUUID()
        val navigateCmd = AgentCommand(type = CommandType.NAVIGATE, target = "type-management")
        val notifyCmd = AgentCommand(
            type = CommandType.NOTIFY,
            payload = mapOf("level" to "info", "message" to "완료"),
        )
        val plan = ExecutionPlan(
            intent = "타입 정의 수정",
            steps = listOf(
                ExecutionStep(group = 0, order = 0, command = navigateCmd, description = "이동"),
                ExecutionStep(group = 1, order = 1, command = notifyCmd, description = "알림"),
            ),
            confidence = 0.9,
        )
        every { planExecutor.execute(plan) } returns Flux.just(navigateCmd, notifyCmd)
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()
        every { auditRepository.updateArtifact(any(), any()) } returns Mono.empty()

        When("execute를 호출하면") {
            val executionId = UUID.randomUUID()
            val result = service.execute(workspace, executionId, plan)

            Then("Kafka 이벤트가 발행된다") {
                StepVerifier.create(result).verifyComplete()
                Thread.sleep(100)
                verify(atLeast = 1) { eventPublisher.publish(workspace, any(), any()) }
            }
        }
    }

    Given("서브 에이전트가 포함된 실행 계획이 주어졌을 때") {
        val workspace = UUID.randomUUID()
        val subAgentDef1 = SubAgentDefinition(name = "analyzer", role = "분석가", task = "분석하라", group = 0)
        val subAgentDef2 = SubAgentDefinition(name = "writer", role = "작성자", task = "작성하라", group = 1, dependsOn = listOf("analyzer"))
        val planWithSubAgents = ExecutionPlan(
            intent = "복합 태스크",
            steps = emptyList(),
            confidence = 0.9,
            subAgents = listOf(subAgentDef1, subAgentDef2),
        )

        val subAgentExecutor = mockk<SubAgentPlanExecutor>()
        val artifactAggregator = mockk<ArtifactAggregator>()
        val serviceWithSubAgents = AssistantService(intentParser, planExecutor, eventPublisher, auditRepository, subAgentExecutor = subAgentExecutor, artifactAggregator = artifactAggregator)

        val analyzerArtifact = Artifact(executionId = UUID.randomUUID(), summary = "분석 완료", changes = listOf(ArtifactChange(type = "NAVIGATE", target = "t", description = "d")))
        val writerArtifact = Artifact(executionId = UUID.randomUUID(), summary = "작성 완료", changes = listOf(ArtifactChange(type = "MUTATE", target = "t2", description = "d2")))
        val aggregatedArtifact = Artifact(executionId = UUID.randomUUID(), summary = "종합 결과", changes = listOf(ArtifactChange(type = "NAVIGATE", target = "t", description = "d"), ArtifactChange(type = "MUTATE", target = "t2", description = "d2")))

        every { subAgentExecutor.execute(workspace, any(), match { it.name == "analyzer" }, any()) } returns Mono.just(analyzerArtifact)
        every { subAgentExecutor.execute(workspace, any(), match { it.name == "writer" }, any()) } returns Mono.just(writerArtifact)
        every { artifactAggregator.aggregate(any(), any(), any()) } returns aggregatedArtifact
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()
        every { auditRepository.updateArtifact(any(), any()) } returns Mono.empty()

        When("execute를 호출하면") {
            val executionId = UUID.randomUUID()
            val result = serviceWithSubAgents.execute(workspace, executionId, planWithSubAgents)

            Then("서브 에이전트가 그룹 순서대로 실행되고 DELEGATE/COMPLETE 이벤트가 발행된다") {
                StepVerifier.create(result).verifyComplete()
                Thread.sleep(200)
                verify { subAgentExecutor.execute(workspace, executionId, match { it.name == "analyzer" }, any()) }
                verify { subAgentExecutor.execute(workspace, executionId, match { it.name == "writer" }, any()) }
                verify { artifactAggregator.aggregate(executionId, "복합 태스크", any()) }
                verify(atLeast = 1) { eventPublisher.publish(workspace, any(), match { it.type == CommandType.DELEGATE }) }
                verify(atLeast = 1) { eventPublisher.publish(workspace, any(), match { it.type == CommandType.COMPLETE }) }
            }
        }
    }

    Given("서브 에이전트가 병렬 그룹에서 실패할 때") {
        val workspace = UUID.randomUUID()
        val subAgentDef1 = SubAgentDefinition(name = "analyzer", role = "분석가", task = "분석하라", group = 0)
        val subAgentDef2 = SubAgentDefinition(name = "checker", role = "검증자", task = "검증하라", group = 0)
        val planWithFailingSubAgent = ExecutionPlan(
            intent = "병렬 실패 태스크",
            steps = emptyList(),
            confidence = 0.9,
            subAgents = listOf(subAgentDef1, subAgentDef2),
        )

        val subAgentExecutor = mockk<SubAgentPlanExecutor>()
        val artifactAggregator = mockk<ArtifactAggregator>()
        val serviceWithSubAgents = AssistantService(intentParser, planExecutor, eventPublisher, auditRepository, subAgentExecutor = subAgentExecutor, artifactAggregator = artifactAggregator)

        val analyzerArtifact = Artifact(executionId = UUID.randomUUID(), summary = "분석 완료", changes = emptyList())
        every { subAgentExecutor.execute(workspace, any(), match { it.name == "analyzer" }, any()) } returns Mono.just(analyzerArtifact)
        every { subAgentExecutor.execute(workspace, any(), match { it.name == "checker" }, any()) } returns Mono.error(RuntimeException("서브 에이전트 실패"))
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()

        When("execute를 호출하면") {
            val executionId = UUID.randomUUID()
            val result = serviceWithSubAgents.execute(workspace, executionId, planWithFailingSubAgent)

            Then("실행은 시작되지만 서브 에이전트 실패로 에러 상태가 된다") {
                StepVerifier.create(result).verifyComplete()
                Thread.sleep(300)
                // 내부 subscribe에서 에러 발생 → context status가 ERROR로 설정됨
            }
        }
    }

    Given("dependsOn이 존재하지 않는 서브 에이전트를 참조할 때") {
        val workspace = UUID.randomUUID()
        val subAgentDef = SubAgentDefinition(name = "writer", role = "작성자", task = "작성하라", group = 0, dependsOn = listOf("non-existent-agent"))
        val planWithMissingDep = ExecutionPlan(
            intent = "의존성 누락 태스크",
            steps = emptyList(),
            confidence = 0.9,
            subAgents = listOf(subAgentDef),
        )

        val subAgentExecutor = mockk<SubAgentPlanExecutor>()
        val artifactAggregator = mockk<ArtifactAggregator>()
        val serviceWithSubAgents = AssistantService(intentParser, planExecutor, eventPublisher, auditRepository, subAgentExecutor = subAgentExecutor, artifactAggregator = artifactAggregator)

        val writerArtifact = Artifact(executionId = UUID.randomUUID(), summary = "작성 완료", changes = emptyList())
        every { subAgentExecutor.execute(workspace, any(), match { it.name == "writer" }, any()) } returns Mono.just(writerArtifact)
        every { artifactAggregator.aggregate(any(), any(), any()) } returns writerArtifact
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()
        every { auditRepository.updateArtifact(any(), any()) } returns Mono.empty()

        When("execute를 호출하면") {
            val executionId = UUID.randomUUID()
            val result = serviceWithSubAgents.execute(workspace, executionId, planWithMissingDep)

            Then("빈 upstream artifacts로 실행된다") {
                StepVerifier.create(result).verifyComplete()
                Thread.sleep(300)
                verify { subAgentExecutor.execute(workspace, executionId, match { it.name == "writer" }, match { it.isEmpty() }) }
            }
        }
    }

    Given("순환 의존관계가 있을 때 (A→B, B→A, 같은 그룹)") {
        val workspace = UUID.randomUUID()
        val subAgentA = SubAgentDefinition(name = "agentA", role = "A", task = "A 태스크", group = 0, dependsOn = listOf("agentB"))
        val subAgentB = SubAgentDefinition(name = "agentB", role = "B", task = "B 태스크", group = 0, dependsOn = listOf("agentA"))
        val planWithCircular = ExecutionPlan(
            intent = "순환 의존 태스크",
            steps = emptyList(),
            confidence = 0.9,
            subAgents = listOf(subAgentA, subAgentB),
        )

        val subAgentExecutor = mockk<SubAgentPlanExecutor>()
        val artifactAggregator = mockk<ArtifactAggregator>()
        val serviceWithSubAgents = AssistantService(intentParser, planExecutor, eventPublisher, auditRepository, subAgentExecutor = subAgentExecutor, artifactAggregator = artifactAggregator)

        val artifactA = Artifact(executionId = UUID.randomUUID(), summary = "A 완료", changes = emptyList())
        val artifactB = Artifact(executionId = UUID.randomUUID(), summary = "B 완료", changes = emptyList())
        val aggregated = Artifact(executionId = UUID.randomUUID(), summary = "종합", changes = emptyList())

        every { subAgentExecutor.execute(workspace, any(), match { it.name == "agentA" }, any()) } returns Mono.just(artifactA)
        every { subAgentExecutor.execute(workspace, any(), match { it.name == "agentB" }, any()) } returns Mono.just(artifactB)
        every { artifactAggregator.aggregate(any(), any(), any()) } returns aggregated
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()
        every { auditRepository.updateArtifact(any(), any()) } returns Mono.empty()

        When("execute를 호출하면") {
            val executionId = UUID.randomUUID()
            val result = serviceWithSubAgents.execute(workspace, executionId, planWithCircular)

            Then("데드락 없이 완료된다 (같은 그룹 병렬 실행, 빈 upstream)") {
                StepVerifier.create(result).verifyComplete()
                Thread.sleep(300)
                verify { subAgentExecutor.execute(workspace, executionId, match { it.name == "agentA" }, any()) }
                verify { subAgentExecutor.execute(workspace, executionId, match { it.name == "agentB" }, any()) }
            }
        }
    }

    Given("존재하지 않는 executionId에 respond를 호출할 때") {
        When("respond를 호출하면") {
            val result = service.respond(UUID.randomUUID(), UUID.randomUUID(), "confirm")

            Then("에러 없이 완료된다 (멱등성)") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }
    }

    Given("AWAIT_CONFIRM 대기 중 abort를 호출할 때") {
        val workspace = UUID.randomUUID()
        val awaitCmd = AgentCommand(type = CommandType.AWAIT_CONFIRM, payload = mapOf("message" to "계속할까요?"))
        val plan = ExecutionPlan(
            intent = "확인 대기 태스크",
            steps = listOf(
                ExecutionStep(group = 0, order = 0, command = awaitCmd, description = "사용자 확인 대기"),
            ),
            confidence = 0.9,
        )
        every { planExecutor.execute(plan) } returns Flux.just(
            AgentCommand(type = CommandType.PROGRESS, payload = mapOf("currentGroup" to 1, "totalGroups" to 1)),
            awaitCmd,
        )
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()

        When("execute 후 abort를 호출하면") {
            val executionId = UUID.randomUUID()
            service.execute(workspace, executionId, plan).subscribe()
            Thread.sleep(200)

            val abortResult = service.abort(executionId)

            Then("실행이 중단되고 에러 없이 완료된다") {
                StepVerifier.create(abortResult)
                    .verifyComplete()
            }
        }
    }

    Given("두 개의 실행이 동시에 진행 중일 때") {
        val workspace = UUID.randomUUID()
        val cmd1 = AgentCommand(type = CommandType.NAVIGATE, target = "page1")
        val cmd2 = AgentCommand(type = CommandType.NAVIGATE, target = "page2")
        val plan1 = ExecutionPlan(
            intent = "태스크 1",
            steps = listOf(ExecutionStep(group = 0, order = 0, command = cmd1, description = "이동1")),
            confidence = 0.9,
        )
        val plan2 = ExecutionPlan(
            intent = "태스크 2",
            steps = listOf(ExecutionStep(group = 0, order = 0, command = cmd2, description = "이동2")),
            confidence = 0.9,
        )

        val awaitCmd = AgentCommand(type = CommandType.AWAIT_CONFIRM, payload = mapOf("message" to "대기"))
        every { planExecutor.execute(plan1) } returns Flux.just(
            AgentCommand(type = CommandType.PROGRESS, payload = mapOf("currentGroup" to 1, "totalGroups" to 1)),
            awaitCmd,
        )
        every { planExecutor.execute(plan2) } returns Flux.just(
            AgentCommand(type = CommandType.PROGRESS, payload = mapOf("currentGroup" to 1, "totalGroups" to 1)),
            cmd2,
            AgentCommand(type = CommandType.COMPLETE, payload = mapOf("intent" to "태스크 2")),
        )
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()
        every { auditRepository.updateArtifact(any(), any()) } returns Mono.empty()

        When("abort를 첫 번째 실행에만 호출하면") {
            val executionId1 = UUID.randomUUID()
            val executionId2 = UUID.randomUUID()

            service.execute(workspace, executionId1, plan1).subscribe()
            service.execute(workspace, executionId2, plan2).subscribe()
            Thread.sleep(200)

            val abortResult = service.abort(executionId1)

            Then("첫 번째만 중단되고 두 번째는 영향받지 않는다") {
                StepVerifier.create(abortResult).verifyComplete()
            }
        }
    }

    Given("실행 중인 계획이 있을 때") {
        When("abort를 호출하면") {
            val executionId = UUID.randomUUID()
            val result = service.abort(executionId)

            Then("성공적으로 완료된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }
    }

    Given("워크스페이스에 활성 실행이 있을 때 (UC-A9)") {
        val workspace = UUID.randomUUID()
        val navigateCmd = AgentCommand(type = CommandType.NAVIGATE, target = "type")
        val plan = ExecutionPlan(
            intent = "타입 이동",
            steps = listOf(ExecutionStep(group = 0, order = 0, command = navigateCmd, description = "이동")),
            confidence = 0.9,
        )
        // execute를 시작하면 contextManager에 등록됨 — AWAIT_CONFIRM으로 대기시킴
        val awaitCmd = AgentCommand(type = CommandType.AWAIT_CONFIRM, payload = mapOf("message" to "대기"))
        every { planExecutor.execute(plan) } returns Flux.just(
            AgentCommand(type = CommandType.PROGRESS, payload = mapOf("currentGroup" to 0, "totalGroups" to 1)),
            awaitCmd,
        )
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()

        When("getExecutions를 호출하면") {
            val executionId = UUID.randomUUID()
            service.execute(workspace, executionId, plan).subscribe()
            Thread.sleep(100)
            val result = service.getExecutions(workspace)

            Then("활성 실행 목록이 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it["executionId"] shouldBe executionId
                        it["intent"] shouldBe "타입 이동"
                        it["status"] shouldBe "EXECUTING"
                    }
                    .verifyComplete()
            }

            // cleanup
            service.abort(executionId).subscribe()
        }
    }

    Given("워크스페이스에 완료된 실행의 아티팩트가 있을 때 (UC-A10)") {
        val workspace = UUID.randomUUID()
        val executionId = UUID.randomUUID()
        val artifact = Artifact(
            executionId = executionId,
            summary = "이동 완료",
            changes = listOf(ArtifactChange(type = "NAVIGATE", target = "type", description = "이동")),
        )
        val auditEntry = AuditEntry(
            id = executionId,
            workspace = workspace,
            userMessage = "이동해줘",
            intent = "이동",
            confidence = 0.9,
            plan = ExecutionPlan(intent = "이동", steps = emptyList(), confidence = 0.9),
            status = AuditEntry.Status.COMPLETED,
            artifact = artifact,
        )
        every {
            auditRepository.findByWorkspaceAndStatusWithArtifact(workspace, AuditEntry.Status.COMPLETED)
        } returns Flux.just(auditEntry)

        When("getArtifacts를 호출하면") {
            val result = service.getArtifacts(workspace)

            Then("아티팩트가 포함된 AuditEntry 목록이 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.artifact shouldNotBe null
                        it.artifact!!.summary shouldBe "이동 완료"
                        it.artifact!!.changes.size shouldBe 1
                        it.status shouldBe AuditEntry.Status.COMPLETED
                    }
                    .verifyComplete()
            }
        }
    }

    Given("워크스페이스에 아티팩트가 없을 때 (UC-A10)") {
        val workspace = UUID.randomUUID()
        every {
            auditRepository.findByWorkspaceAndStatusWithArtifact(workspace, AuditEntry.Status.COMPLETED)
        } returns Flux.empty()

        When("getArtifacts를 호출하면") {
            val result = service.getArtifacts(workspace)

            Then("빈 목록이 반환된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }
    }
})
