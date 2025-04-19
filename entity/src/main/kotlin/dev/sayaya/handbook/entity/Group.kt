package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.time.Instant
import java.util.*

@Table(name = "\"group\"")
@Entity
internal class Group {
    @EmbeddedId lateinit var id: GroupId
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumns(JoinColumn(name = "workspace", insertable = false, updatable = false))
    private lateinit var workspace: Workspace
    fun name() = id.name
    @CreatedDate @Column(name = "created_at", nullable = false) private lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) private lateinit var createBy: User
    @LastModifiedDate @Column(name = "last_modified_at", nullable = false) private lateinit var lastModifyDateTime: Instant
    @LastModifiedBy @ManyToOne @JoinColumn(name = "last_modified_by", nullable = false) private lateinit var lastModifyBy: User

    @ManyToMany @JoinTable(name = "group_member",
        joinColumns = [JoinColumn(name = "workspace", referencedColumnName = "workspace"), JoinColumn(name = "\"group\"", referencedColumnName = "name")],
        inverseJoinColumns = [JoinColumn(name = "member")],
        indexes = [Index(columnList = "member")])
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var members: List<User>
    @ManyToMany @JoinTable(name = "group_role",
        joinColumns = [JoinColumn(name = "workspace", referencedColumnName = "workspace"), JoinColumn(name = "\"group\"", referencedColumnName = "name")],
        inverseJoinColumns = [JoinColumn(name = "role")],
        indexes = [Index(columnList = "role")])
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var roles: List<Role>

    companion object {
        @Embeddable
        data class GroupId (
            @JvmField @Column(name = "workspace") val workspace: UUID = UUID.randomUUID(),
            @JvmField @Column(name = "name", length = 32) val name: String = ""
        ) : Serializable
    }
}
