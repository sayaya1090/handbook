package dev.sayaya.handbook.client.interfaces.box;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.BoxTailor;
import dev.sayaya.handbook.client.usecase.UpdatableBoxList;

@Module
public interface BoxElementModule {
    @Binds UpdatableBoxList updatableBoxProvider(BoxElementList impl);
    @Provides static BoxTailor boxTailorProvider() {
        return box->{
            if(box == null) return 0;
            return 100 + box.values().size()*57;
        };
    }
}
