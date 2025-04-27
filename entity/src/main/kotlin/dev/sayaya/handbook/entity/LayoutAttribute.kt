package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable
import java.util.*

@Table(name = "layout_attribute")
@Entity
@IdClass(LayoutAttribute.Companion.AttributeLayoutId::class)
internal class LayoutAttribute {
    @Id @Column(name = "workspace") lateinit var workspace: UUID
    @Id @Column(name = "layout") lateinit var layout: UUID
    @Id @Column(name = "type") lateinit var type: String
    @Id @Column(name = "version") lateinit var version: String
    @Id @Column(name = "name") lateinit var name: String
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumns(
        JoinColumn(name = "workspace", insertable = false, updatable = false),
        JoinColumn(name = "layout", insertable = false, updatable = false)
    ) private lateinit var layoutObj: Layout
    companion object {
        data class AttributeLayoutId (
            val workspace: UUID = UUID.randomUUID(),
            val layout: UUID = UUID.randomUUID(),
            val type: String = "",
            val version: String = "",
            val name: String = ""
        ) : Serializable
    }
}