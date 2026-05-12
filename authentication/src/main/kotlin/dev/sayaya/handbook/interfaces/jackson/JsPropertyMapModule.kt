package dev.sayaya.handbook.interfaces.jackson

import jsinterop.base.JsPropertyMap
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.JavaType
import tools.jackson.databind.module.SimpleModule
import java.util.*

/**
 * GWT JsPropertyMap 인터페이스를 JVM에서 지원하기 위한 Jackson 모듈.
 * 
 * **책임:** JsPropertyMap 타입의 필드를 역직렬화할 때, Map 기반의 Proxy 객체를 생성하여 주입한다.
 * 이를 통해 GWT 공유 도메인 모델을 백엔드(JVM)에서도 그대로 사용할 수 있다.
 */
class JsPropertyMapModule : SimpleModule() {
    init {
        addDeserializer(JsPropertyMap::class.java, JsPropertyMapDeserializer())
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
                        // GWT JsPropertyMap.forEach implementation if needed
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
