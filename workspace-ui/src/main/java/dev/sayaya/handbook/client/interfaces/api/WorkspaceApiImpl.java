package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.handbook.client.usecase.WorkspaceApi;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WorkspaceApiImpl implements WorkspaceApi {
    private final FetchApi fetchApi;

    @Inject WorkspaceApiImpl(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    @Override
    public Observable<Workspace> update(String id, String name, String description) {
        JsPropertyMap<Object> body = JsPropertyMap.of();
        body.set("name", name);
        if (description != null) body.set("description", description);

        RequestInit init = RequestInit.create();
        init.setMethod("PUT");
        init.setBody(Global.JSON.stringify(body));
        init.setHeaders(new String[][]{
                {"Content-Type", "application/vnd.sayaya.handbook.v1+json"},
                {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });

        Promise<Workspace> promise = fetchApi.request("workspace/" + id, init)
                .then(this::handleResponse)
                .then(resp -> resp.json())
                .then(json -> Promise.resolve(Js.<Workspace>cast(json)))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.update failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.update failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Group[]> listGroups(String workspaceId) {
        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setHeaders(new String[][]{{"Accept", "application/vnd.sayaya.handbook.v1+json"}});

        Promise<Group[]> promise = fetchApi.request("workspace/" + workspaceId + "/groups", init)
                .then(this::handleResponse)
                .then(resp -> resp.json())
                .then(json -> Promise.resolve(Js.<Group[]>cast(json)))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.listGroups failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.listGroups failed: " + err);
                    return Promise.<Group[]>reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Group> createGroup(String workspaceId, String name, String description) {
        JsPropertyMap<Object> body = JsPropertyMap.of();
        body.set("name", name);
        if (description != null) body.set("description", description);

        RequestInit init = RequestInit.create();
        init.setMethod("POST");
        init.setBody(Global.JSON.stringify(body));
        init.setHeaders(new String[][]{
                {"Content-Type", "application/json"},
                {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });

        Promise<Group> promise = fetchApi.request("workspace/" + workspaceId + "/groups", init)
                .then(this::handleResponse)
                .then(resp -> resp.json())
                .then(json -> Promise.resolve(Js.<Group>cast(json)))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.createGroup failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.createGroup failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> deleteGroup(String workspaceId, String groupId) {
        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");

        Promise<Void> promise = fetchApi.request("workspace/" + workspaceId + "/groups/" + groupId, init)
                .then(this::handleResponse)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.deleteGroup failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.deleteGroup failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<User[]> listMembers(String workspaceId, String groupId) {
        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setHeaders(new String[][]{{"Accept", "application/vnd.sayaya.handbook.v1+json"}});

        Promise<User[]> promise = fetchApi.request("workspace/" + workspaceId + "/groups/" + groupId + "/members", init)
                .then(this::handleResponse)
                .then(resp -> resp.json())
                .then(json -> Promise.resolve(Js.<User[]>cast(json)))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.listMembers failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.listMembers failed: " + err);
                    return Promise.<User[]>reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> addMember(String workspaceId, String groupId, String userId) {
        RequestInit init = RequestInit.create();
        init.setMethod("POST");

        Promise<Void> promise = fetchApi.request("workspace/" + workspaceId + "/groups/" + groupId + "/members/" + userId, init)
                .then(this::handleResponse)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.addMember failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.addMember failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> removeMember(String workspaceId, String groupId, String userId) {
        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");

        Promise<Void> promise = fetchApi.request("workspace/" + workspaceId + "/groups/" + groupId + "/members/" + userId, init)
                .then(this::handleResponse)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.removeMember failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.removeMember failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<String[]> listRoles(String workspaceId, String groupId) {
        RequestInit init = RequestInit.create();
        init.setMethod("GET");

        Promise<String[]> promise = fetchApi.request("workspace/" + workspaceId + "/groups/" + groupId + "/roles", init)
                .then(this::handleResponse)
                .then(resp -> resp.json())
                .then(json -> Promise.resolve(Js.<String[]>cast(json)))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.listRoles failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.listRoles failed: " + err);
                    return Promise.<String[]>reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> assignRole(String workspaceId, String groupId, String roleName) {
        JsPropertyMap<Object> body = JsPropertyMap.of();
        body.set("roleName", roleName);

        RequestInit init = RequestInit.create();
        init.setMethod("POST");
        init.setBody(Global.JSON.stringify(body));
        init.setHeaders(new String[][]{{"Content-Type", "application/json"}});

        Promise<Void> promise = fetchApi.request("workspace/" + workspaceId + "/groups/" + groupId + "/roles", init)
                .then(this::handleResponse)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.assignRole failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.assignRole failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> removeRole(String workspaceId, String groupId, String roleName) {
        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");

        Promise<Void> promise = fetchApi.request("workspace/" + workspaceId + "/groups/" + groupId + "/roles/" + roleName, init)
                .then(this::handleResponse)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.removeRole failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.removeRole failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    private Promise<Response> handleResponse(Response response) {
        if (response.ok) return Promise.resolve(response);
        return Promise.reject("HTTP " + response.status);
    }
}
