package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import tools.jackson.databind.ObjectMapper

class AgentCommandTest : DescribeSpec({

    val mapper = ObjectMapper()

    describe("NavigateCommand") {
        it("직렬화 시 type 필드가 navigate로 설정된다") {
            val cmd = NavigateCommand(1, "타입 관리 화면으로 이동", "타입", "타입 관리", "/workspace/ws-1/type")
            val json = mapper.writeValueAsString(cmd)
            val tree = mapper.readTree(json)
            tree.get("type").asText() shouldBe "navigate"
            tree.get("seq").asInt() shouldBe 1
            tree.get("menu").asText() shouldBe "타입"
            tree.get("tool").asText() shouldBe "타입 관리"
            tree.get("url").asText() shouldBe "/workspace/ws-1/type"
        }
        it("역직렬화 시 NavigateCommand로 복원된다") {
            val json = """{"type":"navigate","seq":1,"description":"이동","menu":"타입","tool":"편집","url":"/ws/1"}"""
            val cmd = mapper.readValue(json, AgentCommand::class.java)
            cmd.shouldBeInstanceOf<NavigateCommand>()
            cmd.menu() shouldBe "타입"
            cmd.tool() shouldBe "편집"
            cmd.url() shouldBe "/ws/1"
        }
    }

    describe("HighlightCommand") {
        it("직렬화/역직렬화 라운드트립") {
            val cmd = HighlightCommand(3, "저장 버튼 강조", "#save-button")
            val json = mapper.writeValueAsString(cmd)
            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<HighlightCommand>()
            restored.target() shouldBe "#save-button"
            restored.seq() shouldBe 3
        }
    }

    describe("AttentionCommand") {
        it("모든 필드가 직렬화/역직렬화된다") {
            val cmd = AttentionCommand(4, "속성 편집 안내", ".property-panel", AttentionStyle.COACHMARK, "여기서 편집합니다", "bottom", true)
            val json = mapper.writeValueAsString(cmd)
            val tree = mapper.readTree(json)
            tree.get("type").asText() shouldBe "attention"
            tree.get("style").asText() shouldBe "COACHMARK"
            tree.get("dismissable").asBoolean() shouldBe true

            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<AttentionCommand>()
            restored.style() shouldBe AttentionStyle.COACHMARK
            restored.message() shouldBe "여기서 편집합니다"
            restored.position() shouldBe "bottom"
            restored.dismissable() shouldBe true
        }
    }

    describe("ScrollCommand") {
        it("직렬화/역직렬화 라운드트립") {
            val cmd = ScrollCommand(2, "변경된 행으로 스크롤", "tr[data-row-id='row-42']")
            val json = mapper.writeValueAsString(cmd)
            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<ScrollCommand>()
            restored.target() shouldBe "tr[data-row-id='row-42']"
        }
    }

    describe("PreviewCommand") {
        it("changes 배열이 직렬화/역직렬화된다") {
            val changes = arrayOf("field:name:label: 이름 → 고객명", "field:name:desc: 설명 변경")
            val cmd = PreviewCommand(5, "필드명 변경 미리보기", changes)
            val json = mapper.writeValueAsString(cmd)
            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<PreviewCommand>()
            restored.changes().size shouldBe 2
            restored.changes()[0] shouldBe "field:name:label: 이름 → 고객명"
        }
    }

    describe("MutateCommand") {
        it("changes 배열이 직렬화/역직렬화된다") {
            val changes = arrayOf("ADD field:phone:type=STRING", "SET field:phone:label=전화번호")
            val cmd = MutateCommand(7, "전화번호 필드 추가", changes)
            val json = mapper.writeValueAsString(cmd)
            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<MutateCommand>()
            restored.changes().size shouldBe 2
        }
    }

    describe("NotifyCommand") {
        it("level과 message가 직렬화/역직렬화된다") {
            val cmd = NotifyCommand(8, "권한 경고", "warning", "3건의 문서에 권한이 부족합니다")
            val json = mapper.writeValueAsString(cmd)
            val tree = mapper.readTree(json)
            tree.get("type").asText() shouldBe "notify"
            tree.get("level").asText() shouldBe "warning"

            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<NotifyCommand>()
            restored.level() shouldBe "warning"
            restored.message() shouldBe "3건의 문서에 권한이 부족합니다"
        }
    }

    describe("ProgressCommand") {
        it("value와 max가 직렬화/역직렬화된다") {
            val cmd = ProgressCommand(9, "우편번호 보정 진행", 12.0, 50.0)
            val json = mapper.writeValueAsString(cmd)
            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<ProgressCommand>()
            restored.value() shouldBe 12.0
            restored.max() shouldBe 50.0
        }
    }

    describe("AwaitConfirmCommand") {
        it("options 배열이 직렬화/역직렬화된다") {
            val cmd = AwaitConfirmCommand(6, "변경사항 적용 확인", arrayOf("적용", "수정", "취소"))
            val json = mapper.writeValueAsString(cmd)
            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<AwaitConfirmCommand>()
            restored.options().size shouldBe 3
            restored.options()[0] shouldBe "적용"
            restored.options()[2] shouldBe "취소"
        }
    }

    describe("CompleteCommand") {
        it("summary가 직렬화/역직렬화된다") {
            val cmd = CompleteCommand(10, "작업 완료", "고객 타입에 전화번호 필드를 추가했습니다.")
            val json = mapper.writeValueAsString(cmd)
            val restored = mapper.readValue(json, AgentCommand::class.java)
            restored.shouldBeInstanceOf<CompleteCommand>()
            restored.summary() shouldBe "고객 타입에 전화번호 필드를 추가했습니다."
        }
    }

    describe("폴리모픽 역직렬화") {
        it("type 필드만으로 올바른 서브클래스를 결정한다") {
            val commands = listOf(
                """{"type":"navigate","seq":1,"description":"이동","menu":"타입","tool":null,"url":null}""",
                """{"type":"highlight","seq":2,"description":"강조","target":"#btn"}""",
                """{"type":"attention","seq":3,"description":"안내","target":".panel","style":"SPOTLIGHT","message":"봐주세요","position":"top","dismissable":false}""",
                """{"type":"scroll","seq":4,"description":"스크롤","target":"#row"}""",
                """{"type":"preview","seq":5,"description":"미리보기","changes":["a→b"]}""",
                """{"type":"mutate","seq":6,"description":"변경","changes":["ADD x"]}""",
                """{"type":"notify","seq":7,"description":"알림","level":"info","message":"OK"}""",
                """{"type":"progress","seq":8,"description":"진행","value":1.0,"max":10.0}""",
                """{"type":"await_confirm","seq":9,"description":"확인","options":["예","아니오"]}""",
                """{"type":"complete","seq":10,"description":"완료","summary":"끝"}"""
            )
            val expectedTypes = listOf(
                NavigateCommand::class, HighlightCommand::class, AttentionCommand::class,
                ScrollCommand::class, PreviewCommand::class, MutateCommand::class,
                NotifyCommand::class, ProgressCommand::class, AwaitConfirmCommand::class,
                CompleteCommand::class
            )
            commands.zip(expectedTypes).forEach { (json, expectedType) ->
                val cmd = mapper.readValue(json, AgentCommand::class.java)
                cmd::class shouldBe expectedType
            }
        }
    }

    describe("CommandType 열거형") {
        it("10개의 커맨드 타입이 정의되어 있다") {
            CommandType.entries.size shouldBe 10
        }
        it("모든 커맨드 타입이 존재한다") {
            CommandType.valueOf("NAVIGATE")
            CommandType.valueOf("HIGHLIGHT")
            CommandType.valueOf("ATTENTION")
            CommandType.valueOf("SCROLL")
            CommandType.valueOf("PREVIEW")
            CommandType.valueOf("MUTATE")
            CommandType.valueOf("NOTIFY")
            CommandType.valueOf("PROGRESS")
            CommandType.valueOf("AWAIT_CONFIRM")
            CommandType.valueOf("COMPLETE")
        }
    }

    describe("AttentionStyle 열거형") {
        it("5개의 스타일이 정의되어 있다") {
            AttentionStyle.entries.size shouldBe 5
        }
    }
})
