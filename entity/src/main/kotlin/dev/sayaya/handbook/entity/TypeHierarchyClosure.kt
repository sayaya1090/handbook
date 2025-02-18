package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable

@Table(name = "type_hierarchy_closure", indexes = [
    Index(columnList = "ancestor"),
    Index(columnList = "descendant")
]) @Entity
@IdClass(TypeHierarchyClosure.Companion.TypeHierarchyClosureId::class)
internal class TypeHierarchyClosure {
    @Id @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "ancestor", nullable = false)
    lateinit var ancestor: Type // 복합 키의 첫 번째 필드

    @Id @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "descendant", nullable = false)
    lateinit var descendant: Type // 복합 키의 두 번째 필드

    @Column(nullable = false) var depth: Int = 0

    companion object {
        data class TypeHierarchyClosureId(
            val ancestor: Type = Type(),
            val descendant: Type = Type()
        ) : Serializable
    }
}
