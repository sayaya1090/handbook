package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Table(name = "type")
@Entity
class Type {
    @Id @Column(length = 16) lateinit var id: String
    @CreatedDate @Column(name = "created_at", nullable = false) private lateinit var createDateTime: LocalDateTime
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) private lateinit var createBy: User
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) private lateinit var astModifyDateTime: LocalDateTime
    @LastModifiedBy @ManyToOne @JoinColumn(name = "last_modified_by", nullable = false) private lateinit var lastModifyBy: User
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
    @ElementCollection @CollectionTable(name = "attributes", joinColumns = [JoinColumn(name = "type")]) var attributes: List<String> = listOf()
}