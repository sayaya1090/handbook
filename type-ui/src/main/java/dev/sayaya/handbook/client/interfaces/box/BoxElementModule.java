package dev.sayaya.handbook.client.interfaces.box;

import dagger.Binds;
import dagger.Module;
import dev.sayaya.handbook.client.usecase.UpdatableTypeList;

@Module
public interface BoxElementModule {
    @Binds UpdatableTypeList updatableBoxProvider(TypeElementList impl);
}
