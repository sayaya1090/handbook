package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.handbook.client.domain.AttributeValue;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.CreateBoxAction;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.client.usecase.action.EditBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import dev.sayaya.handbook.usecase.MutationReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * 에이전트의 MutateCommand(changes 문자열 배열)를 type-ui Action으로 변환하여 실행한다.
 *
 * <p>지원 명령어:
 * <ul>
 *   <li>{@code CREATE type:<id>} — 새 타입 생성</li>
 *   <li>{@code DELETE type:<typeKey>} — 타입 삭제</li>
 *   <li>{@code ADD field:<typeKey>:<attrName>:type=<attrType>} — 속성 추가</li>
 *   <li>{@code REMOVE field:<typeKey>:<attrName>} — 속성 삭제</li>
 *   <li>{@code SET type:<typeKey>:description=<value>} — 타입 설명 변경</li>
 * </ul>
 */
@Singleton
public class AgentMutationHandler {
    private final ActionManager actionManager;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final LayoutProvider layoutProvider;

    @Inject
    AgentMutationHandler(ActionManager actionManager, TypeList typeList, PositionMap positionMap,
                         ChangeTracker tracker, LayoutProvider layoutProvider,
                         MutationReceiver mutationReceiver) {
        this.actionManager = actionManager;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.layoutProvider = layoutProvider;

        mutationReceiver.mutations().subscribe(changes -> {
            if (changes == null) return;
            for (String change : changes) {
                processChange(change);
            }
        });
    }

    private void processChange(String change) {
        if (change == null || change.isEmpty()) return;
        String[] parts = change.split("\\s+", 2);
        if (parts.length < 2) return;
        String command = parts[0].toUpperCase();
        String operand = parts[1];

        switch (command) {
            case "CREATE": processCreate(operand); break;
            case "DELETE": processDelete(operand); break;
            case "ADD":    processAdd(operand); break;
            case "REMOVE": processRemove(operand); break;
            case "SET":    processSet(operand); break;
        }
    }

    /** CREATE type:customer */
    private void processCreate(String operand) {
        if (!operand.startsWith("type:")) return;
        String id = operand.substring(5);
        var period = layoutProvider.getValue();
        if (period == null) return;
        TypeValue newType = TypeValue.create(id, "1.0", period.effectDateTime, period.expireDateTime);
        Position pos = Position.of(50, 80, 240, 160);
        actionManager.execute(new ComplexAction(
                new CreateBoxAction(typeList, positionMap, tracker, newType, pos),
                new PushOutOverlapAction(positionMap, newType.key(), 10)
        ));
    }

    /** DELETE type:customer:1.0 */
    private void processDelete(String operand) {
        if (!operand.startsWith("type:")) return;
        String typeKey = operand.substring(5);
        for (TypeValue type : typeList.getValue()) {
            if (type.key().equals(typeKey)) {
                actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                break;
            }
        }
    }

    /** ADD field:customer:1.0:phone:type=text */
    private void processAdd(String operand) {
        if (!operand.startsWith("field:")) return;
        String rest = operand.substring(6);
        // format: typeKey:attrName:type=attrType
        String[] fieldParts = rest.split(":");
        if (fieldParts.length < 4) return;
        String typeKey = fieldParts[0] + ":" + fieldParts[1];
        String attrName = fieldParts[2];
        String typeSpec = fieldParts.length > 3 ? fieldParts[3] : "";

        TypeValue type = findType(typeKey);
        if (type == null) return;

        AttributeTypeValue attrType = parseAttrType(typeSpec);
        int nextOrder = (type.attributes != null ? type.attributes.length : 0) + 1;
        AttributeValue newAttr = AttributeValue.of(attrName, nextOrder, attrType);

        AttributeValue[] oldAttrs = type.attributes != null ? type.attributes : new AttributeValue[0];
        AttributeValue[] newAttrs = Arrays.copyOf(oldAttrs, oldAttrs.length + 1);
        newAttrs[oldAttrs.length] = newAttr;
        TypeValue after = type.withAttributes(newAttrs);
        actionManager.execute(new EditBoxAction(typeList, tracker, type, after));
    }

    /** REMOVE field:customer:1.0:phone */
    private void processRemove(String operand) {
        if (!operand.startsWith("field:")) return;
        String rest = operand.substring(6);
        String[] fieldParts = rest.split(":");
        if (fieldParts.length < 3) return;
        String typeKey = fieldParts[0] + ":" + fieldParts[1];
        String attrName = fieldParts[2];

        TypeValue type = findType(typeKey);
        if (type == null || type.attributes == null) return;

        AttributeValue[] newAttrs = Arrays.stream(type.attributes)
                .filter(a -> !a.name.equals(attrName))
                .toArray(AttributeValue[]::new);
        TypeValue after = type.withAttributes(newAttrs);
        actionManager.execute(new EditBoxAction(typeList, tracker, type, after));
    }

    /** SET type:customer:1.0:description=고객 정보 */
    private void processSet(String operand) {
        if (!operand.startsWith("type:")) return;
        String rest = operand.substring(5);
        // format: typeKey:property=value
        int eqIdx = rest.indexOf('=');
        if (eqIdx < 0) return;
        String keyAndProp = rest.substring(0, eqIdx);
        String value = rest.substring(eqIdx + 1);

        int lastColon = keyAndProp.lastIndexOf(':');
        if (lastColon < 0) return;
        String typeKey = keyAndProp.substring(0, lastColon);
        String property = keyAndProp.substring(lastColon + 1);

        TypeValue type = findType(typeKey);
        if (type == null) return;

        TypeValue after;
        switch (property) {
            case "description": after = type.withDescription(value); break;
            case "parent":      after = type.withParent(value); break;
            default: return;
        }
        actionManager.execute(new EditBoxAction(typeList, tracker, type, after));
    }

    private TypeValue findType(String typeKey) {
        for (TypeValue t : typeList.getValue()) {
            if (t.key().equals(typeKey)) return t;
        }
        return null;
    }

    private AttributeTypeValue parseAttrType(String spec) {
        if (spec.startsWith("type=")) spec = spec.substring(5);
        switch (spec.toLowerCase()) {
            case "number":   return AttributeTypeValue.number(null, null);
            case "date":     return AttributeTypeValue.date(null, null);
            case "bool":     return AttributeTypeValue.bool();
            case "file":     return AttributeTypeValue.file(null);
            default:         return AttributeTypeValue.text();
        }
    }
}
