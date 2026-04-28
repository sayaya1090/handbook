package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.TypeValue;

/**
 * {@link TypeElement}의 Dagger AssistedFactory.
 *
 * <p><b>책임:</b> TypeValue와 Position을 인자로 받아 TypeElement 인스턴스를 생성한다.
 * 나머지 의존성(ActionManager, PositionMap 등)은 DI 그래프에서 주입된다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeElement} — 생성 대상 (AssistedInject 사용)</li>
 *   <li>{@link TypeValue} — Assisted 파라미터</li>
 *   <li>{@link Position} — Assisted 파라미터</li>
 * </ul></p>
 * <p><b>주의:</b> BoxElementModule에서 Dagger에 바인딩된다.</p>
 */
@AssistedFactory
public interface BoxElementFactory {
    TypeElement create(TypeValue type, Position position);
}
