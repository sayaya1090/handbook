package dev.sayaya.handbook.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * JSON 언어팩 파일을 fetch로 로드한다.
 * 요청 실패 시 영어(en) 팩으로 폴백한다.
 */
@Singleton
public class FetchLanguagePackRepository implements LanguagePackRepository {
    private final FetchApi fetchApi;

    @Inject FetchLanguagePackRepository(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    @Override
    public Observable<Labels> load(String lang) {
        Promise<Labels> promise = fetchLanguagePack(lang)
                .catch_(err -> {
                    GWT.log("Language pack '" + lang + "' not found, falling back to 'en'");
                    return fetchLanguagePack("en");
                });
        return AsyncSubject.await(promise);
    }

    private Promise<Labels> fetchLanguagePack(String lang) {
        return fetchApi.request("js/language." + lang + ".json")
                .then(this::handleResponse)
                .then(Response::json)
                .then(obj -> Promise.resolve((Labels) obj));
    }

    private Promise<Response> handleResponse(Response response) {
        if (response.ok) return Promise.resolve(response);
        return Promise.reject("HTTP " + response.status);
    }
}
