package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable
import java.util.*

@Table(name = "attribute")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "attribute_type", discriminatorType = DiscriminatorType.STRING)
internal abstract class Attribute {
    @EmbeddedId lateinit var id: AttributeId
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumns(
        JoinColumn(name = "workspace", insertable = false, updatable = false),
        JoinColumn(name = "type", insertable = false, updatable = false)
    ) private lateinit var typeObj: Type
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
    @Column(nullable = false) open var nullable: Boolean = false
    @Column open var description: String? = null
    companion object {
        @Embeddable
        data class AttributeId (
            @Column(name = "workspace", updatable = false) val workspace: UUID = UUID.randomUUID(),
            @Column(name = "type", updatable = false) val typeId: UUID = UUID.randomUUID(),
            @Column(name = "name", length = 32, nullable = false, updatable = false) val name: String = ""
        ) : Serializable
    }
}
