package dev.sayaya.handbook.client.canvas;

import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.box.BoxElementList;
import dev.sayaya.handbook.client.repository.LanguageRepository;
import dev.sayaya.handbook.client.usecase.BoxTailor;
import dev.sayaya.handbook.client.usecase.UpdatableBoxList;
import dev.sayaya.handbook.client.usecase.language.LanguageProvider;

@dagger.Module
public interface Module {
    @Binds LanguageProvider bindLanguageProvider(LanguageRepository impl);
    @Binds UpdatableBoxList updatableBoxProvider(BoxElementList impl);
    @Provides static BoxTailor boxTailorProvider() {
        return box->{
            if(box == null) return 0;
            return 100 + box.values().size()*57;
        };
    }
}
