package dev.sayaya.handbook.client.usecase.action;

import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActionFactory {
    @Delegate private final AddDocumentAction.AddDocumentActionFactory add;
    @Delegate private final EditDocumentAction.EditDocumentActionFactory edit;
    @Delegate private final SaveAction.SaveActionFactory save;
    @Inject ActionFactory(
            AddDocumentAction.AddDocumentActionFactory add,
            EditDocumentAction.EditDocumentActionFactory edit,
            SaveAction.SaveActionFactory save
    ) {
        this.add = add;
        this.edit = edit;
        this.save = save;
    }
}
