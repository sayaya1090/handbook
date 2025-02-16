package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable

@Table(name = "attribute")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "attribute_type", discriminatorType = DiscriminatorType.STRING)
@IdClass(Attribute.Companion.AttributeId::class) // 복합 키 클래스 설정
internal abstract class Attribute {
    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "type", nullable = false)
    lateinit var type: Type
    @Id
    @Column(name = "name", length = 32, nullable = false, updatable = false)
    lateinit var name: String

    @Column(nullable = false) open var nullable: Boolean = false
    @Column open var description: String? = null
    companion object {
        data class AttributeId(
            val type: Type = Type(),
            val name: String = ""
        ) : Serializable
    }
}
