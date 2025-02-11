package dev.sayaya.handbook.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import java.util.*

@Table(name = "role")
@Entity
internal class Role {
    @Id var id: UUID = UUID.randomUUID()
    @Column(length = 16, nullable = false) lateinit var name: String
    @CreatedDate @Column(name = "created_at", nullable = false) private lateinit var createDateTime: LocalDateTime
    @CreatedBy @Column(name = "created_by", length=128, nullable = false) private lateinit var createBy: String
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) private lateinit var astModifyDateTime: LocalDateTime
    @LastModifiedBy @Column(name = "last_modified_by", length=128, nullable = false) private lateinit var lastModifyBy: String
}
