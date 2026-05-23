package com.taskmanager.model

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class TaskPriority { LOW, MEDIUM, HIGH, URGENT }

@Entity
@Table(name = "tasks")
class Task(
    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    val board: Board,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    var column: BoardColumn,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    var assignee: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var priority: TaskPriority = TaskPriority.MEDIUM,

    @Column(nullable = false)
    var position: Int = 0,

    @Column(name = "due_date")
    var dueDate: LocalDate? = null,

    @Convert(converter = StringListConverter::class)
    @Column(name = "labels")
    var labels: List<String> = emptyList(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @Id
    @UuidGenerator
    var id: UUID? = null
}

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attr: List<String>?): String =
        attr?.filter { it.isNotBlank() }?.joinToString(",") ?: ""

    override fun convertToEntityAttribute(data: String?): List<String> =
        if (data.isNullOrBlank()) emptyList() else data.split(",").filter { it.isNotBlank() }
}
