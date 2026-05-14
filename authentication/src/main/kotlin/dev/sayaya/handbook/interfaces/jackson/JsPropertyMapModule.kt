package dev.sayaya.handbook.interfaces.jackson

import jsinterop.base.JsPropertyMap
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.*
import tools.jackson.databind.module.SimpleModule
import java.util.*

/**
 * GWT JsPropertyMap 인터페이스를 JVM에서 지원하기 위한 Jackson 모듈.
 * 
 * **책임:** 
 * 1. 역직렬화: JsPropertyMap 타입의 필드를 역직렬화할 때, Map 기반의 Proxy 객체를 생성하여 주입한다.
 * 2. 직렬화: Proxy 객체를 직렬화할 때 이를 Map으로 간주하여 처리함으로써, Proxy 내부 필드(h 등)에 대한 리플렉션 접근 오류를 방지한다.
 */
class JsPropertyMapModule : SimpleModule() {
    init {
        addDeserializer(JsPropertyMap::class.java, JsPropertyMapDeserializer())
        addSerializer(JsPropertyMap::class.java, JsPropertyMapSerializer())
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> createProxy(map: MutableMap<String, T>): JsPropertyMap<T> {
            return java.lang.reflect.Proxy.newProxyInstance(
                JsPropertyMap::class.java.classLoader,
                arrayOf(JsPropertyMap::class.java, Map::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "get" -> map[args[0] as String]
                    "set" -> {
                        val key = args[0] as String
                        map[key] = args[1] as T
                        null
                    }
                    "forEach" -> {
                        val callback = args[0]
                        val callbackMethod = callback.javaClass.methods.firstOrNull { it.declaringClass != Any::class.java }
                        map.keys.forEach { key ->
                            callbackMethod?.invoke(callback, key)
                        }
                        null
                    }
                    "entrySet" -> map.entries
                    "keySet" -> map.keys
                    "values" -> map.values
                    "size" -> map.size
                    "isEmpty" -> map.isEmpty()
                    "containsKey" -> map.containsKey(args[0])
                    "containsValue" -> map.containsValue(args[0])
                    "hashCode" -> map.hashCode()
                    "equals" -> map == args[0]
                    "toString" -> map.toString()
                    else -> null
                }
            } as JsPropertyMap<T>
        }
    }

    private class JsPropertyMapSerializer : ValueSerializer<JsPropertyMap<*>>() {
        override fun serialize(value: JsPropertyMap<*>, gen: JsonGenerator, ctxt: SerializationContext) {
            if (value is Map<*, *>) {
                gen.writeStartObject()
                for ((k, v) in value) {
                    gen.writeName(k.toString())
                    if (v == null) gen.writeNull()
                    else ctxt.writeValue(gen, v)
                }
                gen.writeEndObject()
            } else {
                gen.writeStartObject()
                gen.writeEndObject()
            }
        }
    }

    private class JsPropertyMapDeserializer(
        private val contentType: JavaType? = null
    ) : ValueDeserializer<JsPropertyMap<*>>() {
        override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): ValueDeserializer<*> {
            val type = property?.type ?: ctxt.contextualType
            val contentType = type?.containedType(0)
            return if (contentType != null) JsPropertyMapDeserializer(contentType) else this
        }

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JsPropertyMap<*> {
            val map = mutableMapOf<String, Any?>()
            if (p.currentToken() == JsonToken.START_OBJECT) {
                while (p.nextToken() != JsonToken.END_OBJECT) {
                    val key = p.currentName()
                    p.nextToken()
                    map[key] = if (contentType != null && contentType.rawClass != Any::class.java) {
                        ctxt.readValue(p, contentType)
                    } else {
                        p.readValueAs(Any::class.java)
                    }
                }
            }
            return java.lang.reflect.Proxy.newProxyInstance(
                JsPropertyMap::class.java.classLoader,
                arrayOf(JsPropertyMap::class.java, Map::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "get" -> {
                        val key = args[0] as String
                        val value = map[key]
                        if (value is Map<*, *> && !method.returnType.isInstance(value)) {
                            val context = p.objectReadContext()
                            if (context is tools.jackson.databind.ObjectMapper) {
                                val javaType = context.typeFactory.constructType(method.genericReturnType)
                                context.convertValue(value, javaType)
                            } else value
                        } else value
                    }
                    "set" -> {
                        val key = args[0] as String
                        map[key] = args[1]
                        null
                    }
                    "forEach" -> {
                        val callback = args[0]
                        val callbackMethod = callback.javaClass.methods.firstOrNull { it.declaringClass != Any::class.java }
                        map.keys.forEach { key ->
                            callbackMethod?.invoke(callback, key)
                        }
                        null
                    }
                    // Map 인터페이스 구현 (Jackson 직렬화 호환성 및 Kotlin 편의성)
                    "entrySet" -> map.entries
                    "keySet" -> map.keys
                    "values" -> map.values
                    "size" -> map.size
                    "isEmpty" -> map.isEmpty()
                    "containsKey" -> map.containsKey(args[0])
                    "containsValue" -> map.containsValue(args[0])
                    "hashCode" -> map.hashCode()
                    "equals" -> map == args[0]
                    "toString" -> map.toString()
                    else -> null
                }
            } as JsPropertyMap<*>
        }
    }
}
