package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Item
import tables.*
import data.*

@Serializable
class Raiting (
    override var id: Int = -1,
    val studentId: Int,
    val courseId: Int
): Item {

    val getValue
        get() = calculateRaiting()

    private val student = students.read(studentId)
    private val course = courses.read(courseId)

    private fun getTasks() : Map<Task, Grade> {
        val result = mutableMapOf<Task, Grade>()
        val tasksList = course?.getTasks()
        val studentGrades = transaction {
            gradeTable.select { gradeTable.studentId eq EntityID(student!!.id, studentTable) }
                    .mapNotNull { gradeTable.readResult(it) }
        }.toList()
        tasksList?.forEach { task ->
            studentGrades.forEach { grade ->
                if (task.id == grade.taskId) {
                    result[task] = grade
                }
            }
        }
        return result.toMap()
    }

    private fun calculateRaiting() : Double{
        var value = 0.0
        if (course != null && student != null) {
            getTasks().forEach{ currentPair ->
                value+= (calculateWeight(currentPair.key.typeId)) * (currentPair.value.value / currentPair.key.maxValue)
            }
        }
        return String.format("%.2f", value).toDouble()
    }

    private fun calculateWeight(type_id: Int):Double{
        val type = transaction {
            typeTable.select { typeTable.id eq EntityID(type_id, taskTable) }
                .firstOrNull()?.let { typeTable.readResult(it) }
        }
        return if (type != null) {
            when (type.name.toLowerCase()) {
                weight[0].first -> weight[0].second
                weight[1].first -> weight[1].second
                weight[2].first -> weight[2].second
                else -> 0.0
            }
        } else 0.0
    }
}