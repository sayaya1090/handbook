package dev.sayaya.handbook.entity

import jakarta.persistence.*
import java.io.Serializable

@Table(name = "type_hierarchy_closure", indexes = [
    Index(columnList = "ancestor"),
    Index(columnList = "descendant")
])
@Entity
class TypeHierarchyClosure {
    @EmbeddedId var id: TypeHierarchyId = TypeHierarchyId(Type(), Type())
    @Column(nullable = false) var depth: Int = 0

    companion object {
        @Embeddable
        @JvmRecord
        data class TypeHierarchyId (
            @ManyToOne(cascade = [CascadeType.REMOVE]) @JoinColumn(name = "ancestor", nullable = false) val ancestor: Type = Type(),
            @ManyToOne(cascade = [CascadeType.REMOVE]) @JoinColumn(name = "descendant", nullable = false) val descendant: Type = Type()
        ) : Serializable
    }
}