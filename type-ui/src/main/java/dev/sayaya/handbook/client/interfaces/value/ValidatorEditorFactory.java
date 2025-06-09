package dev.sayaya.handbook.client.interfaces.value;

import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ValidatorEditorFactory {
    @Delegate private final ValidatorRegexEditorElement.ValidatorRegexEditorElementFactory regex;
    @Delegate private final ValidatorNumberEditorElement.ValidatorNumberEditorElementFactory number;
    @Delegate private final ValidatorSelectEditorElement.ValidatorSelectEditorElementFactory select;
    @Delegate private final ValidatorFileEditorElement.ValidatorFileEditorElementFactory file;
    @Delegate private final ValidatorDocumentEditorElement.ValidatorRegexEditorElementFactory document;
    @Inject ValidatorEditorFactory (
            ValidatorRegexEditorElement.ValidatorRegexEditorElementFactory regex,
            ValidatorNumberEditorElement.ValidatorNumberEditorElementFactory number,
            ValidatorSelectEditorElement.ValidatorSelectEditorElementFactory select,
            ValidatorFileEditorElement.ValidatorFileEditorElementFactory file,
            ValidatorDocumentEditorElement.ValidatorRegexEditorElementFactory document
    ) {
        this.regex = regex;
        this.number = number;
        this.select = select;
        this.file = file;
        this.document = document;
    }
}
