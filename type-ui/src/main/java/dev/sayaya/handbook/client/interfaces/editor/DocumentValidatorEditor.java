package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.TypeList;
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

    public DocumentValidatorEditor(TypeList typeList) {
        this.typeList = typeList;
        select = SelectElementBuilder.select().outlined().label("Referenced type");
        root = div().css("validator-editor").element();
        root.appendChild(select.element());
    }

    @Override
    public void load(AttributeTypeValue value) {
        // 드롭다운 재생성 (옵션 갱신)
        root.innerHTML = "";
        select = SelectElementBuilder.select().outlined().label("Referenced type");
        select.option().value("").text("-- select --").done();

        Set<TypeValue> types = typeList.getValue();
        if (types != null) {
            for (TypeValue type : types) {
                select.option().value(type.id).text(type.id + " (" + type.version + ")").done();
            }
        }
        root.appendChild(select.element());

        if (value != null && value.referencedType != null) {
            select.selectByValue(value.referencedType);
        }
    }

    @Override
    public AttributeTypeValue collect() {
        String ref = select.value();
        return AttributeTypeValue.document(ref != null && !ref.isEmpty() ? ref : null);
    }

    @Override
    public HTMLElement element() { return root; }
}
