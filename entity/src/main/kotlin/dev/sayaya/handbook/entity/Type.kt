package dev.sayaya.handbook.entity

import jakarta.persistence.*

@Table(name = "type")
@Entity
internal class Type {
    @Id @Column(length = 64) lateinit var id: String
    @ManyToOne @JoinColumn(name = "parent") var parent: Type? = null
    companion object {
        fun of(id: String, parent: Type?) = Type().apply {
            this.id = id
            this.parent = parent
        }
    }
}