package dev.sayaya.handbook.client.interfaces.value;

import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ValidatorEditorFactory {
    @Delegate private final ValidatorRegexEditorElement.ValidatorRegexEditorElementFactory regex;
    @Delegate private final ValidatorSelectEditorElement.ValidatorSelectEditorElementFactory select;
    @Inject ValidatorEditorFactory (
            ValidatorRegexEditorElement.ValidatorRegexEditorElementFactory regex,
            ValidatorSelectEditorElement.ValidatorSelectEditorElementFactory select
    ) {
        this.regex = regex;
        this.select = select;
    }
}
