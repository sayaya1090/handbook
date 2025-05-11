package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition.*;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.HashSet;
import java.util.Set;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class AttributeTypeDefinitionNative {
    @JsProperty public String baseType;
    @JsProperty public AttributeTypeDefinitionNative[] arguments;
    @JsProperty public JsPropertyMap<Object> constraints;
    @JsProperty public String[] extensions;
    @JsProperty public String referencedType;

    @JsOverlay @JsIgnore
    public AttributeTypeDefinition toDomain() {
        return switch (baseType) {
            case ("Value") -> new ValueType();
            case ("Array") -> new ArrayType(arguments[0].toDomain());
            case ("Map") -> new MapType(arguments[0].toDomain(), arguments[1].toDomain());
            case ("File") -> new FileType(new HashSet<>(Set.of(extensions)));
            case ("Document") -> new DocumentType(referencedType);
            default -> throw new IllegalArgumentException("Unsupported AttributeTypeDefinition baseType: " + baseType);
        };
    }
    @JsOverlay @JsIgnore
    public static AttributeTypeDefinitionNative from(AttributeTypeDefinition domainObj) {
        if (domainObj == null) return null;
        AttributeTypeDefinitionNative nativeDef = new AttributeTypeDefinitionNative();
        nativeDef.baseType = domainObj.baseType().name();
        if(domainObj instanceof ArrayType) nativeDef.arguments = new AttributeTypeDefinitionNative[]{AttributeTypeDefinitionNative.from(((ArrayType) domainObj).type())};
        else if(domainObj instanceof MapType) nativeDef.arguments = new AttributeTypeDefinitionNative[]{AttributeTypeDefinitionNative.from(((MapType) domainObj).key()), AttributeTypeDefinitionNative.from(((MapType) domainObj).value())};
        else if(domainObj instanceof FileType) nativeDef.extensions = ((FileType) domainObj).extensions().toArray(new String[0]);
        else if(domainObj instanceof DocumentType) nativeDef.referencedType = ((DocumentType) domainObj).referencedType();

        /*switch (domainObj) {
            case ArrayType arrayType        -> nativeDef.arguments = arrayType.arguments().stream().map(AttributeTypeDefinitionNative::from).toArray(AttributeTypeDefinitionNative[]::new);
            case MapType mapType            -> nativeDef.arguments = mapType.arguments().stream().map(AttributeTypeDefinitionNative::from).toArray(AttributeTypeDefinitionNative[]::new);
            case FileType fileType          -> nativeDef.extensions = fileType.extensions().stream().toArray(String[]::new);
            case DocumentType documentType  -> nativeDef.referencedType = documentType.referencedType();
            default -> {}
        }*/
        nativeDef.constraints = Js.asPropertyMap(new Object());
        // domainObj.constraints().forEach((key, value) -> nativeDef.constraints.set(key, value));
        return nativeDef;
    }
/*

    @JsOverlay
    @JsIgnore // This method is for internal conversion, not part of the native JS object API
    public static AttributeTypeDefinitionNative from(AttributeTypeDefinition domainObj) {
        if (domainObj == null) {
            return null;
        }

        AttributeTypeDefinitionNative nativeObj = new AttributeTypeDefinitionNative();
        nativeObj.baseType = AttributeTypeNative.fromDomain(domainObj.baseType());
        nativeObj.constraints = convertFromDomainConstraints(domainObj.constraints());

        if (domainObj instanceof ValueType) {
            // No specific properties to set beyond baseType and constraints for ValueType
        } else if (domainObj instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) domainObj;
            nativeObj.type = AttributeTypeDefinitionNative.from(arrayType.type()); // Recursive call
        } else if (domainObj instanceof MapType) {
            MapType mapType = (MapType) domainObj;
            nativeObj.key = AttributeTypeDefinitionNative.from(mapType.key());     // Recursive call
            nativeObj.value = AttributeTypeDefinitionNative.from(mapType.value()); // Recursive call
        } else if (domainObj instanceof FileType) {
            FileType fileType = (FileType) domainObj;
            nativeObj.extensions = fileType.extensions().toArray(new String[0]);
        } else if (domainObj instanceof DocumentType) {
            DocumentType documentType = (DocumentType) domainObj;
            nativeObj.referencedType = documentType.referencedType();
        } else {
            throw new IllegalArgumentException("Unsupported AttributeTypeDefinition type: " + domainObj.getClass().getName());
        }
        return nativeObj;
    }


    @JsOverlay
    private static Map<String, Serializable> convertToDomainConstraints(JsPropertyMap<Object> nativeConstraints) {
        if (nativeConstraints == null || nativeConstraints.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Serializable> domainConstraints = new HashMap<>();
        nativeConstraints.forEach(key -> {
            Object value = nativeConstraints.get(key);
            // Only allow basic Serializable types for direct mapping
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                domainConstraints.put(key, (Serializable) value);
            } else {
                System.err.println("Warning: Skipping unsupported constraint value type for key '" + key + "': " + (value != null ? value.getClass().getName() : "null"));
            }
        });
        return domainConstraints;
    }

    @JsOverlay
    private static JsPropertyMap<Object> convertFromDomainConstraints(Map<String, Serializable> domainConstraints) {
        if (domainConstraints == null || domainConstraints.isEmpty()) {
            return JsPropertyMap.of(); // Return an empty JS object
        }
        JsPropertyMap<Object> nativeConstraints = JsPropertyMap.of();
        domainConstraints.forEach((key, value) -> {
            // Only allow basic Serializable types for direct mapping
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                nativeConstraints.set(key, value);
            } else {
                System.err.println("Warning: Skipping unsupported constraint value type for key '" + key + "': " + (value != null ? value.getClass().getName() : "null"));
            }
        });
        return nativeConstraints;
    }*/
}
