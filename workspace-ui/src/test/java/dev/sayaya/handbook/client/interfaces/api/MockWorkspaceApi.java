package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.usecase.WorkspaceApi;
import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MockWorkspaceApi implements WorkspaceApi {
    @Inject MockWorkspaceApi() {}
    @Override public Observable<Workspace> update(String id, String n, String d) { return Observable.from(new Workspace()); }
    @Override public Observable<Group[]> listGroups(String ws) { return Observable.from(new Group[0]); }
    @Override public Observable<Group> createGroup(String ws, String n, String d) { return Observable.from(new Group()); }
    @Override public Observable<Void> deleteGroup(String ws, String gid) { return Observable.of(null); }
    @Override public Observable<User[]> listMembers(String ws, String gid) { return Observable.from(new User[0]); }
    @Override public Observable<Void> addMember(String ws, String gid, String uid) { return Observable.of(null); }
    @Override public Observable<Void> removeMember(String ws, String gid, String uid) { return Observable.of(null); }
    @Override public Observable<String[]> listRoles(String ws, String gid) { return Observable.from(new String[0]); }
    @Override public Observable<Void> assignRole(String ws, String gid, String r) { return Observable.of(null); }
    @Override public Observable<Void> removeRole(String ws, String gid, String r) { return Observable.of(null); }
}
