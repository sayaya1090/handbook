package dev.sayaya.handbook.client.usecase;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.action.*;
import dev.sayaya.handbook.domain.*;
import dev.sayaya.handbook.usecase.MutationReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 에이전트의 MutateCommand(changes 문자열 배열)를 type-ui Action으로 변환하여 실행한다.
 */
@Singleton
public class AgentMutationHandler {
    private final ActionManager actionManager;
    private final Map<String, MutationStrategy> strategies;

    @Inject
    AgentMutationHandler(ActionManager actionManager, TypeList typeList, PositionMap positionMap,
                         ChangeTracker tracker, LayoutProvider layoutProvider,
                         TypeSearchProvider typeSearchProvider,
                         MutationReceiver mutationReceiver) {
        this.actionManager = actionManager;
        this.strategies = Map.of(
            "CREATE", new CreateTypeStrategy(typeList, positionMap, tracker, layoutProvider, typeSearchProvider),
            "DELETE", new DeleteTypeStrategy(typeList, tracker),
            "ADD",    new AddFieldStrategy(typeList, positionMap, tracker, layoutProvider),
            "REMOVE", new RemoveFieldStrategy(typeList, positionMap, tracker, layoutProvider),
            "SET",    new SetPropertyStrategy(typeList, tracker)
        );

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

        MutationStrategy strategy = strategies.get(command);
        if (strategy != null) {
            Action action = strategy.parse(operand);
            if (action != null) {
                actionManager.execute(action);
            }
        }
    }
}

interface MutationStrategy {
    Action parse(String operand);
}

class CreateTypeStrategy implements MutationStrategy {
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final LayoutProvider layoutProvider;
    private final TypeSearchProvider typeSearchProvider;

    CreateTypeStrategy(TypeList typeList, PositionMap positionMap, ChangeTracker tracker, 
                       LayoutProvider layoutProvider, TypeSearchProvider typeSearchProvider) {
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.layoutProvider = layoutProvider;
        this.typeSearchProvider = typeSearchProvider;
    }

    @Override
    public Action parse(String operand) {
        if (!operand.startsWith("type:")) return null;
        String id = operand.substring(5);
        var period = layoutProvider.getValue();
        if (period == null) return null;
        
        Set<String> activeKeys = typeSearchProvider.getVisibleTypes().stream()
                .map(Type::key).collect(Collectors.toSet());
        
        Type newType = Type.create(id, "1.0", period.effectDateTime(), period.expireDateTime());
        Position pos = Position.of(50, 80, 240, 160);
        return new ComplexAction(
                new CreateBoxAction(typeList, positionMap, tracker, layoutProvider, newType, pos),
                new PushOutOverlapAction(positionMap, newType.key(), 10, activeKeys)
        );
    }
}

class DeleteTypeStrategy implements MutationStrategy {
    private final TypeList typeList;
    private final ChangeTracker tracker;

    DeleteTypeStrategy(TypeList typeList, ChangeTracker tracker) {
        this.typeList = typeList;
        this.tracker = tracker;
    }

    @Override
    public Action parse(String operand) {
        if (!operand.startsWith("type:")) return null;
        String typeKey = operand.substring(5);
        for (Type type : typeList.getValue()) {
            if (type.key().equals(typeKey)) {
                return new DeleteBoxAction(typeList, tracker, type);
            }
        }
        return null;
    }
}

class AddFieldStrategy implements MutationStrategy {
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final LayoutProvider layoutProvider;

    AddFieldStrategy(TypeList typeList, PositionMap positionMap, ChangeTracker tracker, LayoutProvider layoutProvider) {
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.layoutProvider = layoutProvider;
    }

    @Override
    public Action parse(String operand) {
        if (!operand.startsWith("field:")) return null;
        String rest = operand.substring(6);
        String[] fieldParts = rest.split(":");
        if (fieldParts.length < 4) return null;
        String typeKey = fieldParts[0] + ":" + fieldParts[1];
        String attrName = fieldParts[2];
        String typeSpec = fieldParts.length > 3 ? fieldParts[3] : "";

        Type type = findType(typeKey);
        if (type == null) return null;

        AttributeType attrType = parseAttrType(typeSpec);
        int nextOrder = (type.attributes() != null ? type.attributes().length : 0) + 1;
        Attribute newAttr = Attribute.create(attrName, attrName, nextOrder, attrType);

        Attribute[] oldAttrs = type.attributes() != null ? type.attributes() : new Attribute[0];
        Attribute[] newAttrs = Arrays.copyOf(oldAttrs, oldAttrs.length + 1);
        newAttrs[oldAttrs.length] = newAttr;
        Type after = type.withAttributes(newAttrs);
        return new EditBoxAction(typeList, positionMap, tracker, layoutProvider, type, after);
    }

    private Type findType(String typeKey) {
        for (Type t : typeList.getValue()) {
            if (t.key().equals(typeKey)) return t;
        }
        return null;
    }

    private AttributeType parseAttrType(String spec) {
        if (spec.startsWith("type=")) spec = spec.substring(5);
        switch (spec.toLowerCase()) {
            case "number":   return AttributeType.number();
            case "date":     return AttributeType.date();
            case "bool":     return AttributeType.bool();
            case "file":     return AttributeType.text();
            default:         return AttributeType.text();
        }
    }
}

class RemoveFieldStrategy implements MutationStrategy {
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final LayoutProvider layoutProvider;

    RemoveFieldStrategy(TypeList typeList, PositionMap positionMap, ChangeTracker tracker, LayoutProvider layoutProvider) {
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.layoutProvider = layoutProvider;
    }

    @Override
    public Action parse(String operand) {
        if (!operand.startsWith("field:")) return null;
        String rest = operand.substring(6);
        String[] fieldParts = rest.split(":");
        if (fieldParts.length < 3) return null;
        String typeKey = fieldParts[0] + ":" + fieldParts[1];
        String attrName = fieldParts[2];

        Type type = findType(typeKey);
        if (type == null || type.attributes() == null) return null;

        Attribute[] newAttrs = Arrays.stream(type.attributes())
                .filter(a -> !a.name().equals(attrName))
                .toArray(Attribute[]::new);
        Type after = type.withAttributes(newAttrs);
        return new EditBoxAction(typeList, positionMap, tracker, layoutProvider, type, after);
    }

    private Type findType(String typeKey) {
        for (Type t : typeList.getValue()) {
            if (t.key().equals(typeKey)) return t;
        }
        return null;
    }
}

class SetPropertyStrategy implements MutationStrategy {
    private final TypeList typeList;
    private final ChangeTracker tracker;

    SetPropertyStrategy(TypeList typeList, ChangeTracker tracker) {
        this.typeList = typeList;
        this.tracker = tracker;
    }

    @Override
    public Action parse(String operand) {
        if (!operand.startsWith("type:")) return null;
        String rest = operand.substring(5);
        int eqIdx = rest.indexOf('=');
        if (eqIdx < 0) return null;
        String keyAndProp = rest.substring(0, eqIdx);
        String value = rest.substring(eqIdx + 1);

        int lastColon = keyAndProp.lastIndexOf(':');
        if (lastColon < 0) return null;
        String typeKey = keyAndProp.substring(0, lastColon);
        String property = keyAndProp.substring(lastColon + 1);

        Type type = findType(typeKey);
        if (type == null) return null;

        return null;
    }

    private Type findType(String typeKey) {
        for (Type t : typeList.getValue()) {
            if (t.key().equals(typeKey)) return t;
        }
        return null;
    }
}
