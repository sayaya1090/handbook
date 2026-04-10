package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 캔버스 편집 모드.
 * - VIEW: 조회만. 모든 편집 비활성.
 * - LAYOUT: 박스 이동/리사이즈. 더블클릭 인라인 편집 비활성.
 * - TYPE: 타입 이름/속성 인라인 편집. 드래그 이동 비활성.
 */
@Singleton
public class CanvasMode {
    public enum Mode { VIEW, LAYOUT, TYPE, READONLY }

    private final BehaviorSubject<Mode> subject = behavior(Mode.LAYOUT);

    @Inject CanvasMode() {}

    public Observable<Mode> observable() { return subject.asObservable(); }
    public Mode getValue() { return subject.getValue(); }

    public void setMode(Mode mode) { subject.next(mode); }

    /** VIEW, READONLY가 아닌 모든 모드에서 편집 가능 (키보드 단축키 등) */
    public boolean isEditable() { return subject.getValue() != Mode.VIEW && subject.getValue() != Mode.READONLY; }

    /** READONLY 모드: RBAC 권한 부족 시 모든 편집 비활성 (드래그, 컨텍스트 메뉴, 삭제 포함) */
    public boolean isReadOnly() { return subject.getValue() == Mode.READONLY; }

    /** LAYOUT 모드: 드래그 이동, 리사이즈 활성 */
    public boolean isLayoutMode() { return subject.getValue() == Mode.LAYOUT; }

    /** TYPE 모드: 인라인 편집 활성 */
    public boolean isTypeMode() { return subject.getValue() == Mode.TYPE; }

    public void subscribe(Consumer<Mode> consumer) {
        subject.subscribe(consumer::accept);
    }
}
