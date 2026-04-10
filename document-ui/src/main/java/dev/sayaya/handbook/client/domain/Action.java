package dev.sayaya.handbook.client.domain;

/** 실행/롤백이 가능한 사용자 액션 인터페이스. Undo/Redo 스택에서 관리된다. */
public interface Action {
    void execute();
    void rollback();
}
