package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Table(name = "document", indexes = [
    Index(columnList = "serial"),
    Index(columnList = "type"),
    Index(columnList = "effective_date, expiry_date"),
    Index(columnList = "attributes")
]) @Entity
class Document {
    @Id var id: UUID = UUID.randomUUID()
    @Column(length = 16, nullable = false) lateinit var serial: String
    @ManyToOne @JoinColumn(name = "type", nullable = false) lateinit var type: Type
    @CreatedDate @Column(name = "created_at", nullable = false) private lateinit var createDateTime: LocalDateTime
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) private lateinit var createBy: User
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) private lateinit var astModifyDateTime: LocalDateTime
    @LastModifiedBy @ManyToOne @JoinColumn(name = "last_modified_by", nullable = false) private lateinit var lastModifyBy: User
    @Column(name = "effective_date", nullable = false) lateinit var effectiveDate: Instant
    @Column(name = "expiry_date", nullable = false) lateinit var expiryDate: Instant
    @Column(columnDefinition = "jsonb", nullable = false) lateinit var attributes: Serializable
}