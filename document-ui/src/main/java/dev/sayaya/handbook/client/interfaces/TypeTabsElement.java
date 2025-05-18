package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.ui.dom.MdTabsElement;
import dev.sayaya.ui.elements.TabsElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.TabsElementBuilder.tabs;

@Singleton
public class TypeTabsElement implements IsElement<MdTabsElement> {
    @Delegate private TabsElementBuilder.TabsPrimaryElementBuilder tabs = tabs().primary().style("""
            min-width: -webkit-fill-available;
            width: fit-content;
            """);
    @Inject
    TypeTabsElement() {
        tabs.tab().add("AAA").end()
                .tab().add("BBB").end().tab().add("AAA").end()
                .tab().add("BBB").end().tab().add("AAA").end()
                .tab().add("BBB").end().tab().add("AAA").end()
                .tab().add("BBB").end().tab().add("AAA").end()
                .tab().add("BBB").end().tab().add("AAA").end()
                .tab().add("BBB").end().tab().add("AAA").end()
                .tab().add("BBB").end();
    }
}
