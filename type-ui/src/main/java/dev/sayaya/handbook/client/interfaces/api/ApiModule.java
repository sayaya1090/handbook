package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
import dev.sayaya.handbook.usecase.TypeRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.interfaces.api.TypeNative;
import dev.sayaya.handbook.interfaces.api.LayoutNative;
import dev.sayaya.handbook.interfaces.api.AttributeNative;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds TypeRepository typeRepository(TypeApi impl);
    @Binds LayoutRepository layoutRepository(LayoutApi impl);
    @Provides static TypeNative typeNative() { return new TypeNative(); }
    @Provides static LayoutNative layoutNative() { return new LayoutNative(); }
    @Provides static AttributeNative attributeNative() { return new AttributeNative(); }
}
