package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

@Table(name = "type")
@Entity
internal class Type {
    @Id @Column(length = 64) lateinit var id: String
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) lateinit var lastModifyDateTime: Instant
    @LastModifiedBy @ManyToOne @JoinColumn(name = "last_modified_by", nullable = false) lateinit var lastModifyBy: User
    @ManyToOne @OnDelete(action = OnDeleteAction.CASCADE) @JoinColumn(name = "parent") var parent: Type? = null
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
    @Column(nullable = false) var primitive: Boolean = false
    companion object {
        fun of(user: User, id: String, parent: Type?) = Type().apply {
            this.id = id
            description = id
            createDateTime = Instant.now()
            createBy = user
            lastModifyDateTime = Instant.now()
            lastModifyBy = user
            this.parent = parent
        }
    }
}