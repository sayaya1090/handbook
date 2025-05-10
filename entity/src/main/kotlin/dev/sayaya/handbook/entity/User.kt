package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.*

@Table(name = "\"user\"", indexes = [Index(columnList = "provider, account", unique = true)])
@Entity
internal class User {
    @Id lateinit var id: UUID
    @Column(name = "provider", length = 32, nullable = false) lateinit var provider: String
    @Column(name = "account", length = 64, nullable = false) lateinit var account: String
    @Column(length = 16, nullable = false) lateinit var name: String
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) lateinit var lastModifyDateTime: Instant
    @Column(name = "last_login_at") val lastLoginDateTime: Instant? = null
    @Column(columnDefinition = "VARCHAR(12) DEFAULT 'ACTIVATED'::VARCHAR NOT NULL") var state: String = "ACTIVATED"
}
