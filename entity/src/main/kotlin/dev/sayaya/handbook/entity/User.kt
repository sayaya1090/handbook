package dev.sayaya.handbook.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

@Table(name = "\"user\"")
@Entity
internal class User {
    @Id @Column(length = 64) lateinit var id: String
    @Column(length = 16, nullable = false) lateinit var name: String
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) lateinit var lastModifyDateTime: Instant
    @Column(name = "last_login_at") val lastLoginDateTime: Instant? = null
}
