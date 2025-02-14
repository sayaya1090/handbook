package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable

@Table(name = "type_hierarchy_closure", indexes = [
    Index(columnList = "ancestor"),
    Index(columnList = "descendant")
])
@Entity
internal class TypeHierarchyClosure {
    @EmbeddedId var id: TypeHierarchyId = TypeHierarchyId(Type(), Type())
    @Column(nullable = false) var depth: Int = 0

    companion object {
        @Embeddable
        @JvmRecord
        data class TypeHierarchyId (
            @ManyToOne @OnDelete(action = OnDeleteAction.CASCADE) @JoinColumn(name = "ancestor", nullable = false) val ancestor: Type = Type(),
            @ManyToOne @OnDelete(action = OnDeleteAction.CASCADE) @JoinColumn(name = "descendant", nullable = false) val descendant: Type = Type()
        ) : Serializable
    }
}