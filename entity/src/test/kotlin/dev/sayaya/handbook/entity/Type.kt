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
    @CreatedBy @Column(name = "created_by", length=128, nullable = false) private lateinit var createBy: String
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) private lateinit var astModifyDateTime: LocalDateTime
    @LastModifiedBy @Column(name = "last_modified_by", length=128, nullable = false) private lateinit var lastModifyBy: String
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
    @ElementCollection @CollectionTable(name = "attributes", joinColumns = [JoinColumn(name = "type")]) var attributes: List<String> = listOf()
}