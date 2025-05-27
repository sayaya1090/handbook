package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import java.io.Serializable
import java.util.*

@Table(name = "attribute")
@Entity
internal class Attribute {
    @EmbeddedId lateinit var id: AttributeId
    @ManyToOne @JoinColumns(
        JoinColumn(name = "workspace", insertable = false, updatable = false),
        JoinColumn(name = "type", insertable = false, updatable = false)
    ) @OnDelete(action = OnDeleteAction.CASCADE)
    private lateinit var typeObj: Type
    @Column(name = "\"order\"", columnDefinition = "smallint") var order: Short = 0
    fun name() = id.name
    fun type(newType: Type) {
        typeObj = newType
        id = if (::id.isInitialized) AttributeId(workspace = newType.workspace, typeId = newType.id, name = id.name)
        else AttributeId(workspace = newType.workspace, typeId = newType.id, name = "")
    }
    fun type() = typeObj
    fun name(newName: String) {
        id = if (::typeObj.isInitialized) AttributeId(workspace = typeObj.workspace, typeId = typeObj.id, name = newName)
        else AttributeId(workspace = UUID.randomUUID(), typeId = UUID.randomUUID(), name = newName)
    }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attribute_type", columnDefinition = "jsonb", nullable = false)
    lateinit var attributeType: AttributeTypeDefinition
    @Column(nullable = false) var nullable: Boolean = false
    @Column var description: String? = null

    companion object {
        @Embeddable
        data class AttributeId (
            @Column(name = "workspace", updatable = false) val workspace: UUID = UUID.randomUUID(),
            @Column(name = "type", updatable = false) val typeId: UUID = UUID.randomUUID(),
            @Column(name = "name", length = 32, nullable = false, updatable = false) val name: String = ""
        ) : Serializable
        fun of(type: Type, name: String, attributeType: AttributeTypeDefinition, order: Short, nullable: Boolean = false, description: String? = null): Attribute = Attribute().apply {
            type(type) // typeObj 및 id 설정
            name(name) // id의 name 설정
            this.attributeType = attributeType
            this.order = order
            this.nullable = nullable
            this.description = description
        }
    }
}
