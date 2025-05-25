package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.DocumentList;

class EditDocumentAction implements Action {
    private final Document before;
    private final Document after;
    private final DocumentList documentList;
    @AssistedInject EditDocumentAction(@Assisted("before") Document before, @Assisted("after") Document after, DocumentList documentList) {
        this.before = before;
        this.after = after;
        this.documentList = documentList;
    }
    @Override
    public void execute() {
        documentList.replace(before, after);
    }
    @Override
    public void rollback() {
        documentList.replace(after, before);
    }
    @AssistedFactory
    interface EditDocumentActionFactory {
        EditDocumentAction _edit(@Assisted("before") Document before, @Assisted("after") Document after);
        default Action edit(Document before, Document after) {
            return _edit(before, after);
        }
    }
}
