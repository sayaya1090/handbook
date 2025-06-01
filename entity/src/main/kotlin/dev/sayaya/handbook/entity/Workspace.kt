package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.*

@Table(name = "workspace", indexes=[
    Index(columnList = "name")
]) @Entity
internal class Workspace {
    @Id lateinit var id: UUID
    @Column(length = 32, nullable = false) lateinit var name: String
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) lateinit var lastModifyDateTime: Instant
    @LastModifiedBy @ManyToOne @JoinColumn(name = "last_modified_by", nullable = false) lateinit var lastModifyBy: User
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
}