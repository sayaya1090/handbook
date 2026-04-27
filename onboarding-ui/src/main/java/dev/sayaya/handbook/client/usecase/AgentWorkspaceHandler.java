package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.usecase.MutationReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 에이전트의 MutateCommand를 워크스페이스 UI 조작으로 변환한다.
 *
 * <p>지원 명령어:
 * <ul>
 *   <li>{@code WS_MODE CREATE} -- CREATE 모드로 전환</li>
 *   <li>{@code WS_MODE JOIN} -- JOIN 모드로 전환</li>
 *   <li>{@code WS_INPUT <value>} -- 입력 필드에 값 설정</li>
 *   <li>{@code WS_SUBMIT} -- 제출 버튼 클릭</li>
 *   <li>{@code WS_CREATE <name>} -- 워크스페이스 생성 (모드 전환 + 입력 + 제출을 한번에)</li>
 * </ul>
 */
@Singleton
public class AgentWorkspaceHandler {
    private final CreateWorkspaceMode mode;
    private final CreateWorkspaceParam param;
    private final WorkspaceRepository repository;

    @Inject
    AgentWorkspaceHandler(CreateWorkspaceMode mode, CreateWorkspaceParam param,
                          WorkspaceRepository repository, MutationReceiver mutationReceiver) {
        this.mode = mode;
        this.param = param;
        this.repository = repository;

        mutationReceiver.mutations().subscribe(changes -> {
            if (changes == null) return;
            for (String change : changes) {
                processChange(change);
            }
        });
    }

    private void processChange(String change) {
        if (change == null || change.isEmpty()) return;
        String[] parts = change.split("\\s+", 2);
        String command = parts[0].toUpperCase();
        String operand = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "WS_MODE":   setMode(operand); break;
            case "WS_INPUT":  setInput(operand); break;
            case "WS_SUBMIT": submit(); break;
            case "WS_CREATE": createWorkspace(operand); break;
        }
    }

    private void setMode(String modeStr) {
        switch (modeStr.toUpperCase()) {
            case "CREATE": mode.next(CreateWorkspaceMode.Mode.CREATE); break;
            case "JOIN":   mode.next(CreateWorkspaceMode.Mode.JOIN); break;
        }
    }

    private void setInput(String value) {
        param.next(value);
    }

    private void submit() {
        String value = param.getValue();
        if (value == null || value.trim().isEmpty()) return;
        if (mode.getValue() == CreateWorkspaceMode.Mode.CREATE) {
            repository.create(value.trim(), null);
        }
    }

    /** 모드 전환 + 입력 + 제출을 한번에 수행한다. */
    private void createWorkspace(String name) {
        if (name == null || name.trim().isEmpty()) return;
        mode.next(CreateWorkspaceMode.Mode.CREATE);
        param.next(name.trim());
        repository.create(name.trim(), null);
    }
}
