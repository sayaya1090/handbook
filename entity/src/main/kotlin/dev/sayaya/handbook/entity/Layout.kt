package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.io.Serializable
import java.time.Instant
import java.util.*

/*
 * 날짜구간 fragment마다 layout이 1:1 대응된다.
 */
@Table(name = "layout")
@Entity
@IdClass(Layout.Companion.LayoutId::class)
internal class Layout {
    @Id @Column(name = "workspace") lateinit var workspace: UUID
    @Id @Column(name = "id") lateinit var id: UUID
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(name = "effective_at", nullable = false) lateinit var effectDateTime: Instant
    @Column(name = "expire_at", nullable = false) lateinit var expireDateTime: Instant

    companion object {
        data class LayoutId (
            val workspace: UUID = UUID.randomUUID(),
            val id: UUID = UUID.randomUUID(),
        ) : Serializable
    }
}