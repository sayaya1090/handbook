package dev.sayaya.handbook.client.domain;

/*
  사용자의 조작을 나타내는 인터페이스
  Undo, Redo를 위해 사용된다. 생성 시 롤백 함수를 같이 넘겨준다.
 */
public interface Action {
    void execute();
    void rollback();
}
