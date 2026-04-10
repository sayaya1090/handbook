package dev.sayaya.handbook.domain;

/**
 * 실행/롤백이 가능한 사용자 액션 인터페이스.
 *
 * <p><b>책임:</b> Command 패턴의 execute/rollback 계약을 정의하여 ActionManager의 Undo/Redo 스택에서 관리된다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 인터페이스, ActionManager가 사용)</li></ul></p>
 */
public interface Action {
    void execute();
    void rollback();
}
