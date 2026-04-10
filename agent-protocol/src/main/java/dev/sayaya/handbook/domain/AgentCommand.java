package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 에이전트 → 프론트엔드 UI 제어 커맨드.
 * Jackson 폴리모픽 직렬화를 지원하며, 커맨드 타입별로 서브클래스가 정의된다.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NavigateCommand.class, name = "navigate"),
    @JsonSubTypes.Type(value = HighlightCommand.class, name = "highlight"),
    @JsonSubTypes.Type(value = AttentionCommand.class, name = "attention"),
    @JsonSubTypes.Type(value = ScrollCommand.class, name = "scroll"),
    @JsonSubTypes.Type(value = PreviewCommand.class, name = "preview"),
    @JsonSubTypes.Type(value = MutateCommand.class, name = "mutate"),
    @JsonSubTypes.Type(value = NotifyCommand.class, name = "notify"),
    @JsonSubTypes.Type(value = ProgressCommand.class, name = "progress"),
    @JsonSubTypes.Type(value = AwaitConfirmCommand.class, name = "await_confirm"),
    @JsonSubTypes.Type(value = CompleteCommand.class, name = "complete"),
})
public abstract class AgentCommand {
    @JsonProperty("seq") private int seq;
    @JsonProperty("description") private String description;

    protected AgentCommand() {}
    protected AgentCommand(int seq, String description) {
        this.seq = seq;
        this.description = description;
    }
    public int seq() { return seq; }
    public String description() { return description; }
}
