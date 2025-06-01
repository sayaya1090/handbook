package dev.sayaya.handbook.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "validation_task", indexes = [
    Index(columnList = "status, priority, created_at"), // 기본적인 처리 순서용 인덱스
    Index(columnList = "created_at"), // requested_at 기반 조회용 인덱스 (NULLS FIRST/LAST는 DB 레벨에서 조정 필요)
    Index(columnList = "status, started_at"), // 오래된 작업 조회용 인덱스
    Index(columnList = "workspace, document, last")  // Document 참조용 인덱스
])
internal class ValidationTask {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID
    @ManyToOne @JoinColumns(
        JoinColumn(name = "workspace", insertable = false, updatable = false),
        JoinColumn(name = "document", insertable = false, updatable = false)
    ) @OnDelete(action = OnDeleteAction.CASCADE)
    private lateinit var document: Document
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results", columnDefinition = "jsonb")
    var results: Map<String, Boolean>? = null
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: TaskStatus = TaskStatus.NEW
    @Column(name = "priority", nullable = false)
    var priority: Int = 0 // 기본 우선순위
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
    @Column(name = "started_at")
    var startedAt: Instant? = null
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0
    @Column(name = "last_error", columnDefinition = "TEXT")
    var lastError: String? = null
    @Column(name="\"last\"", nullable = false, columnDefinition = "boolean DEFAULT true") var last: Boolean = true

    companion object {
        enum class TaskStatus {
            NEW,        // 작업이 생성되어 처리 대기 중
            PROCESSING, // 작업이 워커에 의해 처리 중
            DONE,       // 작업 성공적으로 완료
            FAILED      // 작업 실패
        }
    }
}