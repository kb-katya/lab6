package model

import `object`.DateSerializer
import kotlinx.serialization.*
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Item
import java.time.LocalDate
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import tables.*
import data.*

@Serializable
class Task(
    override var id: Int = -1,
    val name: String,
    val description: String = "",
    val maxValue: Int = 1,
    @Serializable(with = DateSerializer::class)
    val deadline: LocalDate = LocalDate.now(),
    val typeId: Int,
    val courseId: Int,
) : Item {

    fun getGrade(studentName: String): Int{
        val student = students.read().find { it.name == studentName }
        return transaction {
            gradeTable.select {
                gradeTable.studentId eq student?.id
            }.firstOrNull()?.let { gradeTable.readResult(it) }
        }?.value ?: 0
    }

    fun getGrades() = transaction {
        gradeTable.selectAll().mapNotNull { gradeTable.readResult(it) }
    }.filter { it.taskId == this.id }

    fun addGrade(grade: Grade)= transaction {
        gradeTable.insertAndGetId { fill(it, grade) }.value
        true
    }
}