package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.*

@Table(name = "\"group\"")
@Entity
internal class Group {
    @Id var id: UUID = UUID.randomUUID()
    @Column(length = 16, nullable = false) lateinit var name: String
    @CreatedDate @Column(name = "created_at", nullable = false) private lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) private lateinit var createBy: User
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) private lateinit var lastModifyDateTime: Instant
    @LastModifiedBy @ManyToOne @JoinColumn(name = "last_modified_by", nullable = false) private lateinit var lastModifyBy: User

    @ManyToMany @JoinTable(name = "group_member",
        joinColumns = [JoinColumn(name = "\"group\"")],
        inverseJoinColumns = [JoinColumn(name = "member")])
    lateinit var members: List<User>
    @ManyToMany @JoinTable(name = "group_role",
        joinColumns = [JoinColumn(name = "\"group\"")],
        inverseJoinColumns = [JoinColumn(name = "role")])
    lateinit var roles: List<Role>
}
