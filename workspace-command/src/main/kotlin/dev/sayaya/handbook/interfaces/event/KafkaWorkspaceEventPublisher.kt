package dev.sayaya.handbook.interfaces.event

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceEventPublisher
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class KafkaWorkspaceEventPublisher(private val streamBridge: StreamBridge) : WorkspaceEventPublisher {
    override fun publishCreated(workspace: Workspace): Mono<Void> = Mono.fromRunnable {
        streamBridge.send("workspace-out-0", mapOf(
            "type" to "WORKSPACE_CREATED",
            "workspaceId" to workspace.id(),
            "name" to workspace.name(),
        ))
    }

    override fun publishDeleted(workspaceId: UUID): Mono<Void> = Mono.fromRunnable {
        streamBridge.send("workspace-out-0", mapOf(
            "type" to "WORKSPACE_DELETED",
            "workspaceId" to workspaceId.toString(),
        ))
    }
}
