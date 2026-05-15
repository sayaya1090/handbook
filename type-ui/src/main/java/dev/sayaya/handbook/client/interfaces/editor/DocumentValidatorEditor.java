package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.AttributeType;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import java.util.Set;

import static org.jboss.elemento.Elements.div;

/**
 * Document 타입 참조 대상 선택 에디터.
 *
 * <p><b>책임:</b> 현재 레이아웃의 타입 목록을 MD3 Select 드롭다운으로 표시하여
 * 참조할 타입을 선택하게 한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link TypeList} — 현재 레이아웃의 타입 목록</li>
 *   <li>{@link SelectElementBuilder} — MD3 Select 드롭다운 (sayaya-ui)</li>
 * </ul></p>
 */
public class DocumentValidatorEditor implements ValidatorEditor {
    private final HTMLDivElement root;
    private SelectElementBuilder.OutlinedSelectElementBuilder select;
    private final TypeList typeList;
    private String currentRef;

    public DocumentValidatorEditor(TypeList typeList) {
        this.typeList = typeList;
        select = SelectElementBuilder.select().outlined().label("Referenced type");
        root = div().css("validator-editor").element();
        root.appendChild(select.element());
    }

    @Override
    public void load(AttributeType value) {
        root.innerHTML = "";
        select = SelectElementBuilder.select().outlined().label("Referenced type");
        select.option().value("").text("-- select --").done();

        Set<Type> types = typeList.getValue();
        if (types != null) {
            for (Type type : types) {
                select.option().value(type.id()).text(type.id() + " (" + type.version() + ")").done();
            }
        }
        
        select.onChange(e -> currentRef = select.value());
        root.appendChild(select.element());

        if (value != null && value.referencedType() != null) {
            currentRef = value.referencedType();
            select.selectByValue(value.referencedType());
        } else {
            currentRef = null;
        }
    }

    @Override
    public AttributeType collect() {
        String ref = select.value();
        // MD3 컴포넌트 특성상, 사용자 상호작용 없이 다이얼로그가 닫히면 select.value()가 빈 값일 수 있으므로 백업 필드를 참조한다.
        if (ref == null || ref.isEmpty()) {
            return AttributeType.document(currentRef);
        }
        return AttributeType.document(ref);
    }

    @Override
    public HTMLElement element() { return root; }
}
