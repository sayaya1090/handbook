package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.DateFormatter;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/**
 * 타입의 유효기간(시작/종료일)을 수정하는 다이얼로그.
 * 
 * <p><b>책임:</b> 현재 선택된 타입의 시작 일시와 종료 일시를 입력받아
 * {@link dev.sayaya.handbook.client.usecase.action.EditTBoxDateAction}을 트리거한다.</p>
 */
@Singleton
public class DateCorrectionDialog implements IsElement<HTMLElement> {
    private final DialogElementBuilder _this;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder effectInput;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder expireInput;
    private final ToastContainer toastContainer;
    private Consumer<DateResult> callback;
    private Type currentType;

    public record DateResult(double effect, double expire) {}

    @Inject
    DateCorrectionDialog(ToastContainer toastContainer) {
        this.toastContainer = toastContainer;
        effectInput = TextFieldElementBuilder.textField().outlined()
                .attr("id", "date-correction-start")
                .label("Effect Date (YYYY-MM-DD)");
        
        expireInput = TextFieldElementBuilder.textField().outlined()
                .attr("id", "date-correction-expire")
                .label("Expire Date (YYYY-MM-DD or ∞)");

        _this = DialogElementBuilder.dialog()
                .attr("id", "date-correction-dialog")
                .headline("Correct Validity Period")
                .content(div().css("type-dialog-content").add(effectInput).add(expireInput))
                .actions(ButtonElementBuilder.button().filled().attr("id", "date-correction-apply").text("Apply").on(EventType.click, e -> apply()));
        
        _this.actions(ButtonElementBuilder.button().text().attr("id", "date-correction-close").text("Cancel").on(EventType.click, e -> _this.close()));
    }

    public void show(Type type, Consumer<DateResult> callback) {
        elemental2.dom.DomGlobal.console.log("[DateCorrectionDialog] show() - type: " + type.key());
        elemental2.dom.DomGlobal.console.log("[DateCorrectionDialog] Attached to DOM: " + (_this.element().parentNode != null));
        this.callback = callback;
        this.currentType = type;
        
        // Reset errors
        effectInput.element().removeAttribute("error");
        effectInput.element().removeAttribute("error-text");
        expireInput.element().removeAttribute("error");
        expireInput.element().removeAttribute("error-text");
        
        _this.show();
        
        // 브라우저 렌더링(레이아웃 계산) 후에 값을 채워야 MD3 라벨 애니메이션 NaN 에러를 방지할 수 있음
        elemental2.dom.DomGlobal.requestAnimationFrame(t -> {
            effectInput.value(DateFormatter.format(type.effectDateTime()));
            expireInput.value(DateFormatter.format(type.expireDateTime()));
        });
    }

    private void apply() {
        String effectVal = effectInput.value();
        String expireVal = expireInput.value();
        elemental2.dom.DomGlobal.console.log("[DateCorrectionDialog] apply() - raw effect: " + effectVal + ", raw expire: " + expireVal);
        
        effectInput.element().removeAttribute("error");
        expireInput.element().removeAttribute("error");
        
        try {
            double effect = DateFormatter.parse(effectVal);
            double expire = DateFormatter.parse(expireVal);
            
            if (effect >= expire && expire != 253402214400000.0) {
                effectInput.element().setAttribute("error", "");
                effectInput.element().setAttribute("error-text", "Start date must be before end date");
                return;
            }
            
            elemental2.dom.DomGlobal.console.log("[DateCorrectionDialog] apply() - parsed effect: " + effect + ", parsed expire: " + expire);
            
            if (callback != null) callback.accept(new DateResult(effect, expire));
            toastContainer.show(ToastLevel.SUCCESS, "Date corrected for " + currentType.id());
            _this.close();
        } catch (IllegalArgumentException e) {
            effectInput.element().setAttribute("error", "");
            effectInput.element().setAttribute("error-text", "Invalid date format");
        }
    }

    @Override
    public HTMLElement element() { return _this.element(); }
}
