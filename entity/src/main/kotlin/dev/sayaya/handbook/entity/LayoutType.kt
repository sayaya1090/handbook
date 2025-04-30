package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable
import java.util.*

/*
 * Type에 대해 (type name, version)으로 정의되는 약한 참조를 사용한다.
 * 레이아웃 내 모든 type의 name, version을 사용한 UUID를 layout을 하고, 전체 레이아웃에 대한 정보는 레이아웃 키로 모든
 * 엔티티를 한꺼번에 조회한다.
 */
@Table(name = "layout_type", indexes=[
    Index(columnList = "workspace, layout"),
    Index(columnList = "workspace, type, version")
]) @Entity
@IdClass(LayoutType.Companion.TypeLayoutId::class)
internal class LayoutType {
    @Id @Column(name = "workspace") lateinit var workspace: UUID
    @Id @Column(name = "layout") lateinit var layout: UUID
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumns(
        JoinColumn(name = "workspace", insertable = false, updatable = false),
        JoinColumn(name = "layout", insertable = false, updatable = false)
    ) private lateinit var layoutObj: Layout

    @Id @Column(name = "type", length = 64) lateinit var type: String
    @Id @Column(name = "version", length = 64) lateinit var version: String
    @Column(nullable = false, columnDefinition = "smallint") val x: Short = 0
    @Column(nullable = false, columnDefinition = "smallint") val y: Short = 0
    @Column(nullable = false, columnDefinition = "smallint") val width: Short = 0
    @Column(nullable = false, columnDefinition = "smallint") val height: Short = 0

    companion object {
        data class TypeLayoutId (
            val workspace: UUID = UUID.randomUUID(),
            val layout: UUID = UUID.randomUUID(),
            val type: String = "",
            val version: String = ""
        ) : Serializable
    }
}