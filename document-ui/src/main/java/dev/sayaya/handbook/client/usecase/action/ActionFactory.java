package dev.sayaya.handbook.client.usecase.action;

import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActionFactory {
    @Delegate private final AddDocumentAction.AddDocumentActionFactory add;
    @Delegate private final DeleteDocumentAction.DeleteDocumentActionFactory delete;
    @Delegate private final EditDocumentAction.EditDocumentActionFactory edit;
    @Delegate private final SaveAction.SaveActionFactory save;
    @Inject ActionFactory(
            AddDocumentAction.AddDocumentActionFactory add,
            DeleteDocumentAction.DeleteDocumentActionFactory delete,
            EditDocumentAction.EditDocumentActionFactory edit,
            SaveAction.SaveActionFactory save
    ) {
        this.add = add;
        this.delete = delete;
        this.edit = edit;
        this.save = save;
    }
}
